# Design: Spring Boot 3 Backend Implementation

## Change
`implement-backend-spring-boot`

## Executive Summary
Layered Spring Boot 3 architecture with feature-based package structure, implementing Controller → Service → Repository pattern with strict multi-tenancy via `x-restaurant-id` header, JWT authentication with RBAC, MapStruct DTO mapping, STOMP WebSocket for real-time events, and pessimistic locking for financial invariants (folio sequence, idempotent payments).

---

## 1. Architecture Approach

### 1.1 Pattern: Layered Architecture with Feature-Based Packages

**Decision:** Classic 3-tier layered architecture organized by domain feature rather than technical layer.

```
com.restaurant.app
├── config/                  # Cross-cutting configuration
├── security/                # JWT, RBAC, TenantFilter
├── common/                  # Shared utilities, error handling
├── auth/                    # Authentication feature
│   ├── controller/
│   ├── service/
│   ├── dto/
│   └── model/
├── user/                    # User management
├── menu/                    # Sections, categories, products, options
├── order/                   # Orders and order details
├── table/                   # Restaurant tables
├── billing/                 # Bills, invoices, folio sequence (💰)
├── cash/                    # Cash registers, transactions (💰)
├── report/                  # Reporting and analytics
├── supplier/                # Supplier CRUD
└── restaurant/              # Tenant management, production areas
```

**Rationale:**
- Feature-based grouping improves cohesion — all code for a domain lives together
- Easier navigation for developers working on specific business areas
- Aligns with SDD spec structure (each spec → one package)
- Facilitates judgment double enforcement — money paths are clearly isolated in `billing/` and `cash/`

**Rejected Alternatives:**
1. **Technical layering** (com.restaurant.app.controller, .service, .repository): Would scatter related code across directories, harder to navigate
2. **Hexagonal/Clean Architecture:** Overkill for this scope; adds indirection without clear benefit for a monolithic REST API
3. **Domain-Driven Design with bounded contexts:** Premature — domain is cohesive restaurant operations, not multiple contexts

---

## 2. Component Architecture

### 2.1 Layer Responsibilities

| Layer | Responsibility | Key Rules |
|-------|----------------|------------|
| **Controller** | HTTP request handling, DTO mapping, response formatting | No business logic. Only validates input, calls service, returns HTTP status |
| **Service** | Business rules, transaction boundaries, invariant enforcement | `@Transactional`. All state mutations happen here. Throws domain-specific exceptions |
| **Repository** | Data access via Spring Data JPA | Methods accept `restaurantId` parameter. All queries filter by tenant |
| **DTO** | Request/response contracts | Never expose JPA entities. MapStruct for entity ↔ DTO conversion |

### 2.2 Request Flow

```
HTTP Request (with x-restaurant-id)
         ↓
TenantFilter (validates tenant, sets context)
         ↓
JwtAuthenticationFilter (validates JWT)
         ↓
Controller (accepts DTO, validates with Jakarta Bean Validation)
         ↓
Service (@Transactional, enforces invariants)
         ↓
Repository (JPA, filtered by restaurant_id)
         ↓
MySQL
         ↓
Service (maps Entity → DTO via MapStruct)
         ↓
Controller (returns HTTP response)
         ↓
WebSocket broadcasting (if applicable)
```

---

## 3. Data Flow and Integration Points

### 3.1 Multi-Tenancy Flow (ADR-004)

**Component:** `TenantFilter`

```java
@Component
@Order(1)
public class TenantFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        String restaurantId = ((HttpServletRequest) request).getHeader("x-restaurant-id");
        
        if (restaurantId == null) {
            // Public endpoints (login) bypass tenant validation
            chain.doFilter(request, response);
            return;
        }
        
        // Validate user has role in this restaurant
        UserDetails userDetails = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!userDetails.hasRestaurantRole(restaurantId)) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        // Store in thread-local context for repositories
        TenantContext.setRestaurantId(restaurantId);
        chain.doFilter(request, response);
        TenantContext.clear();
    }
}
```

