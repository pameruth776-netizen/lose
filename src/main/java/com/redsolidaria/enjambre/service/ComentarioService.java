package com.redsolidaria.enjambre.service;

import com.redsolidaria.enjambre.model.ComentarioAyuda;
import com.redsolidaria.enjambre.model.HistorialAyuda;
import com.redsolidaria.enjambre.model.TipoComentario;
import com.redsolidaria.enjambre.model.Usuario;
import com.redsolidaria.enjambre.repository.ComentarioAyudaRepository;
import com.redsolidaria.enjambre.repository.HistorialAyudaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComentarioService {

    @Autowired
    private ComentarioAyudaRepository comentarioAyudaRepository;

    @Autowired
    private HistorialAyudaRepository historialAyudaRepository;

    public List<ComentarioAyuda> obtenerPorHistorial(Long historialId) {
        return comentarioAyudaRepository.findByHistorialAyuda_Id(historialId);
    }

    @Transactional
    public ComentarioAyuda guardarComentario(Long historialId, Usuario autor, String comentario, TipoComentario tipo) {
        HistorialAyuda historial = historialAyudaRepository.findById(historialId)
                .orElseThrow(() -> new IllegalArgumentException("Historial de ayuda no encontrado"));

        ComentarioAyuda comentarioAyuda = new ComentarioAyuda(historial, autor, comentario, tipo);
        return comentarioAyudaRepository.save(comentarioAyuda);
    }
}
