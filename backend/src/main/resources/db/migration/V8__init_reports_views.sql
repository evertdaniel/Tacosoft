-- ============ REPORTING VIEWS ============
-- Migration V8__init_reports_views.sql
-- Implements: SPEC-REPORT-001

-- Dashboard view: sales summary, active orders, table occupancy
CREATE OR REPLACE VIEW v_dashboard AS
SELECT
    r.id AS restaurant_id,
    (SELECT COUNT(*) FROM restaurant_table rt WHERE rt.restaurant_id = r.id AND rt.status = 'OCCUPIED') AS occupied_tables,
    (SELECT COUNT(*) FROM `order` o WHERE o.restaurant_id = r.id AND o.status IN ('PENDING', 'IN_PROGRESS')) AS active_orders,
    (SELECT COUNT(*) FROM `order` o WHERE o.restaurant_id = r.id AND o.status = 'CLOSED' AND DATE(o.created_at) = CURDATE()) AS closed_orders_today,
    (SELECT COALESCE(SUM(i.total), 0) FROM invoice i WHERE i.restaurant_id = r.id AND DATE(i.created_at) = CURDATE()) AS sales_today
FROM restaurant r;

-- Sales report view: revenue by payment method, period comparison
CREATE OR REPLACE VIEW v_sales_report AS
SELECT
    r.id AS restaurant_id,
    DATE(i.created_at) AS sale_date,
    i.payment_method,
    COUNT(*) AS invoice_count,
    SUM(i.subtotal) AS total_subtotal,
    SUM(i.tax) AS total_tax,
    SUM(i.total) AS total_revenue
FROM invoice i
JOIN restaurant r ON r.id = i.restaurant_id
GROUP BY r.id, DATE(i.created_at), i.payment_method;

-- Product sales view: sales by product, margin analysis
CREATE OR REPLACE VIEW v_product_sales AS
SELECT
    p.restaurant_id,
    p.id AS product_id,
    p.name AS product_name,
    COUNT(od.id) AS order_count,
    SUM(od.quantity) AS total_quantity,
    SUM(od.amount) AS total_revenue,
    p.unit_cost,
    SUM(od.amount) - (SUM(od.quantity) * p.unit_cost) AS total_margin
FROM product p
LEFT JOIN order_detail od ON od.product_id = p.id
GROUP BY p.id, p.restaurant_id, p.name, p.unit_cost;

-- Financial report view: income/expense breakdown
CREATE OR REPLACE VIEW v_financial_report AS
SELECT
    cr.restaurant_id,
    DATE(t.created_at) AS transaction_date,
    t.type,
    t.payment_method,
    COUNT(*) AS transaction_count,
    SUM(t.amount) AS total_amount
FROM cash_register cr
JOIN `transaction` t ON t.cash_register_id = cr.id
GROUP BY cr.restaurant_id, DATE(t.created_at), t.type, t.payment_method;

-- Footfall view: peak hours analysis
CREATE OR REPLACE VIEW v_footfall AS
SELECT
    o.restaurant_id,
    DATE(o.created_at) AS order_date,
    HOUR(o.created_at) AS hour,
    COUNT(*) AS order_count,
    SUM(o.people) AS total_people
FROM `order` o
GROUP BY o.restaurant_id, DATE(o.created_at), HOUR(o.created_at);
