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
@Table(name = "comentarios_ayuda")
public class ComentarioAyuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historial_ayuda_id", nullable = false)
    private HistorialAyuda historialAyuda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(nullable = false, length = 500)
    private String comentario;

    @Column(name = "fecha_comentario", nullable = false)
    private LocalDateTime fechaComentario = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoComentario tipo;

    public ComentarioAyuda(HistorialAyuda historialAyuda, Usuario autor, String comentario, TipoComentario tipo) {
        this.historialAyuda = historialAyuda;
        this.autor = autor;
        this.comentario = comentario;
        this.tipo = tipo;
        this.fechaComentario = LocalDateTime.now();
    }

    public Usuario getDestinatario() {
        if (historialAyuda != null && historialAyuda.getSolicitud() != null && autor != null) {
            Usuario vol = historialAyuda.getSolicitud().getVoluntarioAceptado();
            Usuario disc = historialAyuda.getSolicitud().getDiscapacitado();
            if (vol != null && autor.getId().equals(vol.getId())) {
                return disc;
            } else {
                return vol;
            }
        }
        return null;
    }
}
