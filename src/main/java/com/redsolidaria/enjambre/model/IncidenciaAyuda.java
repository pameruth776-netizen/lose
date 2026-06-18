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
@Table(name = "incidencias_ayuda")
public class IncidenciaAyuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historial_ayuda_id", nullable = false)
    private HistorialAyuda historialAyuda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reportado_por_id", nullable = false)
    private Usuario reportadoPor;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(name = "fecha_reporte", nullable = false)
    private LocalDateTime fechaReporte = LocalDateTime.now();

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Column(name = "ruta_archivo")
    private String rutaArchivo;

    @Column(name = "tipo_archivo")
    private String tipoArchivo;

    @Column(name = "tamanio_archivo")
    private Long tamanioArchivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evidencia")
    private TipoEvidencia tipoEvidencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoIncidencia estado = EstadoIncidencia.PENDIENTE;

    @Column(length = 500)
    private String resolucion;

    public IncidenciaAyuda(HistorialAyuda historialAyuda, Usuario reportadoPor, String descripcion) {
        this.historialAyuda = historialAyuda;
        this.reportadoPor = reportadoPor;
        this.descripcion = descripcion;
        this.fechaReporte = LocalDateTime.now();
        this.estado = EstadoIncidencia.PENDIENTE;
    }

    public Usuario getInvolucrado() {
        if (historialAyuda != null && historialAyuda.getSolicitud() != null && reportadoPor != null) {
            Usuario vol = historialAyuda.getSolicitud().getVoluntarioAceptado();
            Usuario disc = historialAyuda.getSolicitud().getDiscapacitado();
            if (vol != null && reportadoPor.getId().equals(vol.getId())) {
                return disc;
            } else {
                return vol;
            }
        }
        return null;
    }
}
