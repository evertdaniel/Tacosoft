package com.restaurant.app.report.service;

import com.restaurant.app.report.dto.*;
import com.restaurant.app.report.repository.ReportRepository;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for report operations. Implements SPEC-REPORT-001. */
@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /**
     * T9.1: Get dashboard report with sales summary, active orders, table occupancy, low stock
     * alerts.
     */
    @Transactional(readOnly = true)
    public DashboardReportDto getDashboard() {
        String restaurantId = TenantContext.getRestaurantId();

        ReportRepository.DashboardRow dashboard = reportRepository.getDashboard(restaurantId);
        Integer totalTables = reportRepository.getTotalTables(restaurantId);
        Integer lowStockProducts = reportRepository.getLowStockCount(restaurantId);

        return DashboardReportDto.builder()
                .occupiedTables(dashboard.occupiedTables != null ? dashboard.occupiedTables : 0)
                .activeOrders(dashboard.activeOrders != null ? dashboard.activeOrders : 0)
                .closedOrdersToday(
                        dashboard.closedOrdersToday != null ? dashboard.closedOrdersToday : 0)
                .salesToday(dashboard.salesToday != null ? dashboard.salesToday : BigDecimal.ZERO)
                .totalTables(totalTables != null ? totalTables : 0)
                .lowStockProducts(lowStockProducts != null ? lowStockProducts : 0)
                .build();
    }

    /**
     * T9.2: Get sales report with date range filters, revenue by payment method, top products,
     * period comparison.
     */
    @Transactional(readOnly = true)
    public SalesSummaryDto getSalesReport(LocalDate startDate, LocalDate endDate) {
        String restaurantId = TenantContext.getRestaurantId();

        // Get sales data by date and payment method
        List<ReportRepository.SalesReportRow> salesRows =
                reportRepository.getSalesReport(restaurantId, startDate, endDate);

        // Get summary
        ReportRepository.SalesSummaryRow summary =
                reportRepository.getSalesSummary(restaurantId, startDate, endDate);

        // Get top products
        List<ReportRepository.TopProductRow> topProducts =
                reportRepository.getTopProducts(restaurantId, startDate, endDate, 10);

        // Calculate period comparison (same duration previous period)
        PeriodComparisonDto periodComparison =
                calculatePeriodComparison(restaurantId, startDate, endDate);

        // Calculate average ticket
        BigDecimal averageTicket =
                summary.totalInvoices > 0
                        ? summary.totalRevenue.divide(
                                BigDecimal.valueOf(summary.totalInvoices), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

        return SalesSummaryDto.builder()
                .totalRevenue(summary.totalRevenue != null ? summary.totalRevenue : BigDecimal.ZERO)
                .totalInvoices(summary.totalInvoices != null ? summary.totalInvoices : 0L)
                .averageTicket(averageTicket)
                .topProducts(mapTopProducts(topProducts))
                .periodComparison(periodComparison)
                .build();
    }

    /** T9.3: Get product report with sales by product, margin analysis, stock turnover. */
    @Transactional(readOnly = true)
    public List<ProductReportDto> getProductReport(LocalDate startDate, LocalDate endDate) {
        String restaurantId = TenantContext.getRestaurantId();

        List<ReportRepository.ProductReportRow> productRows =
                reportRepository.getProductReport(restaurantId, startDate, endDate);

        return productRows.stream().map(this::mapProductReport).collect(Collectors.toList());
    }

    /**
     * T9.4: Get financial report with income/expense breakdown, cash register reconciliation,
     * invoice summary. Requires judgment double (💰).
     */
    @Transactional(readOnly = true)
    public FinancialReportDto getFinancialReport(LocalDate startDate, LocalDate endDate) {
        String restaurantId = TenantContext.getRestaurantId();

        // Get income and expense breakdown
        List<ReportRepository.TransactionSummaryRow> income =
                reportRepository.getIncomeBreakdown(restaurantId, startDate, endDate);
        List<ReportRepository.TransactionSummaryRow> expenses =
                reportRepository.getExpenseBreakdown(restaurantId, startDate, endDate);

        // Get invoice summary
        ReportRepository.InvoiceSummaryRow invoiceSummary =
                reportRepository.getInvoiceSummary(restaurantId, startDate, endDate);

        // Calculate net cash flow
        BigDecimal totalIncome =
                income.stream()
                        .map(row -> row.totalAmount != null ? row.totalAmount : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses =
                expenses.stream()
                        .map(row -> row.totalAmount != null ? row.totalAmount : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netCashFlow = totalIncome.subtract(totalExpenses);

        // Get cash register summary for today
        ReportRepository.CashRegisterSummaryRow cashRegisterSummary =
                reportRepository.getCashRegisterSummary(restaurantId, LocalDate.now());

        return FinancialReportDto.builder()
                .transactionDate(startDate)
                .income(mapTransactionSummaries(income))
                .expenses(mapTransactionSummaries(expenses))
                .netCashFlow(netCashFlow)
                .cashRegisterSummary(
                        mapCashRegisterSummary(cashRegisterSummary, totalIncome, totalExpenses))
                .invoiceSummary(mapInvoiceSummary(invoiceSummary))
                .build();
    }

    /** T9.5: Get footfall report with peak hours and traffic patterns. */
    @Transactional(readOnly = true)
    public FootfallReportDto getFootfallReport(LocalDate date) {
        String restaurantId = TenantContext.getRestaurantId();

        List<ReportRepository.FootfallRow> footfallRows =
                reportRepository.getFootfallByHour(restaurantId, date);

        // Map to DTOs
        List<HourlyTrafficDto> hourlyTraffic =
                footfallRows.stream().map(this::mapHourlyTraffic).collect(Collectors.toList());

        // Calculate peak hours
        PeakHoursDto peakHours = calculatePeakHours(footfallRows);

        return FootfallReportDto.builder()
                .orderDate(date)
                .hourlyTraffic(hourlyTraffic)
                .peakHours(peakHours)
                .build();
    }

    /** T9.5: Get staff planning report with workload analysis. */
    @Transactional(readOnly = true)
    public StaffPlanningReportDto getStaffPlanningReport(LocalDate date) {
        String restaurantId = TenantContext.getRestaurantId();

        List<ReportRepository.WorkloadRow> workloadRows =
                reportRepository.getHourlyWorkload(restaurantId, date);

        // Map to DTOs with workload levels and staff recommendations
        List<HourlyWorkloadDto> hourlyWorkload =
                workloadRows.stream().map(this::mapHourlyWorkload).collect(Collectors.toList());

        // Calculate staff recommendation
        StaffRecommendationDto recommendation = calculateStaffRecommendation(hourlyWorkload);

        return StaffPlanningReportDto.builder()
                .date(date)
                .hourlyWorkload(hourlyWorkload)
                .staffRecommendation(recommendation)
                .build();
    }

    // Helper methods for mapping and calculations

    private PeriodComparisonDto calculatePeriodComparison(
            String restaurantId, LocalDate currentStart, LocalDate currentEnd) {
        // Calculate previous period (same duration)
        long days = java.time.temporal.ChronoUnit.DAYS.between(currentStart, currentEnd);
        LocalDate previousEnd = currentStart.minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(days);

        ReportRepository.SalesSummaryRow currentSummary =
                reportRepository.getSalesSummary(restaurantId, currentStart, currentEnd);
        ReportRepository.SalesSummaryRow previousSummary =
                reportRepository.getSalesSummary(restaurantId, previousStart, previousEnd);

        BigDecimal currentRevenue =
                currentSummary.totalRevenue != null ? currentSummary.totalRevenue : BigDecimal.ZERO;
        BigDecimal previousRevenue =
                previousSummary.totalRevenue != null
                        ? previousSummary.totalRevenue
                        : BigDecimal.ZERO;

        BigDecimal growth = currentRevenue.subtract(previousRevenue);
        Double growthPercentage =
                previousRevenue.compareTo(BigDecimal.ZERO) > 0
                        ? growth.divide(previousRevenue, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .doubleValue()
                        : null;

        return PeriodComparisonDto.builder()
                .currentRevenue(currentRevenue)
                .previousRevenue(previousRevenue)
                .growth(growth)
                .growthPercentage(growthPercentage)
                .build();
    }

    private List<TopProductDto> mapTopProducts(List<ReportRepository.TopProductRow> rows) {
        return rows.stream()
                .map(
                        row ->
                                TopProductDto.builder()
                                        .productId(row.productId)
                                        .productName(row.productName)
                                        .totalQuantity(row.totalQuantity)
                                        .totalRevenue(row.totalRevenue)
                                        .orderCount(row.orderCount)
                                        .build())
                .collect(Collectors.toList());
    }

    private ProductReportDto mapProductReport(ReportRepository.ProductReportRow row) {
        // Calculate margin
        BigDecimal totalCost =
                row.unitCost != null
                        ? row.unitCost.multiply(
                                BigDecimal.valueOf(
                                        row.totalQuantity != null ? row.totalQuantity : 0L))
                        : BigDecimal.ZERO;

        BigDecimal totalMargin =
                (row.totalRevenue != null ? row.totalRevenue : BigDecimal.ZERO).subtract(totalCost);
        Double marginPercentage =
                row.totalRevenue != null && row.totalRevenue.compareTo(BigDecimal.ZERO) > 0
                        ? totalMargin
                                .divide(row.totalRevenue, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .doubleValue()
                        : null;

        // Calculate stock turnover days (simplified)
        Integer stockTurnoverDays = null;
        if (row.currentStock != null
                && row.currentStock > 0
                && row.totalQuantity != null
                && row.totalQuantity > 0) {
            // Estimate: if current stock = X days of sales at current rate
            long daysBetween =
                    java.time.temporal.ChronoUnit.DAYS.between(
                            LocalDate.now().minusDays(30), LocalDate.now());
            if (daysBetween > 0) {
                double dailySales = (double) row.totalQuantity / daysBetween;
                if (dailySales > 0) {
                    stockTurnoverDays = (int) Math.round(row.currentStock / dailySales);
                }
            }
        }

        return ProductReportDto.builder()
                .productId(row.productId)
                .productName(row.productName)
                .orderCount(row.orderCount != null ? row.orderCount : 0L)
                .totalQuantity(row.totalQuantity != null ? row.totalQuantity : 0L)
                .totalRevenue(row.totalRevenue != null ? row.totalRevenue : BigDecimal.ZERO)
                .unitCost(row.unitCost)
                .totalMargin(totalMargin)
                .marginPercentage(marginPercentage)
                .currentStock(row.currentStock)
                .stockTurnoverDays(stockTurnoverDays)
                .build();
    }

    private List<TransactionSummaryDto> mapTransactionSummaries(
            List<ReportRepository.TransactionSummaryRow> rows) {
        return rows.stream()
                .map(
                        row ->
                                TransactionSummaryDto.builder()
                                        .paymentMethod(row.paymentMethod)
                                        .transactionCount(row.transactionCount)
                                        .totalAmount(row.totalAmount)
                                        .build())
                .collect(Collectors.toList());
    }

    private CashRegisterSummaryDto mapCashRegisterSummary(
            ReportRepository.CashRegisterSummaryRow row,
            BigDecimal totalIncome,
            BigDecimal totalExpenses) {
        BigDecimal expectedBalance =
                (row.totalOpeningBalance != null ? row.totalOpeningBalance : BigDecimal.ZERO)
                        .add(totalIncome != null ? totalIncome : BigDecimal.ZERO)
                        .subtract(totalExpenses != null ? totalExpenses : BigDecimal.ZERO);

        BigDecimal actualBalance =
                row.totalClosingBalance != null ? row.totalClosingBalance : BigDecimal.ZERO;
        BigDecimal discrepancy = actualBalance.subtract(expectedBalance);

        return CashRegisterSummaryDto.builder()
                .openRegisters(row.openRegisters != null ? row.openRegisters : 0)
                .closedRegisters(row.closedRegisters != null ? row.closedRegisters : 0)
                .totalOpeningBalance(
                        row.totalOpeningBalance != null ? row.totalOpeningBalance : BigDecimal.ZERO)
                .totalClosingBalance(
                        row.totalClosingBalance != null ? row.totalClosingBalance : BigDecimal.ZERO)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .expectedBalance(expectedBalance)
                .actualBalance(actualBalance)
                .discrepancy(discrepancy)
                .build();
    }

    private InvoiceSummaryDto mapInvoiceSummary(ReportRepository.InvoiceSummaryRow row) {
        Double paymentRate =
                row.totalInvoices != null && row.totalInvoices > 0
                        ? (row.paidInvoices != null ? row.paidInvoices : 0L)
                                * 100.0
                                / row.totalInvoices
                        : null;

        return InvoiceSummaryDto.builder()
                .totalInvoices(row.totalInvoices != null ? row.totalInvoices : 0L)
                .paidInvoices(row.paidInvoices != null ? row.paidInvoices : 0L)
                .unpaidInvoices(row.unpaidInvoices != null ? row.unpaidInvoices : 0L)
                .totalInvoiced(row.totalInvoiced != null ? row.totalInvoiced : BigDecimal.ZERO)
                .totalPaid(row.totalPaid != null ? row.totalPaid : BigDecimal.ZERO)
                .totalPending(row.totalPending != null ? row.totalPending : BigDecimal.ZERO)
                .paymentRate(paymentRate)
                .build();
    }

    private HourlyTrafficDto mapHourlyTraffic(ReportRepository.FootfallRow row) {
        Double averagePeoplePerOrder =
                row.orderCount != null && row.orderCount > 0
                        ? (row.totalPeople != null ? row.totalPeople : 0) * 1.0 / row.orderCount
                        : null;

        return HourlyTrafficDto.builder()
                .hour(row.hour)
                .orderCount(row.orderCount)
                .totalPeople(row.totalPeople)
                .averagePeoplePerOrder(averagePeoplePerOrder)
                .build();
    }

    private PeakHoursDto calculatePeakHours(List<ReportRepository.FootfallRow> footfallRows) {
        if (footfallRows.isEmpty()) {
            return new PeakHoursDto();
        }

        // Find peak order hours (top 3 hours by order count)
        List<Integer> peakOrderHours =
                footfallRows.stream()
                        .filter(row -> row.orderCount != null && row.orderCount > 0)
                        .sorted((a, b) -> Long.compare(b.orderCount, a.orderCount))
                        .limit(3)
                        .map(row -> row.hour)
                        .collect(Collectors.toList());

        // Find peak people hours (top 3 hours by total people)
        List<Integer> peakPeopleHours =
                footfallRows.stream()
                        .filter(row -> row.totalPeople != null && row.totalPeople > 0)
                        .sorted((a, b) -> Integer.compare(b.totalPeople, a.totalPeople))
                        .limit(3)
                        .map(row -> row.hour)
                        .collect(Collectors.toList());

        // Calculate totals and averages
        Long totalOrders =
                footfallRows.stream()
                        .map(row -> row.orderCount != null ? row.orderCount : 0L)
                        .reduce(0L, Long::sum);

        Integer totalPeople =
                footfallRows.stream()
                        .map(row -> row.totalPeople != null ? row.totalPeople : 0)
                        .reduce(0, Integer::sum);

        Double averageOrdersPerHour =
                footfallRows.stream()
                        .filter(row -> row.orderCount != null && row.orderCount > 0)
                        .mapToDouble(row -> row.orderCount)
                        .average()
                        .orElse(0.0);

        Double averagePeoplePerHour =
                footfallRows.stream()
                        .filter(row -> row.totalPeople != null && row.totalPeople > 0)
                        .mapToDouble(row -> row.totalPeople)
                        .average()
                        .orElse(0.0);

        return PeakHoursDto.builder()
                .peakOrderHours(peakOrderHours)
                .peakPeopleHours(peakPeopleHours)
                .totalOrders(totalOrders.intValue())
                .totalPeople(totalPeople)
                .averageOrdersPerHour(averageOrdersPerHour)
                .averagePeoplePerHour(averagePeoplePerHour)
                .build();
    }

    private HourlyWorkloadDto mapHourlyWorkload(ReportRepository.WorkloadRow row) {
        // Determine workload level based on active orders
        String workloadLevel;
        Integer recommendedStaff;

        if (row.activeOrders == null || row.activeOrders == 0) {
            workloadLevel = "LOW";
            recommendedStaff = 1;
        } else if (row.activeOrders < 5) {
            workloadLevel = "LOW";
            recommendedStaff = 1;
        } else if (row.activeOrders < 10) {
            workloadLevel = "MEDIUM";
            recommendedStaff = 2;
        } else if (row.activeOrders < 15) {
            workloadLevel = "HIGH";
            recommendedStaff = 3;
        } else {
            workloadLevel = "CRITICAL";
            recommendedStaff = 4;
        }

        return HourlyWorkloadDto.builder()
                .hour(row.hour)
                .activeOrders(row.activeOrders)
                .totalPeople(row.totalPeople)
                .workloadLevel(workloadLevel)
                .recommendedStaff(recommendedStaff)
                .build();
    }

    private StaffRecommendationDto calculateStaffRecommendation(
            List<HourlyWorkloadDto> hourlyWorkload) {
        if (hourlyWorkload.isEmpty()) {
            return new StaffRecommendationDto();
        }

        // Find minimum, recommended, and peak staff
        Integer minimumStaff =
                hourlyWorkload.stream()
                        .mapToInt(HourlyWorkloadDto::getRecommendedStaff)
                        .min()
                        .orElse(1);

        Integer peakStaff =
                hourlyWorkload.stream()
                        .mapToInt(HourlyWorkloadDto::getRecommendedStaff)
                        .max()
                        .orElse(1);

        Integer recommendedStaff = (minimumStaff + peakStaff) / 2;

        // Find peak hours
        List<String> peakHours =
                hourlyWorkload.stream()
                        .filter(
                                dto ->
                                        "HIGH".equals(dto.getWorkloadLevel())
                                                || "CRITICAL".equals(dto.getWorkloadLevel()))
                        .map(dto -> String.format("%02d:00", dto.getHour()))
                        .collect(Collectors.toList());

        // Generate rationale
        String rationale =
                String.format(
                        "Based on hourly workload analysis: minimum %d staff during quiet hours,"
                            + " peak %d staff during busy hours (%s). Average recommendation: %d"
                            + " staff.",
                        minimumStaff, peakStaff, String.join(", ", peakHours), recommendedStaff);

        return StaffRecommendationDto.builder()
                .minimumStaff(minimumStaff)
                .recommendedStaff(recommendedStaff)
                .peakStaff(peakStaff)
                .peakHours(peakHours)
                .rationale(rationale)
                .build();
    }
}
