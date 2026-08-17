CREATE TABLE region (
                        id SERIAL PRIMARY KEY,
                        code VARCHAR(50),
                        name VARCHAR(100),
                        active CHAR(1) DEFAULT 'Y'
);

CREATE TABLE manager (
                         id SERIAL PRIMARY KEY,
                         code VARCHAR(50),
                         name VARCHAR(100),
                         region_id INT REFERENCES region(id),
                         active CHAR(1) DEFAULT 'Y'
);

CREATE TABLE representative (
                                id SERIAL PRIMARY KEY,
                                code VARCHAR(50),
                                name VARCHAR(100),
                                region_id INT REFERENCES region(id),
                                manager_id INT REFERENCES manager(id),
                                active CHAR(1) DEFAULT 'Y'
);

CREATE TABLE customer (
                          id SERIAL PRIMARY KEY,
                          code VARCHAR(50),
                          name VARCHAR(100),
                          region_id INT REFERENCES region(id),
                          join_date TIMESTAMP,
                          status VARCHAR(20) NOT NULL,
                          verified BOOLEAN DEFAULT FALSE,
                          active CHAR(1) DEFAULT 'Y'
);

CREATE TABLE product_category (
                                  id SERIAL PRIMARY KEY,
                                  code VARCHAR(50),
                                  name VARCHAR(100),
                                  active CHAR(1) DEFAULT 'Y'
);

CREATE TABLE product (
                         id SERIAL PRIMARY KEY,
                         code VARCHAR(50),
                         name VARCHAR(100),
                         description TEXT,
                         price NUMERIC(12, 2),
                         category_id INT REFERENCES product_category(id),
                         quantity INT,
                         inventory_status VARCHAR(20) NOT NULL,
                         rating INT,
                         active CHAR(1) DEFAULT 'Y'
);

-- "customer_order" is a PostgreSQL reserved word; quotes are required
CREATE TABLE "customer_order" (
                         id SERIAL PRIMARY KEY,
                         invoice VARCHAR(50),
                         customer_id INT REFERENCES customer(id),
                         representative_id INT REFERENCES representative(id),
                         order_date TIMESTAMP,
                         status VARCHAR(20) NOT NULL,
                         active CHAR(1) DEFAULT 'Y'
);

CREATE TABLE orderline (
                           id SERIAL PRIMARY KEY,
                           code VARCHAR(50),
                           order_id INT REFERENCES "customer_order"(id) ON DELETE CASCADE,
                           product_id INT REFERENCES product(id),
                           amount NUMERIC(12, 2),
                           quantity INT,
                           active CHAR(1) DEFAULT 'Y'
);