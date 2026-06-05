CREATE TABLE IF NOT EXISTS donaciones (
    id UUID PRIMARY KEY,
    centro_id UUID NOT NULL,
    usuario_donante_id UUID,
    codigo_qr VARCHAR(255) NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    donado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    confirmado_en TIMESTAMPTZ,
    confirmado_por_usuario_id UUID,
    CONSTRAINT chk_donacion_estado CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'CANCELADA'))
);

CREATE INDEX idx_donaciones_centro_id ON donaciones(centro_id);
CREATE INDEX idx_donaciones_usuario_donante_id ON donaciones(usuario_donante_id);
CREATE INDEX idx_donaciones_estado ON donaciones(estado);
CREATE INDEX idx_donaciones_codigo_qr ON donaciones(codigo_qr);