**Integration Point:** Every repository method requires `restaurantId`:

```java
public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId")
    List<Order> findByRestaurantId(@Param("restaurantId") String restaurantId);
    
    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId AND o.id = :id")
    Optional<Order> findByIdAndTenant(@Param("id") UUID id, @Param("restaurantId") String restaurantId);
}
```

### 3.2 Authentication Flow (SPEC-AUTH-001)

**Components:**
1. `JwtAuthenticationFilter`: Validates JWT on every request
2. `AuthService`: Handles login, token generation
3. `UserDetailsAdapter`: Bridge between JPA entity and Spring Security

```java
@Service
public class AuthService {
    public LoginResponse login(LoginRequest request) {
        AppUser user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        
        if (!user.isActive()) {
            throw new UnauthorizedException("User inactive");
        }
        
        String token = jwtService.generateToken(user);
        return LoginResponse.builder()
            .token(token)
            .user(userMapper.toDto(user))
            .currentRestaurant(user.getPrimaryRestaurant())
            .build();
    }
}
```

**Integration Point:** Spring Security filter chain

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .csrf().disable()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(tenantFilter, JwtAuthenticationFilter.class)
        .build();
}
```

### 3.3 Financial Invariant Flow (ADR-003, INV-02)

**Component:** `InvoiceService` with pessimistic locking

```java
@Service
public class InvoiceService {
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public InvoiceDto createInvoice(String restaurantId, CreateInvoiceRequest request) {
        // 1. Lock folio sequence for this restaurant
        FolioSequence folioSeq = folioSequenceRepository
            .lockByRestaurantId(restaurantId); // SELECT ... FOR UPDATE
        
        // 2. Assign folio
        Long nextFolio = folioSeq.getNextFolio();
        folioSeq.setNextFolio(nextFolio + 1);
        
        // 3. Create invoice
        Invoice invoice = new Invoice();
        invoice.setFolio(nextFolio);
        invoice.setRestaurantId(restaurantId);
        // ... set other fields
        
        // 4. Persist
        invoice = invoiceRepository.save(invoice);
        folioSequenceRepository.save(folioSeq);
        
        return invoiceMapper.toDto(invoice);
    }
}
```

**Repository lock method:**

```java
@Query("SELECT fs FROM FolioSequence fs WHERE fs.restaurantId = :restaurantId")
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<FolioSequence> lockByRestaurantId(@Param("restaurantId") String restaurantId);
```

### 3.4 Idempotent Payment Flow (INV-03)

**Component:** `TransactionService` with unique reference

```java
@Service
public class TransactionService {
    @Transactional
    public TransactionDto recordPayment(String invoiceId, PaymentRequest request) {
        // Check for duplicate payment
        if (transactionRepository.existsByReferenceId(invoiceId)) {
            // Already paid — return existing transaction
            return transactionMapper.toDto(
                transactionRepository.findByReferenceId(invoiceId).get()
            );
        }
        
        // Verify cash register is open
        CashRegister cashRegister = cashRegisterRepository
            .findOpenByUserIdAndRestaurantId(
                getCurrentUserId(), 
                TenantContext.getRestaurantId()
            )
            .orElseThrow(() -> new ConflictException("No open cash register"));
        
        // Create transaction
        Transaction tx = Transaction.builder()
            .cashRegister(cashRegister)
            .type(TransactionType.INCOME)
            .amount(request.getAmount())
            .paymentMethod(request.getPaymentMethod())
            .referenceId(invoiceId) // Unique constraint prevents duplicates
            .build();
        
        return transactionMapper.toDto(transactionRepository.save(tx));
    }
}
```

**Database constraint:**

```sql
ALTER TABLE transaction ADD CONSTRAINT uk_tx_reference UNIQUE (reference_id);
```

### 3.5 WebSocket Broadcasting Flow

**Component:** `WebSocketConfig` + `OrderService`

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
}
```

