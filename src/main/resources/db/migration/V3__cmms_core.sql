-- ===========================================
-- V3 - CMMS CORE: COMPANY, TECHNICIAN, ASSET, WORK ORDER
-- ===========================================

-- Empresas (clientes dueños de activos / contratos)
CREATE TABLE companies (
                           id           BIGSERIAL PRIMARY KEY,
                           name         VARCHAR(150) NOT NULL,
                           tax_id       VARCHAR(50),
                           phone        VARCHAR(50),
                           email        VARCHAR(150),
                           address      VARCHAR(255),
                           is_active    BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                           updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Sedes / sitios de la empresa
CREATE TABLE sites (
                       id           BIGSERIAL PRIMARY KEY,
                       company_id   BIGINT NOT NULL REFERENCES companies(id),
                       name         VARCHAR(150) NOT NULL,
                       code         VARCHAR(50),
                       address      VARCHAR(255),
                       city         VARCHAR(100),
                       state        VARCHAR(100),
                       country      VARCHAR(100),
                       is_active    BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                       updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Técnicos (especialización de user)
CREATE TABLE technicians (
                             id            BIGSERIAL PRIMARY KEY,
                             user_id       BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
                             skill_level   VARCHAR(50),      -- junior, semi, senior
                             hourly_rate   NUMERIC(10,2),
                             phone_alt     VARCHAR(50),
                             notes         VARCHAR(255),
                             is_active     BOOLEAN NOT NULL DEFAULT TRUE,
                             created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
                             updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Clientes/Contactos finales ligados a usuario (opcional)
CREATE TABLE customers (
                           id            BIGSERIAL PRIMARY KEY,
                           user_id       BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
                           company_id    BIGINT REFERENCES companies(id),
                           position      VARCHAR(100),
                           phone_alt     VARCHAR(50),
                           notes         VARCHAR(255),
                           is_active     BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
                           updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Categorías de activos
CREATE TABLE asset_categories (
                                  id            BIGSERIAL PRIMARY KEY,
                                  name          VARCHAR(100) NOT NULL,
                                  code          VARCHAR(50),
                                  description   VARCHAR(255),
                                  is_active     BOOLEAN NOT NULL DEFAULT TRUE,
                                  created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
                                  updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Activos / Equipos
CREATE TABLE assets (
                        id               BIGSERIAL PRIMARY KEY,
                        site_id          BIGINT NOT NULL REFERENCES sites(id),
                        category_id      BIGINT REFERENCES asset_categories(id),
                        code             VARCHAR(100) NOT NULL,           -- tag interno, placa, etc.
                        name             VARCHAR(150) NOT NULL,
                        description      VARCHAR(255),
                        serial_number    VARCHAR(100),
                        manufacturer     VARCHAR(100),
                        model            VARCHAR(100),
                        installed_at     DATE,
                        is_active        BOOLEAN NOT NULL DEFAULT TRUE,
                        created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
                        updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
                        CONSTRAINT uk_assets_site_code UNIQUE (site_id, code)
);

-- Órdenes de trabajo
CREATE TABLE work_orders (
                             id                 BIGSERIAL PRIMARY KEY,
                             code               VARCHAR(50) NOT NULL,  -- WO-0001, etc.
                             title              VARCHAR(200) NOT NULL,
                             description        TEXT,
                             company_id         BIGINT REFERENCES companies(id),
                             site_id            BIGINT REFERENCES sites(id),
                             asset_id           BIGINT REFERENCES assets(id),

                             requester_id       BIGINT REFERENCES users(id),    -- quién la pidió
                             assigned_tech_id   BIGINT REFERENCES technicians(id),

                             status             VARCHAR(30) NOT NULL,           -- OPEN, IN_PROGRESS, COMPLETED, CANCELLED
                             priority           VARCHAR(20) NOT NULL,           -- LOW, MEDIUM, HIGH, URGENT

                             due_date           DATE,
                             scheduled_start    TIMESTAMP,
                             scheduled_end      TIMESTAMP,
                             started_at         TIMESTAMP,
                             completed_at       TIMESTAMP,

                             estimated_hours    NUMERIC(10,2),
                             actual_hours       NUMERIC(10,2),

                             created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
                             created_by         BIGINT REFERENCES users(id),
                             updated_at         TIMESTAMP NOT NULL DEFAULT NOW(),
                             updated_by         BIGINT REFERENCES users(id)
);

-- Evidencias (fotos, audios, docs) asociadas a la OT
CREATE TABLE work_order_evidences (
                                      id             BIGSERIAL PRIMARY KEY,
                                      work_order_id  BIGINT NOT NULL REFERENCES work_orders(id) ON DELETE CASCADE,
                                      type           VARCHAR(30) NOT NULL,         -- PHOTO, AUDIO, DOCUMENT
                                      url            VARCHAR(500) NOT NULL,        -- ruta en S3, disco, etc.
                                      description    VARCHAR(255),
                                      uploaded_at    TIMESTAMP NOT NULL DEFAULT NOW(),
                                      uploaded_by    BIGINT REFERENCES users(id)
);
