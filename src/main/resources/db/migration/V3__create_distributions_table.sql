CREATE TABLE data_distribution_profile (
                                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                           table_name VARCHAR(100) NOT NULL UNIQUE,
                                           distribution_rules JSONB NOT NULL,
                                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);