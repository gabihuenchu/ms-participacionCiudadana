package cl.catastrofescl.citizen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "necesidades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Necesidad {

    @Id
    private UUID id;

    @Column(name = "centro_id", nullable = false)
    private UUID centroId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "emergencia_id")
    private UUID emergenciaId;

    @Column(name = "cantidad_necesaria", nullable = false)
    private Integer cantidadNecesaria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadNecesidad prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigenNecesidad origen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoNecesidad estado;

    @Column(name = "creada_en", nullable = false)
    private OffsetDateTime creadaEn;

    @Column(name = "resuelta_en")
    private OffsetDateTime resueltaEn;
}
