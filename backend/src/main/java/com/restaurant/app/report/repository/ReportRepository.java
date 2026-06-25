package com.restaurant.app.report.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Repository for report queries using native SQL and database views. Implements SPEC-REPORT-001.
 */
@Repository
public class ReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReportRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /** Get dashboard summary for current restaurant. */
    public DashboardRow getDashboard(String restaurantId) {
        String sql =
                """
SELECT
    (SELECT COUNT(*) FROM restaurant_table WHERE restaurant_id = ? AND status = 'OCCUPIED') AS occupied_tables,
    (SELECT COUNT(*) FROM `order` WHERE restaurant_id = ? AND status IN ('PENDING', 'IN_PROGRESS')) AS active_orders,
    (SELECT COUNT(*) FROM `order` WHERE restaurant_id = ? AND status = 'CLOSED' AND DATE(created_at) = CURDATE()) AS closed_orders_today,
    (SELECT COALESCE(SUM(total), 0) FROM invoice WHERE restaurant_id = ? AND DATE(created_at) = CURDATE()) AS sales_today
""";

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> {
                    DashboardRow row = new DashboardRow();
                    row.occupiedTables = rs.getInt("occupied_tables");
                    row.activeOrders = rs.getInt("active_orders");
                    row.closedOrdersToday = rs.getInt("closed_orders_today");
                    row.salesToday = rs.getBigDecimal("sales_today");
                    return row;
                },
                restaurantId,
                restaurantId,
                restaurantId,
                restaurantId);
    }

    /** Get total tables count for occupancy calculation. */
    public Integer getTotalTables(String restaurantId) {
        String sql = "SELECT COUNT(*) FROM restaurant_table WHERE restaurant_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, restaurantId);
    }

    /** Get low stock products count (stock < 10 where manage_stock = true). */
    public Integer getLowStockCount(String restaurantId) {
        String sql =
                "SELECT COUNT(*) FROM product WHERE restaurant_id = ? AND manage_stock = true AND"
                        + " stock < 10 AND status = 'AVAILABLE'";
        return jdbcTemplate.queryForObject(sql, Integer.class, restaurantId);
    }

    /** Get sales report data with date range filters. */
    public List<SalesReportRow> getSalesReport(
            String restaurantId, LocalDate startDate, LocalDate endDate) {
        String sql =
                """
                SELECT
                    DATE(i.created_at) AS saleDate,
                    i.payment_method AS paymentMethod,
                    COUNT(*) AS invoiceCount,
                    SUM(i.subtotal) AS totalSubtotal,
                    SUM(i.tax) AS totalTax,
                    SUM(i.total) AS totalRevenue
                FROM invoice i
                WHERE i.restaurant_id = ?
                    AND DATE(i.created_at) BETWEEN ? AND ?
                GROUP BY DATE(i.created_at), i.payment_method
                ORDER BY saleDate DESC, totalRevenue DESC
                """;

        return jdbcTemplate.query(
                sql, new SalesReportRowMapper(), restaurantId, startDate, endDate);
    }

    /** Get total revenue and invoice count for period. */
    public SalesSummaryRow getSalesSummary(
            String restaurantId, LocalDate startDate, LocalDate endDate) {
        String sql =
                """
                SELECT
                    COUNT(*) AS totalInvoices,
                    COALESCE(SUM(total), 0) AS totalRevenue
                FROM invoice
                WHERE restaurant_id = ?
                    AND DATE(created_at) BETWEEN ? AND ?
                """;

        return jdbcTemplate.queryForObject(
                sql, new SalesSummaryRowMapper(), restaurantId, startDate, endDate);
    }

    /** Get top products by revenue for period. */
    public List<TopProductRow> getTopProducts(
            String restaurantId, LocalDate startDate, LocalDate endDate, int limit) {
        String sql =
                """
                SELECT
                    p.id AS productId,
                    p.name AS productName,
                    SUM(od.quantity) AS totalQuantity,
                    SUM(od.amount) AS totalRevenue,
                    COUNT(DISTINCT od.order_id) AS orderCount
                FROM product p
                JOIN order_detail od ON od.product_id = p.id
                JOIN `order` o ON o.id = od.order_id
                WHERE p.restaurant_id = ?
                    AND DATE(o.created_at) BETWEEN ? AND ?
                GROUP BY p.id, p.name
                ORDER BY totalRevenue DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql, new TopProductRowMapper(), restaurantId, startDate, endDate, limit);
    }

    /** Get product report with margin analysis. */
    public List<ProductReportRow> getProductReport(
            String restaurantId, LocalDate startDate, LocalDate endDate) {
        String sql =
                """
                SELECT
                    p.id AS productId,
                    p.name AS productName,
                    p.unit_cost AS unitCost,
                    p.stock AS currentStock,
                    COUNT(DISTINCT od.id) AS orderCount,
                    SUM(od.quantity) AS totalQuantity,
                    SUM(od.amount) AS totalRevenue
                FROM product p
                LEFT JOIN order_detail od ON od.product_id = p.id
                    AND EXISTS (
                        SELECT 1 FROM `order` o
                        WHERE o.id = od.order_id
                        AND o.restaurant_id = ?
                        AND DATE(o.created_at) BETWEEN ? AND ?
                    )
                WHERE p.restaurant_id = ?
                GROUP BY p.id, p.name, p.unit_cost, p.stock
                ORDER BY totalRevenue DESC
                """;

        return jdbcTemplate.query(
                sql, new ProductReportRowMapper(), restaurantId, startDate, endDate, restaurantId);
    }

    /** Get financial report - income breakdown. */
    public List<TransactionSummaryRow> getIncomeBreakdown(
            String restaurantId, LocalDate startDate, LocalDate endDate) {
        String sql =
                """
                SELECT
                    payment_method AS paymentMethod,
                    COUNT(*) AS transactionCount,
                    SUM(amount) AS totalAmount
                FROM transaction t
                JOIN cash_register cr ON cr.id = t.cash_register_id
                WHERE cr.restaurant_id = ?
                    AND t.type = 'INCOME'
                    AND DATE(t.created_at) BETWEEN ? AND ?
                GROUP BY payment_method
                ORDER BY totalAmount DESC
                """;

        return jdbcTemplate.query(
                sql, new TransactionSummaryRowMapper(), restaurantId, startDate, endDate);
    }

    /** Get financial report - expense breakdown. */
    public List<TransactionSummaryRow> getExpenseBreakdown(
            String restaurantId, LocalDate startDate, LocalDate endDate) {
        String sql =
                """
                SELECT
                    payment_method AS paymentMethod,
                    COUNT(*) AS transactionCount,
                    SUM(amount) AS totalAmount
                FROM transaction t
                JOIN cash_register cr ON cr.id = t.cash_register_id
                WHERE cr.restaurant_id = ?
                    AND t.type = 'EXPENSE'
                    AND DATE(t.created_at) BETWEEN ? AND ?
                GROUP BY payment_method
                ORDER BY totalAmount DESC
                """;

        return jdbcTemplate.query(
                sql, new TransactionSummaryRowMapper(), restaurantId, startDate, endDate);
    }

    /** Get invoice summary for financial report. */
    public InvoiceSummaryRow getInvoiceSummary(
            String restaurantId, LocalDate startDate, LocalDate endDate) {
        String sql =
                """
SELECT
    COUNT(*) AS totalInvoices,
    SUM(CASE WHEN is_paid = true THEN 1 ELSE 0 END) AS paidInvoices,
    SUM(CASE WHEN is_paid = false THEN 1 ELSE 0 END) AS unpaidInvoices,
    COALESCE(SUM(total), 0) AS totalInvoiced,
    COALESCE(SUM(CASE WHEN is_paid = true THEN total ELSE 0 END), 0) AS totalPaid,
    COALESCE(SUM(CASE WHEN is_paid = false THEN total ELSE 0 END), 0) AS totalPending
FROM invoice
WHERE restaurant_id = ?
    AND DATE(created_at) BETWEEN ? AND ?
""";

        return jdbcTemplate.queryForObject(
                sql, new InvoiceSummaryRowMapper(), restaurantId, startDate, endDate);
    }

    /** Get cash register summary for financial report. */
    public CashRegisterSummaryRow getCashRegisterSummary(String restaurantId, LocalDate date) {
        String sql =
                """
SELECT
    SUM(CASE WHEN status = 'OPEN' THEN 1 ELSE 0 END) AS openRegisters,
    SUM(CASE WHEN status = 'CLOSED' THEN 1 ELSE 0 END) AS closedRegisters,
    COALESCE(SUM(CASE WHEN status = 'OPEN' THEN opening_amount ELSE 0 END), 0) AS totalOpeningBalance,
    COALESCE(SUM(CASE WHEN status = 'CLOSED' THEN closing_amount ELSE 0 END), 0) AS totalClosingBalance
FROM cash_register
WHERE restaurant_id = ?
    AND DATE(created_at) = ?
""";

        return jdbcTemplate.queryForObject(
                sql, new CashRegisterSummaryRowMapper(), restaurantId, date);
    }

    /** Get footfall data by hour for specific date. */
    public List<FootfallRow> getFootfallByHour(String restaurantId, LocalDate date) {
        String sql =
                """
                SELECT
                    HOUR(o.created_at) AS hour,
                    COUNT(*) AS orderCount,
                    SUM(o.people) AS totalPeople
                FROM `order` o
                WHERE o.restaurant_id = ?
                    AND DATE(o.created_at) = ?
                GROUP BY HOUR(o.created_at)
                ORDER BY hour
                """;

        return jdbcTemplate.query(sql, new FootfallRowMapper(), restaurantId, date);
    }

    /** Get footfall data for date range (for trend analysis). */
    public List<FootfallRow> getFootfallByDateRange(
            String restaurantId, LocalDate startDate, LocalDate endDate) {
        String sql =
                """
                SELECT
                    HOUR(o.created_at) AS hour,
                    COUNT(*) AS orderCount,
                    SUM(o.people) AS totalPeople
                FROM `order` o
                WHERE o.restaurant_id = ?
                    AND DATE(o.created_at) BETWEEN ? AND ?
                GROUP BY HOUR(o.created_at)
                ORDER BY hour
                """;

        return jdbcTemplate.query(sql, new FootfallRowMapper(), restaurantId, startDate, endDate);
    }

    /** Get staff planning data - hourly workload. */
    public List<WorkloadRow> getHourlyWorkload(String restaurantId, LocalDate date) {
        String sql =
                """
                SELECT
                    HOUR(o.created_at) AS hour,
                    COUNT(*) AS activeOrders,
                    SUM(o.people) AS totalPeople
                FROM `order` o
                WHERE o.restaurant_id = ?
                    AND DATE(o.created_at) = ?
                    AND o.status IN ('PENDING', 'IN_PROGRESS')
                GROUP BY HOUR(o.created_at)
                ORDER BY hour
                """;

        return jdbcTemplate.query(sql, new WorkloadRowMapper(), restaurantId, date);
    }

    // Row mappers for report queries

    public static class DashboardRow {
        public Integer occupiedTables;
        public Integer activeOrders;
        public Integer closedOrdersToday;
        public BigDecimal salesToday;
    }

    public static class SalesReportRow {
        public LocalDate saleDate;
        public String paymentMethod;
        public Long invoiceCount;
        public BigDecimal totalSubtotal;
        public BigDecimal totalTax;
        public BigDecimal totalRevenue;
    }

    public static class SalesSummaryRow {
        public Long totalInvoices;
        public BigDecimal totalRevenue;
    }

    public static class TopProductRow {
        public String productId;
        public String productName;
        public Long totalQuantity;
        public BigDecimal totalRevenue;
        public Long orderCount;
    }

    public static class ProductReportRow {
        public String productId;
        public String productName;
        public BigDecimal unitCost;
        public Integer currentStock;
        public Long orderCount;
        public Long totalQuantity;
        public BigDecimal totalRevenue;
    }

    public static class TransactionSummaryRow {
        public String paymentMethod;
        public Long transactionCount;
        public BigDecimal totalAmount;
    }

    public static class InvoiceSummaryRow {
        public Long totalInvoices;
        public Long paidInvoices;
        public Long unpaidInvoices;
        public BigDecimal totalInvoiced;
        public BigDecimal totalPaid;
        public BigDecimal totalPending;
    }

    public static class CashRegisterSummaryRow {
        public Integer openRegisters;
        public Integer closedRegisters;
        public BigDecimal totalOpeningBalance;
        public BigDecimal totalClosingBalance;
    }

    public static class FootfallRow {
        public Integer hour;
        public Long orderCount;
        public Integer totalPeople;
    }

    public static class WorkloadRow {
        public Integer hour;
        public Long activeOrders;
        public Integer totalPeople;
    }

    // Row mapper implementations

    private static class SalesReportRowMapper implements RowMapper<SalesReportRow> {
        @Override
        public SalesReportRow mapRow(java.sql.ResultSet rs, int rowNum)
                throws java.sql.SQLException {
            SalesReportRow row = new SalesReportRow();
            row.saleDate = rs.getDate("saleDate").toLocalDate();
            row.paymentMethod = rs.getString("paymentMethod");
            row.invoiceCount = rs.getLong("invoiceCount");
            row.totalSubtotal = rs.getBigDecimal("totalSubtotal");
            row.totalTax = rs.getBigDecimal("totalTax");
            row.totalRevenue = rs.getBigDecimal("totalRevenue");
            return row;
        }
    }

    private static class SalesSummaryRowMapper implements RowMapper<SalesSummaryRow> {
        @Override
        public SalesSummaryRow mapRow(java.sql.ResultSet rs, int rowNum)
                throws java.sql.SQLException {
            SalesSummaryRow row = new SalesSummaryRow();
            row.totalInvoices = rs.getLong("totalInvoices");
            row.totalRevenue = rs.getBigDecimal("totalRevenue");
            return row;
        }
    }

    private static class TopProductRowMapper implements RowMapper<TopProductRow> {
        @Override
        public TopProductRow mapRow(java.sql.ResultSet rs, int rowNum)
                throws java.sql.SQLException {
            TopProductRow row = new TopProductRow();
            row.productId = rs.getString("productId");
            row.productName = rs.getString("productName");
            row.totalQuantity = rs.getLong("totalQuantity");
            row.totalRevenue = rs.getBigDecimal("totalRevenue");
            row.orderCount = rs.getLong("orderCount");
            return row;
        }
    }

    private static class ProductReportRowMapper implements RowMapper<ProductReportRow> {
        @Override
        public ProductReportRow mapRow(java.sql.ResultSet rs, int rowNum)
                throws java.sql.SQLException {
            ProductReportRow row = new ProductReportRow();
            row.productId = rs.getString("productId");
            row.productName = rs.getString("productName");
            row.unitCost = rs.getBigDecimal("unitCost");
            row.currentStock = rs.getInt("currentStock");
            row.orderCount = rs.getLong("orderCount");
            row.totalQuantity = rs.getLong("totalQuantity");
            row.totalRevenue = rs.getBigDecimal("totalRevenue");
            return row;
        }
    }

    private static class TransactionSummaryRowMapper implements RowMapper<TransactionSummaryRow> {
        @Override
        public TransactionSummaryRow mapRow(java.sql.ResultSet rs, int rowNum)
                throws java.sql.SQLException {
            TransactionSummaryRow row = new TransactionSummaryRow();
            row.paymentMethod = rs.getString("paymentMethod");
            row.transactionCount = rs.getLong("transactionCount");
            row.totalAmount = rs.getBigDecimal("totalAmount");
            return row;
        }
    }

    private static class InvoiceSummaryRowMapper implements RowMapper<InvoiceSummaryRow> {
        @Override
        public InvoiceSummaryRow mapRow(java.sql.ResultSet rs, int rowNum)
                throws java.sql.SQLException {
            InvoiceSummaryRow row = new InvoiceSummaryRow();
            row.totalInvoices = rs.getLong("totalInvoices");
            row.paidInvoices = rs.getLong("paidInvoices");
            row.unpaidInvoices = rs.getLong("unpaidInvoices");
            row.totalInvoiced = rs.getBigDecimal("totalInvoiced");
            row.totalPaid = rs.getBigDecimal("totalPaid");
            row.totalPending = rs.getBigDecimal("totalPending");
            return row;
        }
    }

    private static class CashRegisterSummaryRowMapper implements RowMapper<CashRegisterSummaryRow> {
        @Override
        public CashRegisterSummaryRow mapRow(java.sql.ResultSet rs, int rowNum)
                throws java.sql.SQLException {
            CashRegisterSummaryRow row = new CashRegisterSummaryRow();
            row.openRegisters = rs.getInt("openRegisters");
            row.closedRegisters = rs.getInt("closedRegisters");
            row.totalOpeningBalance = rs.getBigDecimal("totalOpeningBalance");
            row.totalClosingBalance = rs.getBigDecimal("totalClosingBalance");
            return row;
        }
    }

    private static class FootfallRowMapper implements RowMapper<FootfallRow> {
        @Override
        public FootfallRow mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
            FootfallRow row = new FootfallRow();
            row.hour = rs.getInt("hour");
            row.orderCount = rs.getLong("orderCount");
            row.totalPeople = rs.getInt("totalPeople");
            return row;
        }
    }

    private static class WorkloadRowMapper implements RowMapper<WorkloadRow> {
        @Override
        public WorkloadRow mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
            WorkloadRow row = new WorkloadRow();
            row.hour = rs.getInt("hour");
            row.activeOrders = rs.getLong("activeOrders");
            row.totalPeople = rs.getInt("totalPeople");
            return row;
        }
    }
}
