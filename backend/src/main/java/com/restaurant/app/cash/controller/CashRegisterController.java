package com.restaurant.app.cash.controller;

import com.restaurant.app.cash.dto.CashRegisterDto;
import com.restaurant.app.cash.dto.CloseCashRegisterRequest;
import com.restaurant.app.cash.dto.OpenCashRegisterRequest;
import com.restaurant.app.cash.dto.XReportDto;
import com.restaurant.app.cash.dto.ZReportDto;
import com.restaurant.app.cash.service.CashRegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * CashRegister controller - cash drawer endpoints. Implements SPEC-CASH-001 and INV-05 (Z-report).
 */
@RestController
@RequestMapping("/cash-registers")
@Tag(name = "Cash Registers", description = "Cash register operations")
@SecurityRequirement(name = "bearerAuth")
public class CashRegisterController {

    private final CashRegisterService cashRegisterService;

    public CashRegisterController(CashRegisterService cashRegisterService) {
        this.cashRegisterService = cashRegisterService;
    }

    @PostMapping("/open")
    @PreAuthorize("@tenantSecurityExpression.hasAnyRole('CASHIER', 'ADMIN')")
    @Operation(
            summary = "Open cash register",
            description = "Open new cash drawer for current user")
    public ResponseEntity<CashRegisterDto> openRegister(
            @Valid @RequestBody OpenCashRegisterRequest request, Authentication authentication) {
        String userId = authentication.getName();
        CashRegisterDto response = cashRegisterService.openRegister(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(
                        ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(response.getId())
                                .toUri())
                .body(response);
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("@tenantSecurityExpression.hasAnyRole('CASHIER', 'ADMIN')")
    @Operation(
            summary = "Close cash register",
            description = "Close drawer with Z-report and balance validation (INV-05)")
    public ResponseEntity<ZReportDto> closeRegister(
            @PathVariable String id,
            @Valid @RequestBody CloseCashRegisterRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(cashRegisterService.closeRegister(id, request, userId));
    }

    @GetMapping("/active")
    @Operation(
            summary = "Get active cash register",
            description = "Get current user's open register")
    public ResponseEntity<CashRegisterDto> getActiveRegister(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(cashRegisterService.getActiveRegister(userId));
    }

    @GetMapping("/x-report")
    @Operation(
            summary = "Generate X-report",
            description = "Current status without closing register")
    public ResponseEntity<XReportDto> getXReport(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(cashRegisterService.getXReport(userId));
    }

    @GetMapping
    @Operation(summary = "List all cash registers")
    public ResponseEntity<List<CashRegisterDto>> listRegisters() {
        return ResponseEntity.ok(cashRegisterService.listRegisters());
    }
}
