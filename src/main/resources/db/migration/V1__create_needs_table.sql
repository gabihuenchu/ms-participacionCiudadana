CREATE TABLE IF NOT EXISTS necesidades (
    id UUID PRIMARY KEY,
    centro_id UUID NOT NULL,
    item_id UUID NOT NULL,
    emergencia_id UUID,
    cantidad_necesaria INTEGER NOT NULL CHECK (cantidad_necesaria > 0),
    prioridad VARCHAR(20) NOT NULL,
    origen VARCHAR(20) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
    creada_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resuelta_en TIMESTAMPTZ,
    CONSTRAINT chk_necesidad_prioridad CHECK (prioridad IN ('BAJO', 'MEDIO', 'ALTO', 'CRITICO')),
    CONSTRAINT chk_necesidad_origen CHECK (origen IN ('MANUAL', 'AUTOMATICO')),
    CONSTRAINT chk_necesidad_estado CHECK (estado IN ('ACTIVA', 'PARCIALMENTE_CUBIERTA', 'RESUELTA'))
);

CREATE INDEX idx_necesidades_centro_id ON necesidades(centro_id);
CREATE INDEX idx_necesidades_estado ON necesidades(estado);
CREATE INDEX idx_necesidades_prioridad ON necesidades(prioridad);
CREATE INDEX idx_necesidades_item_id ON necesidades(item_id);