**Broadcasting in service:**

```java
@Service
public class OrderService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        Order order = // ... create order
        
        // Broadcast to restaurant-specific topic
        messagingTemplate.convertAndSend(
            "/topic/restaurant/" + TenantContext.getRestaurantId() + "/orders",
            new OrderCreatedEvent(orderMapper.toDto(order))
        );
        
        return orderMapper.toDto(order);
    }
}
```

---

## 4. ADR-Style Decisions

### ADR-001: Spring Boot 3 + Java 21 (Already Accepted)

**Status:** Accepted (from SDD source document)

**Rationale:**
- Mature ecosystem with enterprise-grade support
- Excellent transaction management for financial operations
- Strong JPA integration for MySQL
- Large talent pool for maintenance

**Rejected Alternatives:**
- Quarkus: Faster startup but smaller ecosystem, less familiar to enterprise teams
- Node.js/Express: Would require rethinking transaction safety for money paths
- .NET: Good, but team expertise is in Java/Spring

### ADR-002: Feature-Based Package Structure

**Status:** New — This design

**Context:** Need to organize ~15 domain modules (auth, orders, billing, etc.) across 3 layers.

**Decision:** Group by domain feature with subdirectories for technical layers.

**Rationale:**
- High cohesion: all code for "orders" lives in `order/`
- Easy to apply judgment double: money paths are isolated in `billing/` and `cash/`
- Aligns with spec structure (each SPEC-XXX maps to one package)
- Easier navigation for developers

**Rejected Alternatives:**
1. **Technical layering** (controller/, service/, repository/ at top level): Scatters related code
2. **DDD bounded contexts**: Overkill for single-domain restaurant system
3. **Flat package structure**: Unscalable beyond 10 classes

**Consequences:**
- Package names may be longer (`com.restaurant.app.order.service.OrderService`)
- Need to enforce via code review to prevent drift

### ADR-003: MapStruct for DTO Mapping

**Status:** New — This design

**Context:** Never expose JPA entities to API. Need efficient entity ↔ DTO conversion.

**Decision:** Use MapStruct at compile time.

**Rationale:**
- Type-safe, compile-time generated code
- Zero runtime reflection overhead
- Clear mapping declarations
- Easy to customize for complex transformations

**Rejected Alternatives:**
1. **Manual mapping**: Error-prone, boilerplate-heavy
2. **ModelMapper**: Runtime reflection, slower, less type-safe
3. **BeanUtils.copyProperties**: Shallow copy only, dangerous for entities

**Consequences:**
- Build-time generation (need to ensure MapStruct processor is configured)
- One mapper interface per DTO

### ADR-004: RFC 7807 Problem Details for Errors

**Status:** New — This design

**Context:** Need consistent error responses across all endpoints.

**Decision:** Implement `@ControllerAdvice` with `ProblemDetail` from Spring Framework.

**Rationale:**
- RFC 7807 is standard for HTTP API errors
- Spring Framework 6+ has built-in support
- Clients can parse errors consistently

