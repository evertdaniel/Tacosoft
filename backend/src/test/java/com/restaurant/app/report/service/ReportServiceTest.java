package com.restaurant.app.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.restaurant.app.report.dto.*;
import com.restaurant.app.report.repository.ReportRepository;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link ReportService}. */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReportRepository reportRepository;

    @InjectMocks private ReportService reportService;

    private final String restaurantId = "restaurant-1";

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getDashboard_WithValues_ReturnsDashboard() {
        ReportRepository.DashboardRow row = new ReportRepository.DashboardRow();
        row.occupiedTables = 3;
        row.activeOrders = 5;
        row.closedOrdersToday = 12;
        row.salesToday = BigDecimal.valueOf(1250.50);

        when(reportRepository.getDashboard(restaurantId)).thenReturn(row);
        when(reportRepository.getTotalTables(restaurantId)).thenReturn(10);
        when(reportRepository.getLowStockCount(restaurantId)).thenReturn(2);

        DashboardReportDto result = reportService.getDashboard();

        assertThat(result.getOccupiedTables()).isEqualTo(3);
        assertThat(result.getActiveOrders()).isEqualTo(5);
        assertThat(result.getClosedOrdersToday()).isEqualTo(12);
        assertThat(result.getSalesToday()).isEqualByComparingTo(BigDecimal.valueOf(1250.50));
        assertThat(result.getTotalTables()).isEqualTo(10);
        assertThat(result.getLowStockProducts()).isEqualTo(2);
    }

    @Test
    void getDashboard_WithNullValues_ReturnsZeros() {
        when(reportRepository.getDashboard(restaurantId))
                .thenReturn(new ReportRepository.DashboardRow());
        when(reportRepository.getTotalTables(restaurantId)).thenReturn(null);
        when(reportRepository.getLowStockCount(restaurantId)).thenReturn(null);

        DashboardReportDto result = reportService.getDashboard();

        assertThat(result.getOccupiedTables()).isZero();
        assertThat(result.getActiveOrders()).isZero();
        assertThat(result.getClosedOrdersToday()).isZero();
        assertThat(result.getSalesToday()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalTables()).isZero();
        assertThat(result.getLowStockProducts()).isZero();
    }

    @Test
    void getSalesReport_WithRevenue_ReturnsSummary() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 7);

        ReportRepository.SalesSummaryRow summary = new ReportRepository.SalesSummaryRow();
        summary.totalInvoices = 10L;
        summary.totalRevenue = BigDecimal.valueOf(1000.00);

        ReportRepository.TopProductRow top = new ReportRepository.TopProductRow();
        top.productId = "p1";
        top.productName = "Taco";
        top.totalQuantity = 20L;
        top.totalRevenue = BigDecimal.valueOf(500.00);
        top.orderCount = 8L;

        ReportRepository.SalesSummaryRow previous = new ReportRepository.SalesSummaryRow();
        previous.totalInvoices = 8L;
        previous.totalRevenue = BigDecimal.valueOf(800.00);

        when(reportRepository.getSalesSummary(restaurantId, start, end)).thenReturn(summary);
        when(reportRepository.getTopProducts(restaurantId, start, end, 10))
                .thenReturn(List.of(top));
        when(reportRepository.getSalesReport(restaurantId, start, end)).thenReturn(List.of());
        when(reportRepository.getSalesSummary(
                        restaurantId, LocalDate.of(2026, 5, 25), LocalDate.of(2026, 5, 31)))
                .thenReturn(previous);

        SalesSummaryDto result = reportService.getSalesReport(start, end);

        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(result.getTotalInvoices()).isEqualTo(10L);
        assertThat(result.getAverageTicket()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(result.getTopProducts()).hasSize(1);
        assertThat(result.getPeriodComparison().getCurrentRevenue())
                .isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(result.getPeriodComparison().getPreviousRevenue())
                .isEqualByComparingTo(BigDecimal.valueOf(800.00));
        assertThat(result.getPeriodComparison().getGrowthPercentage()).isPositive();
    }

    @Test
    void getSalesReport_WithZeroInvoices_ReturnsZeroAverageTicket() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 7);

        ReportRepository.SalesSummaryRow summary = new ReportRepository.SalesSummaryRow();
        summary.totalInvoices = 0L;
        summary.totalRevenue = BigDecimal.ZERO;

        ReportRepository.SalesSummaryRow previous = new ReportRepository.SalesSummaryRow();
        previous.totalInvoices = 0L;
        previous.totalRevenue = BigDecimal.ZERO;

        when(reportRepository.getSalesSummary(restaurantId, start, end)).thenReturn(summary);
        when(reportRepository.getTopProducts(restaurantId, start, end, 10)).thenReturn(List.of());
        when(reportRepository.getSalesSummary(
                        restaurantId, LocalDate.of(2026, 5, 25), LocalDate.of(2026, 5, 31)))
                .thenReturn(previous);

        SalesSummaryDto result = reportService.getSalesReport(start, end);

        assertThat(result.getAverageTicket()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTopProducts()).isEmpty();
        assertThat(result.getPeriodComparison().getGrowthPercentage()).isNull();
    }

    @Test
    void getProductReport_WithStock_ReturnsMarginAndTurnover() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);

        ReportRepository.ProductReportRow row = new ReportRepository.ProductReportRow();
        row.productId = "p1";
        row.productName = "Taco";
        row.unitCost = BigDecimal.valueOf(5.00);
        row.currentStock = 100;
        row.orderCount = 10L;
        row.totalQuantity = 50L;
        row.totalRevenue = BigDecimal.valueOf(500.00);

        when(reportRepository.getProductReport(restaurantId, start, end)).thenReturn(List.of(row));

        List<ProductReportDto> result = reportService.getProductReport(start, end);

        assertThat(result).hasSize(1);
        ProductReportDto dto = result.get(0);
        assertThat(dto.getTotalMargin()).isEqualByComparingTo(BigDecimal.valueOf(250.00));
        assertThat(dto.getMarginPercentage()).isPositive();
        assertThat(dto.getStockTurnoverDays()).isPositive();
    }

    @Test
    void getProductReport_WithoutRevenue_ReturnsNullMargin() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);

        ReportRepository.ProductReportRow row = new ReportRepository.ProductReportRow();
        row.productId = "p1";
        row.productName = "Taco";
        row.unitCost = BigDecimal.valueOf(5.00);
        row.currentStock = 0;
        row.totalRevenue = BigDecimal.ZERO;

        when(reportRepository.getProductReport(restaurantId, start, end)).thenReturn(List.of(row));

        List<ProductReportDto> result = reportService.getProductReport(start, end);

        assertThat(result.get(0).getMarginPercentage()).isNull();
        assertThat(result.get(0).getStockTurnoverDays()).isNull();
    }

    @Test
    void getFinancialReport_WithIncomeAndExpenses_ReturnsNetCashFlow() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 7);

        ReportRepository.TransactionSummaryRow income =
                new ReportRepository.TransactionSummaryRow();
        income.paymentMethod = "CASH";
        income.transactionCount = 5L;
        income.totalAmount = BigDecimal.valueOf(500.00);

        ReportRepository.TransactionSummaryRow expense =
                new ReportRepository.TransactionSummaryRow();
        expense.paymentMethod = "CASH";
        expense.transactionCount = 2L;
        expense.totalAmount = BigDecimal.valueOf(100.00);

        ReportRepository.InvoiceSummaryRow invoice = new ReportRepository.InvoiceSummaryRow();
        invoice.totalInvoices = 10L;
        invoice.paidInvoices = 7L;
        invoice.unpaidInvoices = 3L;
        invoice.totalInvoiced = BigDecimal.valueOf(1000.00);
        invoice.totalPaid = BigDecimal.valueOf(700.00);
        invoice.totalPending = BigDecimal.valueOf(300.00);

        ReportRepository.CashRegisterSummaryRow cash =
                new ReportRepository.CashRegisterSummaryRow();
        cash.openRegisters = 1;
        cash.closedRegisters = 0;
        cash.totalOpeningBalance = BigDecimal.valueOf(200.00);
        cash.totalClosingBalance = BigDecimal.valueOf(600.00);

        when(reportRepository.getIncomeBreakdown(restaurantId, start, end))
                .thenReturn(List.of(income));
        when(reportRepository.getExpenseBreakdown(restaurantId, start, end))
                .thenReturn(List.of(expense));
        when(reportRepository.getInvoiceSummary(restaurantId, start, end)).thenReturn(invoice);
        when(reportRepository.getCashRegisterSummary(restaurantId, LocalDate.now()))
                .thenReturn(cash);

        FinancialReportDto result = reportService.getFinancialReport(start, end);

        assertThat(result.getNetCashFlow()).isEqualByComparingTo(BigDecimal.valueOf(400.00));
        assertThat(result.getIncome()).hasSize(1);
        assertThat(result.getExpenses()).hasSize(1);
        assertThat(result.getInvoiceSummary().getPaymentRate()).isEqualTo(70.0);
        assertThat(result.getCashRegisterSummary().getExpectedBalance())
                .isEqualByComparingTo(BigDecimal.valueOf(600.00));
        assertThat(result.getCashRegisterSummary().getDiscrepancy())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getFootfallReport_WithData_ReturnsPeakHours() {
        LocalDate date = LocalDate.of(2026, 6, 15);

        ReportRepository.FootfallRow row1 = new ReportRepository.FootfallRow();
        row1.hour = 12;
        row1.orderCount = 15L;
        row1.totalPeople = 45;

        ReportRepository.FootfallRow row2 = new ReportRepository.FootfallRow();
        row2.hour = 13;
        row2.orderCount = 8L;
        row2.totalPeople = 24;

        when(reportRepository.getFootfallByHour(restaurantId, date))
                .thenReturn(List.of(row1, row2));

        FootfallReportDto result = reportService.getFootfallReport(date);

        assertThat(result.getHourlyTraffic()).hasSize(2);
        assertThat(result.getPeakHours().getPeakOrderHours()).contains(12);
        assertThat(result.getPeakHours().getTotalOrders()).isEqualTo(23);
        assertThat(result.getPeakHours().getTotalPeople()).isEqualTo(69);
    }

    @Test
    void getFootfallReport_WithEmptyData_ReturnsEmptyPeakHours() {
        LocalDate date = LocalDate.of(2026, 6, 15);

        when(reportRepository.getFootfallByHour(restaurantId, date)).thenReturn(List.of());

        FootfallReportDto result = reportService.getFootfallReport(date);

        assertThat(result.getHourlyTraffic()).isEmpty();
        assertThat(result.getPeakHours()).isNotNull();
        assertThat(result.getPeakHours().getPeakOrderHours()).isNullOrEmpty();
    }

    @Test
    void getStaffPlanningReport_WithWorkload_ReturnsRecommendations() {
        LocalDate date = LocalDate.of(2026, 6, 15);

        ReportRepository.WorkloadRow low = new ReportRepository.WorkloadRow();
        low.hour = 10;
        low.activeOrders = 2L;
        low.totalPeople = 6;

        ReportRepository.WorkloadRow critical = new ReportRepository.WorkloadRow();
        critical.hour = 12;
        critical.activeOrders = 20L;
        critical.totalPeople = 60;

        when(reportRepository.getHourlyWorkload(restaurantId, date))
                .thenReturn(List.of(low, critical));

        StaffPlanningReportDto result = reportService.getStaffPlanningReport(date);

        assertThat(result.getHourlyWorkload()).hasSize(2);
        assertThat(result.getStaffRecommendation().getMinimumStaff()).isEqualTo(1);
        assertThat(result.getStaffRecommendation().getPeakStaff()).isEqualTo(4);
        assertThat(result.getStaffRecommendation().getPeakHours()).contains("12:00");
        assertThat(result.getStaffRecommendation().getRationale()).contains("peak 4 staff");
    }

    @Test
    void getStaffPlanningReport_WithEmptyWorkload_ReturnsEmptyRecommendation() {
        LocalDate date = LocalDate.of(2026, 6, 15);

        when(reportRepository.getHourlyWorkload(restaurantId, date)).thenReturn(List.of());

        StaffPlanningReportDto result = reportService.getStaffPlanningReport(date);

        assertThat(result.getHourlyWorkload()).isEmpty();
        assertThat(result.getStaffRecommendation().getMinimumStaff()).isNull();
    }
}
