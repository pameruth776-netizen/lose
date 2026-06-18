package com.redsolidaria.enjambre.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "calificaciones_ayuda")
public class CalificacionAyuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historial_ayuda_id", nullable = false, unique = true)
    private HistorialAyuda historialAyuda;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_medalla", nullable = false)
    private TipoMedalla tipoMedalla;

    @Column(name = "fecha_calificacion", nullable = false)
    private LocalDateTime fechaCalificacion = LocalDateTime.now();

    @Column(length = 300)
    private String comentario;

    public CalificacionAyuda(HistorialAyuda historialAyuda, TipoMedalla tipoMedalla, String comentario) {
        this.historialAyuda = historialAyuda;
        this.tipoMedalla = tipoMedalla;
        this.comentario = comentario;
        this.fechaCalificacion = LocalDateTime.now();
    }

    public Voluntario getVoluntario() {
        if (historialAyuda != null && historialAyuda.getSolicitud() != null) {
            return historialAyuda.getSolicitud().getVoluntarioAceptado();
        }
        return null;
    }

    public PersonaDiscapacitada getDiscapacitado() {
        if (historialAyuda != null && historialAyuda.getSolicitud() != null) {
            return historialAyuda.getSolicitud().getDiscapacitado();
        }
        return null;
    }
}