**Implementation:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, 
            ex.getMessage()
        );
        problem.setType(URI.create("https://errors.restaurant.app/conflict"));
        problem.setTitle("Conflict");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://errors.restaurant.app/validation"));
        problem.setTitle("Validation failed");
        problem.setProperty("errors", 
            ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField)
                .collect(Collectors.toList())
        );
        return ResponseEntity.badRequest().body(problem);
    }
}
```

### ADR-005: Pessimistic Lock for Folio Sequence

**Status:** Accepted (from ADR-003 in source SDD)

**Context:** Folio must be unique and contiguous per restaurant. High concurrency could cause gaps.

**Decision:** Use `SELECT ... FOR UPDATE` (pessimistic lock) on `folio_sequence`.

**Rationale:**
- Guarantees no gaps or duplicates
- Short transaction window (just increment + insert invoice)
- Database-level lock is reliable

**Rejected Alternatives:**
1. **Optimistic locking** (`@Version`): Could fail under high concurrency, requiring retries
2. **Sequence per restaurant**: Adds complexity, not needed given expected load
3. **Pre-allocate blocks**: Over-optimization, not needed yet

**Consequences:**
- Potential bottleneck at extreme scale (1000+ invoices/sec)
- Can revisit if performance issues arise

### ADR-006: STOMP over SockJS for WebSocket

**Status:** New — This design

**Context:** Frontend uses Socket.IO. Need compatible real-time events.

**Decision:** Spring WebSocket with STOMP protocol and SockJS fallback.

**Rationale:**
- Native Spring support
- STOMP provides pub/sub semantics (topic per restaurant)
- SockJS provides WebSocket fallback for older browsers
- Can be consumed by Socket.IO client with STOMP adapter

**Rejected Alternatives:**
1. **Raw WebSocket**: No pub/sub, would need custom routing
2. **Server-Sent Events (SSE)**: Unidirectional only
3. **Socket.IO server**: Would require additional dependency, Spring WebSocket is sufficient

**Consequences:**
- Frontend may need STOMP client library or Socket.IO STOMP adapter
- Restaurant-scoped topics prevent cross-tenant event leakage

---

## 5. Security Architecture

### 5.1 JWT Structure

```java
public class JwtService {
    private String secret;
    private long expirationMinutes = 120;
    
    public String generateToken(AppUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getId().toString());
        claims.put("username", user.getUsername());
        claims.put("role", user.getPrimaryRole().getName());
        claims.put("restaurantRoles", user.getRestaurantRoles().stream()
            .map(rr -> Map.of(
                "restaurantId", rr.getRestaurantId().toString(),
                "role", rr.getRole().getName()
            ))
            .collect(Collectors.toList())
        );
        
        return Jwts.builder()
            .claims(claims)
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes()), Jwts.SIG.HS256)
            .compact();
    }
}
```

### 5.2 RBAC Matrix

**Enforcement Point:** Method-level security with `@PreAuthorize`

```java
@RestController
@RequestMapping("/orders")
public class OrderController {
    @PostMapping
    @PreAuthorize("hasAnyAuthority('WAITER', 'ADMIN')")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // ...
    }
}

@RestController
@RequestMapping("/invoices")
public class InvoiceController {
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyAuthority('CASHIER', 'ADMIN')")
    public ResponseEntity<InvoiceDto> payInvoice(@PathVariable UUID id, @Valid PaymentRequest request) {
        // ...
    }
}
```

**Custom security expression for multi-tenant RBAC:**

```java
public class TenantSecurityExpression {
    public boolean hasRoleInRestaurant(String role) {
        String restaurantId = TenantContext.getRestaurantId();
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
        return userDetails.hasRoleInRestaurant(restaurantId, role);
    }
}
```

### 5.3 Tenant Isolation

**Three-layer defense:**

1. **Filter layer:** `TenantFilter` validates `x-restaurant-id` against JWT `restaurantRoles`
2. **Service layer:** Explicit `restaurantId` parameter on all business methods
3. **Repository layer:** All queries include `WHERE restaurant_id = ?`

**Example repository contract:**

```java
public interface OrderRepository extends JpaRepository<Order, UUID> {
    // Never use findById(UUID) directly — always tenant-scoped
    Optional<Order> findByIdAndRestaurantId(UUID id, String restaurantId);
    
    // List methods always filter by tenant
    List<Order> findAllByRestaurantId(String restaurantId);
    
    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId AND o.status = :status")
    List<Order> findByRestaurantIdAndStatus(@Param("restaurantId") String restaurantId, 
                                           @Param("status") OrderStatus status);
}
```

---

## 6. Exception Handling Strategy

### 6.1 Exception Hierarchy

```java
// Base exception for application errors
public abstract class AppException extends RuntimeException {
    private final ProblemDetail problemDetail;
    
