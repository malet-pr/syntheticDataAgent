INSERT INTO data_distribution_profile (table_name, distribution_rules) VALUES
-- 1. CUSTOMER / USERS
(
    'customer',
    '{
      "description": "Baseline distribution for customer profiles",
      "column_distributions": {
        "status": {
          "type": "categorical_percent",
          "weights": {"QUALIFIED": 80, "NEW": 15, "UNQUALIFIED": 5}
        }
      }
    }'::jsonb
),

-- 2. PRODUCT
(
    'product',
    '{
      "description": "Catalog distribution across categories and pricing",
      "column_distributions": {
        "status": {
          "type": "categorical_percent",
          "weights": {"INSTOCK": 85, "LOWSTOCK": 10, "OUTOFSTOCK": 5}
        },
        "price": {
          "type": "percent",
          "weights": {"1-100": 60, "100–300": 25, "300–800": 10, "800–2000": 5}
        }
      }
    }'::jsonb
),

-- 3. CUSTOMER_ORDER (Parent Entity)
(
    'customer_order',
    '{
      "description": "Order placement status and timeline rules",
      "parent_table": "customer",
      "parent_fk": "customer_id",
      "column_distributions": {
        "status": {
          "type": "categorical_percent",
          "weights": {"DELIVERED": 70, "CANCELLED": 15, "RETURNED": 10, "PENDING": 5}
        }
      }
    }'::jsonb
),

-- 4. ORDERLINE (Child / Cardinality Constraint)
(
    'orderline',
    '{
      "description": "Cardinality from orders to order_line with product skews",
      "parent_table": "customer_order",
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
)
ON CONFLICT (table_name)
    DO UPDATE SET
                  distribution_rules = EXCLUDED.distribution_rules,
                  updated_at = CURRENT_TIMESTAMP;