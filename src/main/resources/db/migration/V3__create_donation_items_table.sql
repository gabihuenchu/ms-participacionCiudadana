CREATE TABLE IF NOT EXISTS items_donacion (
    id UUID PRIMARY KEY,
    donacion_id UUID NOT NULL REFERENCES donaciones(id) ON DELETE CASCADE,
    item_id UUID NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0)
);

CREATE INDEX idx_items_donacion_donacion_id ON items_donacion(donacion_id);
CREATE INDEX idx_items_donacion_item_id ON items_donacion(item_id);