    protected AppException(HttpStatus status, String message) {
        super(message);
        this.problemDetail = ProblemDetail.forStatusAndDetail(status, message);
    }
    
    public ProblemDetail getProblemDetail() {
        return problemDetail;
    }
}

// Specific domain exceptions
public class NotFoundException extends AppException {
    public NotFoundException(String resource, UUID id) {
        super(HttpStatus.NOT_FOUND, 
              String.format("%s with id %s not found", resource, id));
    }
}

public class ConflictException extends AppException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

public class UnauthorizedException extends AppException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}

public class ForbiddenException extends AppException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
```

### 6.2 Handler Mapping

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ex.getProblemDetail());
    }
    
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ex.getProblemDetail());
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ex.getProblemDetail());
    }
    
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ProblemDetail> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ex.getProblemDetail());
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://errors.restaurant.app/data-integrity"));
        problem.setTitle("Data integrity violation");
        problem.setDetail(ex.getRootCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(URI.create("https://errors.restaurant.app/internal"));
        problem.setTitle("Internal server error");
        // Don't expose stack trace in production
        problem.setDetail("An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
```

---

## 7. Testing Strategy

### 7.1 Test Pyramid

```
          ┌─────────────────┐
          │  E2E (minimal)  │  ← Auth flows + money paths
          ├─────────────────┤
          │   Integration   │  ← Repository + DB with Testcontainers
          ├─────────────────┤
          │     Unit        │  ← Service business logic (Mockito)
          └─────────────────┘
```

### 7.2 Unit Tests (Service Layer)

**Scope:** Business rules, invariants, state transitions.

**Tools:** JUnit 5, Mockito, AssertJ

