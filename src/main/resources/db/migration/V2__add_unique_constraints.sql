ALTER TABLE customer ADD CONSTRAINT uq_customer_code UNIQUE (code);
ALTER TABLE customer_order ADD CONSTRAINT uq_customer_order_invoice UNIQUE (invoice);
ALTER TABLE manager ADD CONSTRAINT uq_manager_code UNIQUE (code);
ALTER TABLE orderline ADD CONSTRAINT uq_orderline_code UNIQUE (code);
ALTER TABLE product ADD CONSTRAINT uq_product_code UNIQUE (code);
ALTER TABLE product_category ADD CONSTRAINT uq_product_category_code UNIQUE (code);
ALTER TABLE region ADD CONSTRAINT uq_region_code UNIQUE (code);
ALTER TABLE representative ADD CONSTRAINT uq_representative_code UNIQUE (code);