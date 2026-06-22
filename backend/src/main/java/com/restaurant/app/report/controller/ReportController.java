package com.restaurant.app.report.controller;

import com.restaurant.app.report.dto.*;
import com.restaurant.app.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller for report endpoints. Implements SPEC-REPORT-001. */
@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "Business analytics and reporting")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * T9.1: Get dashboard report with sales summary, active orders, table occupancy, low stock
     * alerts. GET /reports/dashboard
     */
    @GetMapping("/dashboard")
    @Operation(
            summary = "Dashboard report",
            description = "Get sales summary, active orders, table occupancy, and low stock alerts")
    public ResponseEntity<DashboardReportDto> getDashboard() {
        DashboardReportDto response = reportService.getDashboard();
        return ResponseEntity.ok(response);
    }

    /**
     * T9.2: Get sales report with date range filters, revenue by payment method, top products,
     * period comparison. GET /reports/sales
     */
    @GetMapping("/sales")
    @Operation(
            summary = "Sales report",
            description =
                    "Get sales analytics with date range, payment methods, top products, and period"
                            + " comparison")
    public ResponseEntity<SalesSummaryDto> getSalesReport(
            @Parameter(description = "Start date (ISO format)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @Parameter(description = "End date (ISO format)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        SalesSummaryDto response = reportService.getSalesReport(start, end);
        return ResponseEntity.ok(response);
    }

    /**
     * T9.3: Get product report with sales by product, margin analysis, stock turnover. GET
     * /reports/products
     */
    @GetMapping("/products")
    @Operation(
            summary = "Product report",
            description = "Get product sales, margin analysis, and stock turnover")
    public ResponseEntity<List<ProductReportDto>> getProductReport(
            @Parameter(description = "Start date (ISO format)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @Parameter(description = "End date (ISO format)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        List<ProductReportDto> response = reportService.getProductReport(start, end);
        return ResponseEntity.ok(response);
    }

    /**
     * T9.4: Get financial report with income/expense breakdown, cash register reconciliation,
     * invoice summary. GET /reports/finances
     */
    @GetMapping("/finances")
    @Operation(
            summary = "Financial report",
            description =
                    "Get income/expense breakdown, cash register reconciliation, and invoice"
                            + " summary (💰)")
    public ResponseEntity<FinancialReportDto> getFinancialReport(
            @Parameter(description = "Start date (ISO format)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @Parameter(description = "End date (ISO format)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        FinancialReportDto response = reportService.getFinancialReport(start, end);
        return ResponseEntity.ok(response);
    }

    /** T9.5: Get footfall report with peak hours and traffic patterns. GET /reports/footfall */
    @GetMapping("/footfall")
    @Operation(
            summary = "Footfall report",
            description = "Get peak hours and customer traffic patterns")
    public ResponseEntity<FootfallReportDto> getFootfallReport(
            @Parameter(description = "Report date (ISO format)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date) {
        LocalDate reportDate = date != null ? date : LocalDate.now();

        FootfallReportDto response = reportService.getFootfallReport(reportDate);
        return ResponseEntity.ok(response);
    }

    /** T9.5: Get staff planning report with workload analysis. GET /reports/staff-planning */
    @GetMapping("/staff-planning")
    @Operation(
            summary = "Staff planning report",
            description = "Get workload analysis and staff recommendations")
    public ResponseEntity<StaffPlanningReportDto> getStaffPlanningReport(
            @Parameter(description = "Report date (ISO format)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date) {
        LocalDate reportDate = date != null ? date : LocalDate.now();

        StaffPlanningReportDto response = reportService.getStaffPlanningReport(reportDate);
        return ResponseEntity.ok(response);
    }
}
