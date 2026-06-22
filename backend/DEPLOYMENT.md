# Tacosoft Backend Deployment Guide

This guide provides detailed instructions for deploying the Tacosoft backend to various environments, from development to production.

## 📋 Table of Contents

- [Environment Setup](#environment-setup)
- [Database Configuration](#database-configuration)
- [Application Configuration](#application-configuration)
- [Build & Package](#build--package)
- [Deployment Options](#deployment-options)
- [Production Hardening](#production-hardening)
- [Monitoring & Health Checks](#monitoring--health-checks)
- [Backup & Recovery](#backup--recovery)
- [Scaling Considerations](#scaling-considerations)

---

## Environment Setup

### Required Environment Variables

Create an `.env` file or set these in your deployment environment:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=tacosoft
DB_USER=tacosoft_user
DB_PASS=your_secure_password_here

# JWT Configuration
JWT_SECRET=your_super_secret_jwt_key_at_least_32_characters_long

# Frontend Configuration (CORS)
FRONTEND_URL=http://localhost:3000

# Server Configuration
SERVER_PORT=8080

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

### Secrets Management

**Never commit secrets to git**. Use one of these approaches:

#### 1. Environment Variables (Recommended for Production)

```bash
export JWT_SECRET=$(openssl rand -base64 32)
export DB_PASS=$(openssl rand -base64 16)
```

#### 2. Docker Secrets

```yaml
# docker-compose.yml
version: '3.8'
services:
  backend:
    image: tacosoft-backend:latest
    secrets:
      - jwt_secret
      - db_password
    environment:
      JWT_SECRET_FILE: /run/secrets/jwt_secret
      DB_PASS_FILE: /run/secrets/db_password

secrets:
  jwt_secret:
    external: true
  db_password:
    external: true
```

#### 3. Vault/Cloud Secrets (AWS Secrets Manager, Azure Key Vault)

Use your cloud provider's secret management service for production.

---

## Database Configuration

### MySQL 8.0+ Setup

#### 1. Install MySQL

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install mysql-server -y
sudo mysql_secure_installation
```

**macOS:**
```bash
brew install mysql
brew services start mysql
```

**Docker:**
```bash
docker run --name tacosoft-mysql \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=tacosoft \
  -e MYSQL_USER=tacosoft_user \
  -e MYSQL_PASSWORD=secure_password \
  -p 3306:3306 \
  -d mysql:8.0
```

#### 2. Create Database and User

```sql
-- Connect to MySQL
mysql -u root -p

-- Create database
CREATE DATABASE tacosoft 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'tacosoft_user'@'%' 
  IDENTIFIED BY 'secure_password_here';

-- Grant privileges
GRANT ALL PRIVILEGES ON tacosoft.* 
  TO 'tacosoft_user'@'%';

-- Flush and exit
FLUSH PRIVILEGES;
EXIT;
```

#### 3. Configure Connection Pooling

Edit `application-prod.yml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### Flyway Migrations

#### Manual Migration

```bash
# Check migration status
mvn flyway:info

# Run migrations manually
mvn flyway:migrate

# Validate migrations
mvn flyway:validate

# Repair if needed (use with caution)
mvn flyway:repair
```

#### Production Migration Strategy

**Baseline Strategy** (for existing databases):

```bash
# Baseline existing database without running migrations
mvn flyway:baseline -Dflyway.baselineVersion=1
```

**Always**:
- Test migrations in staging first
- Backup database before production migrations
- Never modify committed migrations
- Use `baseline-on-migrate: true` for existing databases

---

## Application Configuration

### Production Profile (`application-prod.yml`)

```yaml
spring:
  application:
    name: tacosoft-backend

  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=true&serverTimezone=UTC
    username: ${DB_USER}
    password: ${DB_PASS}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: validate  # Never use 'update' in production
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: false

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 86400000  # 24 hours in milliseconds

server:
  port: ${SERVER_PORT:8080}
  tomcat:
    threads:
      max: 200
      min-spare: 10
  error:
    include-message: always
    include-binding-errors: always
    include-stacktrace: never
    include-exception: false

cors:
  allowed-origins: ${FRONTEND_URL}
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
  max-age: 3600

logging:
  level:
    root: INFO
    com.restaurant.app: INFO
    org.springframework.security: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n"
  file:
    name: logs/tacosoft.log
    max-size: 10MB
    max-history: 30

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

### JVM Options

```bash
# Production JVM settings
java -jar \
  -Xms512m \
  -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/tacosoft/ \
  -Dspring.profiles.active=prod \
  backend.jar
```

---

## Build & Package

### Maven Build

```bash
# Clean build with production profile
mvn clean package -Pprod -DskipTests

# Full build with tests
mvn clean package -Pprod

# Build Docker image
mvn clean package -Pprod dockerfile:build
```

### Build Artifacts

- **JAR**: `target/backend-0.0.1-SNAPSHOT.jar`
- **Tests**: `target/surefire-reports/`
- **Coverage**: `target/site/jacoco/`

---

## Deployment Options

### Option 1: Traditional JAR Deployment

#### Systemd Service (Linux)

Create `/etc/systemd/system/tacosoft.service`:

```ini
[Unit]
Description=Tacosoft Backend
After=network.target mysql.service

[Service]
Type=simple
User=tacosoft
Group=tacosoft
WorkingDirectory=/opt/tacosoft
EnvironmentFile=/opt/tacosoft/.env
ExecStart=/usr/bin/java -Xms512m -Xmx2g -jar /opt/tacosoft/backend.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=tacosoft

[Install]
WantedBy=multi-user.target
```

Enable and start:

```bash
sudo systemctl enable tacosoft
sudo systemctl start tacosoft
sudo systemctl status tacosoft
```

#### Nginx Reverse Proxy

```nginx
# /etc/nginx/sites-available/tacosoft
server {
    listen 80;
    server_name api.tacosoft.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### Option 2: Docker Deployment

#### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine AS builder
WORKDIR /app
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
```

#### Docker Compose

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: tacosoft-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
      MYSQL_DATABASE: tacosoft
      MYSQL_USER: ${DB_USER}
      MYSQL_PASSWORD: ${DB_PASS}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - tacosoft-net

  backend:
    build: .
    container_name: tacosoft-backend
    environment:
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: tacosoft
      DB_USER: ${DB_USER}
      DB_PASS: ${DB_PASS}
      JWT_SECRET: ${JWT_SECRET}
      FRONTEND_URL: ${FRONTEND_URL}
      SPRING_PROFILES_ACTIVE: prod
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    networks:
      - tacosoft-net
    restart: unless-stopped

volumes:
  mysql_data:

networks:
  tacosoft-net:
    driver: bridge
```

Deploy:

```bash
# Build and start
docker-compose up -d

# View logs
docker-compose logs -f backend

# Stop
docker-compose down
```

### Option 3: Kubernetes Deployment

#### Deployment YAML

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tacosoft-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: tacosoft-backend
  template:
    metadata:
      labels:
        app: tacosoft-backend
    spec:
      containers:
      - name: backend
        image: tacosoft-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: DB_HOST
          valueFrom:
            configMapKeyRef:
              name: tacosoft-config
              key: db-host
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: tacosoft-secrets
              key: jwt-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: tacosoft-backend
spec:
  selector:
    app: tacosoft-backend
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

---

## Production Hardening

### Security Checklist

- [ ] Change default passwords
- [ ] Use strong JWT_SECRET (32+ chars)
- [ ] Enable HTTPS/TLS
- [ ] Configure CORS properly
- [ ] Disable DEBUG logging
- [ ] Enable security headers
- [ ] Use read-only database user for app
- [ ] Rotate secrets regularly
- [ ] Enable database SSL
- [ ] Configure firewall rules

### TLS/SSL Configuration

#### Nginx with Let's Encrypt

```bash
sudo certbot --nginx -d api.tacosoft.com
```

#### Spring Boot with Keystore

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tacosoft
  port: 8443
```

### Security Headers

```java
// SecurityConfig.java
http.headers()
    .xssProtection()
    .and()
    .contentSecurityPolicy("policy")
    .and()
    .frameOptions().deny()
    .and()
    .httpStrictTransportSecurity();
```

---

## Monitoring & Health Checks

### Actuator Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

### Prometheus Configuration

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'tacosoft'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### Grafana Dashboard

Import JVM Dashboard (ID: 4701) and Spring Boot Dashboard (ID: 12900).

---

## Backup & Recovery

### Database Backup

```bash
# Backup
mysqldump -u tacosoft_user -p tacosoft > tacosoft_backup_$(date +%Y%m%d).sql

# Restore
mysql -u tacosoft_user -p tacosoft < tacosoft_backup_20250621.sql
```

### Automated Backup Script

```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/var/backups/tacosoft"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="tacosoft"
DB_USER="tacosoft_user"

mkdir -p $BACKUP_DIR

mysqldump -u $DB_USER -p$DB_PASS $DB_NAME | gzip > $BACKUP_DIR/tacosoft_$DATE.sql.gz

# Keep last 7 days
find $BACKUP_DIR -name "tacosoft_*.sql.gz" -mtime +7 -delete
```

Add to crontab:

```bash
# Daily backup at 2 AM
0 2 * * * /opt/scripts/backup.sh
```

---

## Scaling Considerations

### Vertical Scaling

- Increase JVM heap: `-Xmx4g`
- Add CPU cores
- Increase database connection pool

### Horizontal Scaling

- Deploy multiple instances behind load balancer
- Use sticky sessions for WebSocket
- Scale database independently

### Database Scaling

- **Read replicas**: Use for reporting queries
- **Connection pooling**: Configure HikariCP
- **Indexing**: Optimize slow queries
- **Partitioning**: Consider for large tables

---

## Troubleshooting

### Common Issues

**1. Out of Memory**

```bash
# Check heap usage
jstat -gcutil <pid> 1000

# Analyze heap dump
jmap -dump:format=b,file=heap.hprof <pid>
jhat heap.hprof
```

**2. Database Connection Pool Exhausted**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30
      connection-timeout: 30000
```

**3. Slow API Responses**

- Enable query logging
- Check database indexes
- Review N+1 queries
- Use caching (Redis)

---

## Post-Deployment Verification

After deployment, verify:

```bash
# Health check
curl -f http://localhost:8080/actuator/health || exit 1

# Swagger UI accessible
curl -f http://localhost:8080/swagger-ui.html || exit 1

# Database connectivity
curl -f http://localhost:8080/actuator/health/db || exit 1

# Run smoke tests
mvn test -Dtest=*SmokeTest
```

---

**Version**: 1.0.0 | **Last Updated**: 2025-06-21 | **Status**: Production Ready