**Example:**

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private OrderRepository orderRepository;
    @Mock private OrderDetailRepository detailRepository;
    @Mock private TableRepository tableRepository;
    @Mock private ProductRepository productRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    @DisplayName("Should create order and occupy table when IN_PLACE")
    void createOrder_InPlace_OccupiesTable() {
        // Given
        String restaurantId = UUID.randomUUID().toString();
        Table table = new Table();
        table.setId(UUID.randomUUID());
        table.setStatus(TableStatus.AVAILABLE);
        
        CreateOrderRequest request = CreateOrderRequest.builder()
            .tableId(table.getId())
            .type(OrderType.IN_PLACE)
            .people(2)
            .details(List.of(/* ... */))
            .build();
        
        when(tableRepository.findByIdAndRestaurantId(table.getId(), restaurantId))
            .thenReturn(Optional.of(table));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        
        // When
        OrderDto result = orderService.createOrder(restaurantId, request);
        
        // Then
        assertThat(result.getTableId()).isEqualTo(table.getId());
        assertThat(table.getStatus()).isEqualTo(TableStatus.OCCUPIED);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/restaurant/" + restaurantId + "/orders"),
            any(OrderCreatedEvent.class)
        );
    }
    
    @Test
    @DisplayName("Should reject IN_PLACE order without tableId")
    void createOrder_InPlace_WithoutTableId_ThrowsException() {
        CreateOrderRequest request = CreateOrderRequest.builder()
            .type(OrderType.IN_PLACE)
            .people(2)
            .details(List.of())
            .build();
        
        assertThatThrownBy(() -> orderService.createOrder("rest-123", request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("tableId is required for IN_PLACE orders");
    }
}
```

### 7.3 Integration Tests (Repository + DB)

**Scope:** JPA mappings, FK constraints, locks, multi-tenancy.

**Tools:** JUnit 5, Spring Boot Test, Testcontainers (MySQL)

**Example:**

```java
@SpringBootTest
@Testcontainers
class OrderRepositoryIntegrationTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
    
    @Test
    @DisplayName("Should isolate orders by restaurant (INV-06)")
    void findAllByRestaurantId_IsolatesTenants() {
        Restaurant rest1 = restaurantRepository.save(new Restaurant());
        Restaurant rest2 = restaurantRepository.save(new Restaurant());
        
        Order order1 = new Order();
        order1.setRestaurantId(rest1.getId());
        order1.setNum(1);
        orderRepository.save(order1);
        
        Order order2 = new Order();
        order2.setRestaurantId(rest2.getId());
        order2.setNum(1);
        orderRepository.save(order2);
        
        List<Order> rest1Orders = orderRepository.findAllByRestaurantId(rest1.getId().toString());
        List<Order> rest2Orders = orderRepository.findAllByRestaurantId(rest2.getId().toString());
        
        assertThat(rest1Orders).hasSize(1);
        assertThat(rest2Orders).hasSize(1);
        assertThat(rest1Orders.get(0).getId()).isNotEqualTo(rest2Orders.get(0).getId());
    }
}
```

### 7.4 Financial Invariant Tests (💰 Judgment Double Required)

**Test file naming:** `*FinancialInvariantTest.java` or `*InvariantTest.java`

**Must cover:**

```java
@TestForInvariant("INV-02")
@Test
@DisplayName("INV-02: 1000 concurrent invoices should have contiguous folios without gaps")
void concurrentInvoiceCreation_ContiguousFolios() throws Exception {
    String restaurantId = restaurantRepository.save(new Restaurant()).getId().toString();
    int threadCount = 1000;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    List<Invoice> invoices = Collections.synchronizedList(new ArrayList<>());
    
    // Create 1000 invoices concurrently
    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                Invoice invoice = invoiceService.createInvoice(restaurantId, /* ... */);
                invoices.add(invoice);
            } finally {
                latch.countDown();
            }
        });
    }
    
    latch.await(30, TimeUnit.SECONDS);
    executor.shutdown();
    
    // Verify: all folios unique, contiguous, no gaps
    List<Long> folios = invoices.stream()
        .map(Invoice::getFolio)
        .sorted()
        .collect(Collectors.toList());
    
    assertThat(folios).hasSize(threadCount);
    assertThat(folios).doesNotHaveDuplicates();
    
    // Check contiguity
    for (int i = 0; i < folios.size() - 1; i++) {
        assertThat(folios.get(i + 1) - folios.get(i)).isEqualTo(1);
    }
}

@TestForInvariant("INV-03")
@Test
@DisplayName("INV-03: Duplicate payment should not create duplicate transaction")
void duplicatePayment_Idempotent() {
    String restaurantId = /* ... */;
    UUID invoiceId = /* ... */;
    PaymentRequest request = PaymentRequest.builder()
        .amount(BigDecimal.valueOf(100))
        .paymentMethod(PaymentMethod.CASH)
        .build();
    
    // First payment
    TransactionDto tx1 = transactionService.recordPayment(invoiceId, request);
    
    // Second payment (should be idempotent)
    TransactionDto tx2 = transactionService.recordPayment(invoiceId, request);
    
    assertThat(tx1.getId()).isEqualTo(tx2.getId());
    assertThat(transactionRepository.count()).isEqualTo(1);
}
```

### 7.5 API Tests (Controller Layer)

**Scope:** HTTP contracts, status codes, error responses.

**Tools:** MockMvc, `@SpringBootTest`

**Example:**

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private JwtService jwtService;
    
    @Test
    @DisplayName("POST /orders returns 201 with valid request")
    void createOrder_Valid_Returns201() throws Exception {
        String token = jwtService.generateToken(testUser);
        
        mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + token)
                .header("x-restaurant-id", testRestaurant.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tableId": "%s",
                      "type": "IN_PLACE",
                      "people": 2,
                      "details": [
                        {
                          "productId": "%s",
                          "quantity": 1
                        }
                      ]
                    }
                    """.formatted(tableId, productId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.num").exists())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.details").isArray());
    }
    
    @Test
    @DisplayName("POST /orders without x-restaurant-id returns 403")
    void createOrder_WithoutTenant_Returns403() throws Exception {
        String token = jwtService.generateToken(testUser);
        
        mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }
}
```

