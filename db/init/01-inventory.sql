-- Executed by the postgres image only when the data volume is created for the first time.
-- Same table definition as src/main/resources/schema.sql, plus sample stock for manual testing.
CREATE TABLE IF NOT EXISTS inventory (
    sku VARCHAR(64) PRIMARY KEY,
    quantity INTEGER NOT NULL
);

INSERT INTO inventory (sku, quantity) VALUES
    ('RICE', 100),
    ('OIL', 50),
    ('SUGAR', 5)
ON CONFLICT (sku) DO NOTHING;
