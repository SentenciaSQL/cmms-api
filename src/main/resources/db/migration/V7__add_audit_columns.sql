-- ===========================================
-- V7 - AUDIT COLUMNS: created_by / updated_by
-- ===========================================
-- Agrega las columnas de auditoría a todas las tablas del CMMS.
-- work_orders ya las tiene desde V3, se omite.
-- Las columnas son nullable para no romper registros existentes.

-- companies
ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES users(id);

-- sites
ALTER TABLE sites
    ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES users(id);

-- asset_categories
ALTER TABLE asset_categories
    ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES users(id);

-- assets
ALTER TABLE assets
    ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES users(id);

-- technicians
ALTER TABLE technicians
    ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES users(id);

-- customers
ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES users(id);

-- suppliers
ALTER TABLE suppliers
    ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES users(id);

-- inventory_items
ALTER TABLE inventory_items
    ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES users(id);

-- inventory_movements
ALTER TABLE inventory_movements
    ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES users(id);

-- maintenance_plans
ALTER TABLE maintenance_plans
    ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES users(id);

-- notifications
ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES users(id);

-- Índices para consultas de auditoría ("¿qué creó/modificó este usuario?")
CREATE INDEX IF NOT EXISTS idx_audit_companies_created_by    ON companies(created_by);
CREATE INDEX IF NOT EXISTS idx_audit_assets_created_by       ON assets(created_by);
CREATE INDEX IF NOT EXISTS idx_audit_work_orders_created_by  ON work_orders(created_by);
CREATE INDEX IF NOT EXISTS idx_audit_inventory_created_by    ON inventory_items(created_by);
CREATE INDEX IF NOT EXISTS idx_audit_maint_plans_created_by  ON maintenance_plans(created_by);
