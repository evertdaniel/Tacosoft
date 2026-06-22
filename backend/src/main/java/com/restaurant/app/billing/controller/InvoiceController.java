package com.restaurant.app.billing.controller;

import com.restaurant.app.billing.dto.CreateInvoiceRequest;
import com.restaurant.app.billing.dto.InvoiceDto;
import com.restaurant.app.billing.dto.PaymentRequest;
import com.restaurant.app.billing.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Invoice controller - billing endpoints. Implements SPEC-BILL-001, INV-02 (folio sequence), INV-03
 * (idempotent payments).
 */
@RestController
@RequestMapping("/invoices")
@Tag(name = "Invoices", description = "Invoice and billing operations with financial invariants")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @Operation(
            summary = "Create invoice",
            description =
                    "Generate invoice with sequential folio number (INV-02). Uses pessimistic lock"
                            + " to prevent gaps/duplicates in folio sequence.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Invoice created successfully with assigned folio",
                        content = @Content(schema = @Schema(implementation = InvoiceDto.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request or order already invoiced"),
                @ApiResponse(responseCode = "404", description = "Order not found"),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - JWT token required"),
                @ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - insufficient permissions")
            })
    public ResponseEntity<InvoiceDto> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request) {
        return ResponseEntity.ok(invoiceService.createInvoice(request));
    }

    @PostMapping("/{id}/pay")
    @Operation(
            summary = "Pay invoice",
            description =
                    "Record payment with idempotency guarantee (INV-03). Duplicate reference_id"
                            + " returns existing transaction without creating new payment. Supports"
                            + " partial payments.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Payment recorded successfully",
                        content = @Content(schema = @Schema(implementation = InvoiceDto.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid payment amount or closed cash register"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Invoice or cash register not found"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Duplicate reference_id (idempotent response)"),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - JWT token required"),
                @ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - insufficient permissions")
            })
    public ResponseEntity<InvoiceDto> payInvoice(
            @Parameter(description = "Invoice ID") @PathVariable String id,
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(invoiceService.payInvoice(id, request, userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<InvoiceDto> getInvoice(@PathVariable String id) {
        return ResponseEntity.ok(invoiceService.getInvoice(id));
    }

    @GetMapping
    @Operation(summary = "List all invoices for current restaurant")
    public ResponseEntity<List<InvoiceDto>> listInvoices() {
        return ResponseEntity.ok(invoiceService.listInvoices());
    }

    @GetMapping("/unpaid")
    @Operation(summary = "List unpaid invoices")
    public ResponseEntity<List<InvoiceDto>> listUnpaidInvoices() {
        return ResponseEntity.ok(invoiceService.listUnpaidInvoices());
    }
}
