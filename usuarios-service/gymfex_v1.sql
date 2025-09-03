CREATE TABLE usuario (
  id SERIAL PRIMARY KEY,
  nombre           VARCHAR(100)    NOT NULL,
  apellidos        VARCHAR(150)    NOT NULL,
  email            VARCHAR(255)    NOT NULL UNIQUE,
  telefono         VARCHAR(20),
  role             VARCHAR(50)     NOT NULL CHECK (role IN ('ADMIN', 'SOCIO', 'ENTRENADOR')),
  
 
  tipo_membresia   VARCHAR(50)     NOT NULL CHECK (tipo_membresia IN ('mensual','trimestral','anual','clases_puntuales')),
  inicio_membresia DATE            NOT NULL,
  fin_membresia    DATE            NOT NULL,
  estado           VARCHAR(20)     NOT NULL CHECK (estado IN ('activa','suspendida','expirada')),
  creado_en        TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
