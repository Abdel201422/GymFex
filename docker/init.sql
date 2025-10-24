CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,

    -- Datos generales
    nombre           VARCHAR(100)    NOT NULL,
    apellidos        VARCHAR(150)    NOT NULL,
    email            VARCHAR(255)    NOT NULL UNIQUE,
    telefono         VARCHAR(20),
    role             VARCHAR(50)     NOT NULL CHECK (role IN ('ADMIN', 'SOCIO')),

    -- Solo para ADMIN
    password         VARCHAR(255),

    -- Solo para SOCIO
    tipo_membresia   VARCHAR(50) CHECK (tipo_membresia IN ('mensual','trimestral','anual')),
    inicio_membresia DATE,
    fin_membresia    DATE,

    -- Estado general
    estado           VARCHAR(20)  CHECK (estado IN ('ACTIVO','INACTIVO', 'EXENTO')),
    creado_en        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
