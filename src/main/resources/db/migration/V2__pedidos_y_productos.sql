ALTER TABLE herramientas RENAME TO productos;

CREATE TABLE pedidos (
    id SERIAL PRIMARY KEY,
    nombre_cliente VARCHAR(150) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    codigo_verificacion VARCHAR(6) NOT NULL,
    productos JSONB NOT NULL,
    CONSTRAINT pedidos_estado_valido CHECK (estado IN ('PENDIENTE', 'ENVIADO', 'RECIBIDO'))
);

CREATE UNIQUE INDEX un_pedido_pendiente_por_cliente
    ON pedidos (LOWER(nombre_cliente))
    WHERE estado = 'PENDIENTE';
