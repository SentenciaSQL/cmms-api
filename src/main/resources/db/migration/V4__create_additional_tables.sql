-- ===========================================
-- V4 - CMMS EXTENDED: MAINTENANCE, INVENTORY, SUPPLIERS, NOTIFICATIONS
-- ===========================================

-- Proveedores
CREATE TABLE suppliers (
                           id                BIGSERIAL PRIMARY KEY,
                           name              VARCHAR(150) NOT NULL,
                           code              VARCHAR(50),
                           description       VARCHAR(255),
                           email             VARCHAR(150) NOT NULL UNIQUE,
                           phone             VARCHAR(50),
                           mobile            VARCHAR(50),
                           website           VARCHAR(255),
                           tax_id            VARCHAR(50),

    -- Dirección
                           street            VARCHAR(255),
                           city              VARCHAR(100),
                           state             VARCHAR(100),
                           postal_code       VARCHAR(20),
                           country           VARCHAR(100),

    -- Contacto
                           contact_person    VARCHAR(150),
                           contact_email     VARCHAR(150),
                           contact_phone     VARCHAR(50),

                           supplier_type     VARCHAR(50),              -- PARTS, TOOLS, SERVICES, MATERIALS, GENERAL
                           notes             TEXT,

                           is_active         BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
                           updated_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Ítems de Inventario (Repuestos, Herramientas, Consumibles)
CREATE TABLE inventory_items (
                                 id                BIGSERIAL PRIMARY KEY,
                                 code              VARCHAR(100) NOT NULL UNIQUE,
                                 name              VARCHAR(150) NOT NULL,
                                 description       TEXT,
                                 item_type         VARCHAR(50) NOT NULL,         -- SPARE_PART, TOOL, CONSUMABLE, MATERIAL

                                 supplier_id       BIGINT REFERENCES suppliers(id),

    -- Stock
                                 current_stock     INTEGER NOT NULL DEFAULT 0,
                                 min_stock         INTEGER NOT NULL DEFAULT 0,
                                 max_stock         INTEGER NOT NULL DEFAULT 100,
                                 reorder_point     INTEGER NOT NULL DEFAULT 10,

                                 unit              VARCHAR(50) NOT NULL,         -- UNIT, KG, LITER, METER, etc.
                                 unit_cost         NUMERIC(10,2),

    -- Ubicación y Detalles
                                 location          VARCHAR(255),                  -- Ubicación en almacén
                                 manufacturer      VARCHAR(150),
                                 part_number       VARCHAR(100),
                                 image_url         VARCHAR(500),

                                 is_active         BOOLEAN NOT NULL DEFAULT TRUE,
                                 created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
                                 updated_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Movimientos de Inventario
CREATE TABLE inventory_movements (
                                     id                    BIGSERIAL PRIMARY KEY,
                                     inventory_item_id     BIGINT NOT NULL REFERENCES inventory_items(id),

                                     movement_type         VARCHAR(50) NOT NULL,     -- IN, OUT, ADJUSTMENT, TRANSFER
                                     quantity              INTEGER NOT NULL,

                                     previous_stock        INTEGER,
                                     new_stock             INTEGER,

                                     unit_cost             NUMERIC(10,2),
                                     total_cost            NUMERIC(10,2),

                                     work_order_id         BIGINT REFERENCES work_orders(id),
                                     user_id               BIGINT NOT NULL REFERENCES users(id),

                                     notes                 TEXT,
                                     reference_number      VARCHAR(100),             -- Número de factura, orden de compra, etc.

                                     movement_date         TIMESTAMP NOT NULL,
                                     created_at            TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Planes de Mantenimiento Preventivo
CREATE TABLE maintenance_plans (
                                   id                            BIGSERIAL PRIMARY KEY,
                                   name                          VARCHAR(200) NOT NULL,
                                   description                   TEXT,

                                   asset_id                      BIGINT NOT NULL REFERENCES assets(id),

                                   type                          VARCHAR(50) NOT NULL,         -- PREVENTIVE, PREDICTIVE, CORRECTIVE
                                   frequency                     VARCHAR(50) NOT NULL,         -- DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
                                   frequency_value               INTEGER NOT NULL,             -- Cada cuántos días/semanas/meses

                                   next_scheduled_date           TIMESTAMP,
                                   last_execution_date           TIMESTAMP,

                                   estimated_duration_minutes    INTEGER NOT NULL,
                                   priority                      VARCHAR(20) NOT NULL,         -- LOW, MEDIUM, HIGH, CRITICAL

                                   instructions                  TEXT,

                                   assigned_technician_id        BIGINT REFERENCES technicians(id),

                                   is_active                     BOOLEAN NOT NULL DEFAULT TRUE,
                                   auto_generate_work_order      BOOLEAN NOT NULL DEFAULT TRUE,

                                   created_at                    TIMESTAMP NOT NULL DEFAULT NOW(),
                                   updated_at                    TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Notificaciones
CREATE TABLE notifications (
                               id                    BIGSERIAL PRIMARY KEY,
                               user_id               BIGINT NOT NULL REFERENCES users(id),

                               title                 VARCHAR(200) NOT NULL,
                               message               TEXT NOT NULL,

                               type                  VARCHAR(50) NOT NULL,         -- INFO, WARNING, ERROR, SUCCESS
                               category              VARCHAR(50) NOT NULL,         -- WORK_ORDER, MAINTENANCE, INVENTORY, ASSET, SYSTEM, USER

                               is_read               BOOLEAN NOT NULL DEFAULT FALSE,
                               link                  VARCHAR(500),                  -- URL para navegar cuando se hace clic
                               related_entity_id     BIGINT,                        -- ID de la entidad relacionada (OT, Asset, etc.)

                               created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
                               read_at               TIMESTAMP
);

-- ===========================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- ===========================================

-- Suppliers
CREATE INDEX idx_suppliers_email ON suppliers(email);
CREATE INDEX idx_suppliers_active ON suppliers(is_active);
CREATE INDEX idx_suppliers_type ON suppliers(supplier_type);

-- Inventory Items
CREATE INDEX idx_inventory_items_code ON inventory_items(code);
CREATE INDEX idx_inventory_items_type ON inventory_items(item_type);
CREATE INDEX idx_inventory_items_supplier ON inventory_items(supplier_id);
CREATE INDEX idx_inventory_items_active ON inventory_items(is_active);
CREATE INDEX idx_inventory_items_low_stock ON inventory_items(current_stock) WHERE current_stock <= min_stock;

-- Inventory Movements
CREATE INDEX idx_inventory_movements_item ON inventory_movements(inventory_item_id);
CREATE INDEX idx_inventory_movements_type ON inventory_movements(movement_type);
CREATE INDEX idx_inventory_movements_wo ON inventory_movements(work_order_id);
CREATE INDEX idx_inventory_movements_user ON inventory_movements(user_id);
CREATE INDEX idx_inventory_movements_date ON inventory_movements(movement_date);

-- Maintenance Plans
CREATE INDEX idx_maintenance_plans_asset ON maintenance_plans(asset_id);
CREATE INDEX idx_maintenance_plans_type ON maintenance_plans(type);
CREATE INDEX idx_maintenance_plans_active ON maintenance_plans(is_active);
CREATE INDEX idx_maintenance_plans_next_date ON maintenance_plans(next_scheduled_date);
CREATE INDEX idx_maintenance_plans_technician ON maintenance_plans(assigned_technician_id);

-- Notifications
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_category ON notifications(category);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_notifications_created ON notifications(created_at);

-- ===========================================
-- COMENTARIOS
-- ===========================================

COMMENT ON TABLE suppliers IS 'Proveedores de repuestos, herramientas y servicios';
COMMENT ON TABLE inventory_items IS 'Repuestos, herramientas, consumibles y materiales';
COMMENT ON TABLE inventory_movements IS 'Registro de entradas, salidas y ajustes de inventario';
COMMENT ON TABLE maintenance_plans IS 'Planes de mantenimiento preventivo y predictivo';
COMMENT ON TABLE notifications IS 'Notificaciones del sistema para usuarios';

COMMENT ON COLUMN inventory_items.reorder_point IS 'Cuando el stock llega a este punto, se debe hacer pedido';
COMMENT ON COLUMN inventory_movements.movement_type IS 'IN=Entrada, OUT=Salida, ADJUSTMENT=Ajuste, TRANSFER=Transferencia';
COMMENT ON COLUMN maintenance_plans.frequency_value IS 'Número de unidades de frecuencia (ej: cada 3 meses)';
COMMENT ON COLUMN maintenance_plans.auto_generate_work_order IS 'Si TRUE, genera OT automáticamente al llegar la fecha';