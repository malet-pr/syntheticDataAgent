INSERT INTO data_distribution_profile (table_name, distribution_rules) VALUES
-- 1. CUSTOMER / USERS
(
    'customer',
    '{
      "description": "Baseline distribution for customer profiles",
      "column_distributions": {
        "status": {
          "type": "categorical_percent",
          "weights": {"ACTIVE": 80, "PENDING": 15, "INACTIVE": 5}
        },
        "customer_type": {
          "type": "categorical_percent",
          "weights": {"RETAIL": 85, "WHOLESALE": 15}
        }
      }
    }'::jsonb
),

-- 2. PRODUCT & CATALOG
(
    'product',
    '{
      "description": "Catalog distribution across categories and pricing",
      "column_distributions": {
        "status": {
          "type": "categorical_percent",
          "weights": {"AVAILABLE": 85, "OUT_OF_STOCK": 10, "DISCONTINUED": 5}
        },
        "price": {
          "type": "range_uniform",
          "min": 5.00,
          "max": 250.00
        }
      }
    }'::jsonb
),

-- 3. ORDERS (Parent Entity)
(
    'orders',
    '{
      "description": "Order placement status and timeline rules",
      "parent_table": "customer",
      "parent_fk": "customer_id",
      "column_distributions": {
        "status": {
          "type": "categorical_percent",
          "weights": {"DELIVERED": 70, "SHIPPED": 15, "PROCESSING": 10, "CANCELLED": 5}
        },
        "payment_method": {
          "type": "categorical_percent",
          "weights": {"CREDIT_CARD": 60, "DEBIT_CARD": 25, "PIX": 10, "INVOICE": 5}
        }
      }
    }'::jsonb
),

-- 4. ORDER LINES (Child / Cardinality Constraint)
(
    'order_line',
    '{
      "description": "Cardinality from orders to order_line with product skews",
      "parent_table": "orders",
      "parent_fk": "order_id",
      "child_cardinality": {
        "type": "categorical_percent",
        "rules": [
          {"min_items": 1, "max_items": 1, "weight_percent": 25},
          {"min_items": 2, "max_items": 3, "weight_percent": 60},
          {"min_items": 4, "max_items": 6, "weight_percent": 15}
        ]
      },
      "column_distributions": {
        "product_id": {
          "type": "pareto_skew",
          "description": "80% of lines reference top 20% of products"
        },
        "quantity": {
          "type": "range_uniform",
          "min": 1,
          "max": 5
        }
      }
    }'::jsonb
),

-- 5. LOGISTICS / WAREHOUSE
(
    'logistic',
    '{
      "description": "Warehouse and carrier logistics assignment",
      "column_distributions": {
        "carrier_status": {
          "type": "categorical_percent",
          "weights": {"ACTIVE": 90, "MAINTENANCE": 10}
        },
        "capacity_utilization": {
          "type": "range_uniform",
          "min": 20,
          "max": 95
        }
      }
    }'::jsonb
)
ON CONFLICT (table_name)
    DO UPDATE SET
                  distribution_rules = EXCLUDED.distribution_rules,
                  updated_at = CURRENT_TIMESTAMP;