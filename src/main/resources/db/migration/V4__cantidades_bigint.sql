-- Cantidades a BIGINT alineadas con ms-resources (fuente de verdad del inventario),
-- para soportar el escalamiento del sistema (miles/millones de unidades).
ALTER TABLE necesidades ALTER COLUMN cantidad_necesaria TYPE BIGINT;
ALTER TABLE items_donacion ALTER COLUMN cantidad TYPE BIGINT;