---

## 8. Database Schema Summary

### 8.1 Migration Strategy

**Tool:** Flyway with numbered migrations

**Naming:** `V{number}__{description}.sql`

**Order:**

```
V1__init_tenancy_users.sql
V2__init_menu.sql
V3__init_tables_clients.sql
V4__init_orders.sql
V5__init_billing.sql          💰 judgment double
V6__init_cash_registers.sql   💰 judgment double
V7__init_folio_sequence.sql   💰 judgment double
V8__init_suppliers.sql
V9__init_reports_views.sql
```

**Rules:**
- Migrations are immutable once merged
- Financial migrations require judgment double
- Never use `ddl-auto: update` in production
- `hibernate.ddl-auto: validate` only

### 8.2 Key Constraints

| Constraint | Purpose | Table |
|------------|---------|-------|
| `UNIQUE (restaurant_id, num)` | INV-01: Order number uniqueness | `order` |
| `UNIQUE (restaurant_id, folio)` | INV-02: Folio uniqueness | `invoice` |
| `UNIQUE (reference_id)` | INV-03: Idempotent payments | `transaction` |
| `FK (restaurant_id)` | INV-06: Tenant isolation | All business tables |

---

## 9. Configuration Summary

### 9.1 application.yml

```yaml
spring:
  application:
    name: restaurant-api
  
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/restaurant}
    username: ${DB_USER:restaurant}
    password: ${DB_PASSWORD:changeme}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate  # Never 'update' in production
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
    open-in-view: false
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080  # Not used with manual JWT

app:
  jwt:
    secret: ${JWT_SECRET:change-this-in-production-min-256-bits}
    expiration-minutes: 120
  
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:3030,http://localhost:5173}
  
  websocket:
    broker:
      relay: # Not using relay (simple broker)
      destination-prefixes: /app
    user-destination-prefix: /user

server:
  port: ${SERVER_PORT:8080}

logging:
  level:
    com.restaurant.app: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

---

## 10. Technology Stack Summary

| Layer | Technology | Purpose |
|-------|------------|---------|
| **Runtime** | Java 21 LTS | Platform |
| **Framework** | Spring Boot 3.3.x | Core framework |
| **Web** | Spring Web MVC | REST API |
| **Security** | Spring Security + JJWT | JWT auth, RBAC |
| **Persistence** | Spring Data JPA + Hibernate 6 | ORM |
| **Database** | MySQL 8 + mysql-connector-j | Data store |
| **Migrations** | Flyway | Schema versioning |
| **Validation** | Jakarta Bean Validation | Request validation |
| **DTO Mapping** | MapStruct 1.5+ | Entity ↔ DTO |
| **WebSocket** | Spring WebSocket + STOMP | Real-time events |
| **API Docs** | springdoc-openapi 2.x | Swagger UI |
| **Testing** | JUnit 5 + Mockito + Testcontainers | Test framework |
| **Build** | Maven (or Gradle) | Dependency management |

---

## 11. Implementation Sequence

### Phase 1: Foundation
1. Project scaffolding (build config, dependencies)
2. Base entities (`Auditable`, `TenantAware`)
3. Security config (JWT filter, RBAC)
4. Tenant filter implementation
5. Exception handling setup
6. Flyway migrations V1-V3 (tenancy, users, menu)

### Phase 2: Core Operations
7. Menu module (sections, categories, products, options)
8. Table module
9. Order module (SPEC-ORDER-001, SPEC-ORDER-002)
10. WebSocket configuration
11. Client module

### Phase 3: Financial (💰 Judgment Double)
12. Flyway migrations V5-V7 (billing, cash, folio)
13. Billing module (SPEC-BILL-001)
14. Cash register module (SPEC-CASH-001)
15. Financial invariant tests (INV-02, INV-03, INV-05)

### Phase 4: Reporting & Suppliers
16. Report module (SPEC-REPORT-001)
17. Supplier module
18. Flyway V9 (reporting views)

### Phase 5: Integration
19. OpenAPI documentation
20. Integration tests (API layer)
21. Performance tests (folio concurrency)
22. Security tests (RBAC, multi-tenancy)

---

## 12. Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| **Folio sequence bottleneck** (high concurrency causing lock contention) | Medium | High | Monitor lock duration. If > 100ms avg, consider pre-allocating folio blocks per restaurant. |
| **Tenant data leakage** (missing `restaurant_id` filter) | Medium | Critical | Repository contract enforced via code review + integration tests for INV-06. |
| **Financial bugs** (incorrect totals, double payment) | Low | Critical | Judgment double (ADR-005). Dedicated invariant tests with concurrent scenarios. |
| **WebSocket connection limits** (STOMP broker memory) | Low | Medium | Spring's simple broker has limits. Consider switching to RabbitMQ/ActiveMQ if needed. |
| **Flyway migration conflicts** (during development) | Medium | Medium | Immutable migrations rule. New changes always add new files, never modify existing. |
| **JWT secret compromise** | Low | High | Use environment variable, rotate periodically. Consider RS256 for production. |
| **MapStruct configuration drift** | Low | Low | CI check: ensure MapStruct processor generates expected mappers. |
| **Testcontainers slow builds** | Medium | Low | Use Testcontainers reuse feature for CI. Consider test slicing. |

---

## 13. Open Questions

1. **Pagos parciales:** SPEC-BILL-001 menciona pagos parciales sin especificar comportamiento. ¿Se permiten? ¿Cómo se trackean múltiples transacciones por invoice?
2. **Diferencias de arqueo:** SPEC-CASH-001 indica que se registre la diferencia pero no especifica acción correctiva. ¿Se crea `Transaction` de ajuste?
3. **Stock management:** SPEC-MENU-001 indica `manageStock` pero no especifica validación al crear `OrderDetail`. ¿Se bloquea pedido si `quantity = 0`?
4. **Multi-cajero:** INV-CASH-001 asume una caja por restaurante. ¿El negocio evolucionará a múltiples cajeros simultáneos?

---

## 14. Contract with Frontend

### 14.1 API Contract Stability

The backend commits to the contract defined in the SDD source document (`docs/SDD-sistema-restaurante.md` §7). Any breaking change requires:

1. Versioned endpoint (e.g., `/v2/orders`)
2. Coordination with frontend team
3. Migration period for frontend to adopt new contract

### 14.2 Error Response Contract

All errors follow RFC 7807:

```json
{
  "type": "https://errors.restaurant.app/conflict",
  "title": "Conflict",
  "status": 409,
  "detail": "Table is already occupied",
  "instance": "/orders"
}
```

### 14.3 WebSocket Event Contract

Events are broadcast to restaurant-scoped topics:

- `/topic/restaurant/{restaurantId}/orders` — order lifecycle
- `/topic/restaurant/{restaurantId}/tables` — table status changes

Event payloads use the same DTOs as REST endpoints.

---

## 15. Non-Functional Requirements

| NFR | Target | Measurement |
|-----|--------|-------------|
| **Latency (p95)** | < 200ms for API calls | APM monitoring |
| **Throughput** | 500 req/sec | Load testing |
| **Uptime** | 99.5% | Monitoring alerts |
| **Transaction time** | < 50ms for folio lock | Database metrics |
| **API documentation** | 100% coverage | OpenAPI scan |
| **Test coverage** | > 80% line coverage | JaCoCo reports |

---

*Design document generated for Spring Boot 3 backend implementation. All financial paths (billing, cash) require judgment double per ADR-005.*
