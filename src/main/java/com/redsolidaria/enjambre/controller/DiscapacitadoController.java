package com.redsolidaria.enjambre.controller;

import com.redsolidaria.enjambre.model.*;
import com.redsolidaria.enjambre.repository.HistorialAyudaRepository;
import com.redsolidaria.enjambre.service.CalificacionService;
import com.redsolidaria.enjambre.service.ComentarioService;
import com.redsolidaria.enjambre.service.IncidenciaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
@RequestMapping("/discapacitado")
public class DiscapacitadoController {

    @Autowired
    private HistorialAyudaRepository historialAyudaRepository;

    @Autowired
    private CalificacionService calificacionService;

    @Autowired
    private ComentarioService comentarioService;

    @Autowired
    private IncidenciaService incidenciaService;

    // Página principal del discapacitado (donde redirige después del login)
    @GetMapping("/inicio")
    public String inicio() {
        return "Users/Disca/botonAyuda";
    }
    
    @GetMapping("/ayuda")
    public String ayuda() {
        return "Users/Disca/botonAyuda";
    }
    
    @GetMapping("/donaciones")
    public String donaciones() {
        return "Users/Disca/donacionesDis";
    }
    
    @GetMapping("/foro")
    public String foro() {
        return "Users/Disca/foroDis";
    }
    
    @GetMapping("/historial")
    public String historial(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario != null) {
            model.addAttribute("historial", 
                historialAyudaRepository.findBySolicitud_Discapacitado_IdOrderByFechaFinalizacionDesc(usuario.getId()));
        }
        return "Users/Disca/historialAyuda";
    }

    @PostMapping("/api/calificar")
    @ResponseBody
    public ResponseEntity<?> calificar(@RequestParam Long historialId,
                                       @RequestParam TipoMedalla tipoMedalla,
                                       @RequestParam(required = false) String comentario,
                                       HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Sesión no válida"));
        }
        try {
            calificacionService.guardarCalificacion(historialId, tipoMedalla, comentario);
            return ResponseEntity.ok(Map.of("mensaje", "Calificación guardada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/comentar")
    @ResponseBody
    public ResponseEntity<?> comentar(@RequestParam Long historialId,
                                      @RequestParam String comentario,
                                      HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Sesión no válida"));
        }
        try {
            comentarioService.guardarComentario(historialId, usuario, comentario, TipoComentario.CONSEJO);
            return ResponseEntity.ok(Map.of("mensaje", "Comentario enviado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/incidencia")
    @ResponseBody
    public ResponseEntity<?> registrarIncidencia(@RequestParam Long historialId,
                                                 @RequestParam String descripcion,
                                                 @RequestParam(value = "evidencia", required = false) MultipartFile evidencia,
                                                 HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Sesión no válida"));
        }
        try {
            incidenciaService.guardarIncidencia(historialId, usuario, descripcion, evidencia);
            return ResponseEntity.ok(Map.of("mensaje", "Incidencia registrada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}