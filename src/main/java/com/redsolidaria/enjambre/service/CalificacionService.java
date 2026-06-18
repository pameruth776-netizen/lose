package com.redsolidaria.enjambre.service;

import com.redsolidaria.enjambre.model.CalificacionAyuda;
import com.redsolidaria.enjambre.model.HistorialAyuda;
import com.redsolidaria.enjambre.model.TipoMedalla;
import com.redsolidaria.enjambre.model.Voluntario;
import com.redsolidaria.enjambre.repository.CalificacionAyudaRepository;
import com.redsolidaria.enjambre.repository.HistorialAyudaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CalificacionService {

    @Autowired
    private CalificacionAyudaRepository calificacionAyudaRepository;

    @Autowired
    private HistorialAyudaRepository historialAyudaRepository;

    @Autowired
    private EmailService emailService;

    public Optional<CalificacionAyuda> obtenerPorHistorial(Long historialId) {
        return calificacionAyudaRepository.findByHistorialAyuda_Id(historialId);
    }

    public boolean verificarCalificacion(Long historialId) {
        return calificacionAyudaRepository.findByHistorialAyuda_Id(historialId).isPresent();
    }

    @Transactional
    public CalificacionAyuda guardarCalificacion(Long historialId, TipoMedalla tipoMedalla, String comentario) {
        HistorialAyuda historial = historialAyudaRepository.findById(historialId)
                .orElseThrow(() -> new IllegalArgumentException("Historial de ayuda no encontrado"));

        if (verificarCalificacion(historialId)) {
            throw new IllegalStateException("Este servicio de ayuda ya ha sido calificado.");
        }

        CalificacionAyuda calificacion = new CalificacionAyuda(historial, tipoMedalla, comentario);
        CalificacionAyuda guardada = calificacionAyudaRepository.save(calificacion);

        // Enviar correo de notificación al voluntario
        Voluntario voluntario = guardada.getVoluntario();
        if (voluntario != null && voluntario.getEmail() != null) {
            try {
                emailService.enviarNotificacionCalificacion(
                        voluntario.getEmail(),
                        voluntario.getNombres(),
                        tipoMedalla.name(),
                        comentario
                );
            } catch (Exception e) {
                System.err.println("Error al enviar correo de calificación al voluntario: " + e.getMessage());
            }
        }

        return guardada;
    }
}
