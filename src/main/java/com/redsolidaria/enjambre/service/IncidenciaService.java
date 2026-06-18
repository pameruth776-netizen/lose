package com.redsolidaria.enjambre.service;

import com.redsolidaria.enjambre.model.*;
import com.redsolidaria.enjambre.repository.HistorialAyudaRepository;
import com.redsolidaria.enjambre.repository.IncidenciaAyudaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class IncidenciaService {

    @Autowired
    private IncidenciaAyudaRepository incidenciaAyudaRepository;

    @Autowired
    private HistorialAyudaRepository historialAyudaRepository;

    public List<IncidenciaAyuda> listarTodas() {
        return incidenciaAyudaRepository.findAll();
    }

    public IncidenciaAyuda obtenerPorId(Long id) {
        return incidenciaAyudaRepository.findById(id).orElse(null);
    }

    @Transactional
    public IncidenciaAyuda guardarIncidencia(Long historialId, Usuario reportadoPor, String descripcion, MultipartFile archivo) throws IOException {
        HistorialAyuda historial = historialAyudaRepository.findById(historialId)
                .orElseThrow(() -> new IllegalArgumentException("Historial de ayuda no encontrado"));

        IncidenciaAyuda incidencia = new IncidenciaAyuda(historial, reportadoPor, descripcion);
        
        // Guardamos primero para obtener el ID para el nombre de archivo
        incidencia = incidenciaAyudaRepository.save(incidencia);

        if (archivo != null && !archivo.isEmpty()) {
            validarArchivo(archivo);

            String originalName = archivo.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
            }

            TipoEvidencia tipoEvidencia = determinarTipoEvidencia(archivo.getContentType(), extension);
            incidencia.setTipoEvidencia(tipoEvidencia);

            LocalDateTime ahora = LocalDateTime.now();
            String anio = String.valueOf(ahora.getYear());
            String mes = String.format("%02d", ahora.getMonthValue());
            String dia = String.format("%02d", ahora.getDayOfMonth());

            String nombreArchivo = generarNombreArchivo(incidencia.getId(), tipoEvidencia.name().toLowerCase(), extension);
            String rutaRelativa = "/uploads/incidencias/" + anio + "/" + mes + "/" + dia + "/" + nombreArchivo;

            Path rutaFisicaDir = Paths.get("uploads/incidencias", anio, mes, dia);
            if (!Files.exists(rutaFisicaDir)) {
                Files.createDirectories(rutaFisicaDir);
            }

            Path rutaFisicaCompleta = rutaFisicaDir.resolve(nombreArchivo);
            Files.write(rutaFisicaCompleta, archivo.getBytes());

            incidencia.setNombreArchivo(nombreArchivo);
            incidencia.setRutaArchivo(rutaRelativa);
            incidencia.setTipoArchivo(archivo.getContentType());
            incidencia.setTamanioArchivo(archivo.getSize());
        }

        return incidenciaAyudaRepository.save(incidencia);
    }

    @Transactional
    public IncidenciaAyuda cambiarEstado(Long id, EstadoIncidencia nuevoEstado, String resolucion) {
        IncidenciaAyuda incidencia = incidenciaAyudaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incidencia no encontrada"));
        incidencia.setEstado(nuevoEstado);
        if (resolucion != null) {
            incidencia.setResolucion(resolucion);
        }
        return incidenciaAyudaRepository.save(incidencia);
    }

    @Transactional
    public void eliminarIncidencia(Long id) {
        IncidenciaAyuda incidencia = incidenciaAyudaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incidencia no encontrada"));
        
        if (incidencia.getRutaArchivo() != null) {
            try {
                String rutaSinSlash = incidencia.getRutaArchivo().startsWith("/") ? 
                        incidencia.getRutaArchivo().substring(1) : incidencia.getRutaArchivo();
                Path fileToDelete = Paths.get(rutaSinSlash);
                Files.deleteIfExists(fileToDelete);
            } catch (IOException e) {
                System.err.println("No se pudo eliminar el archivo de evidencia físico: " + e.getMessage());
            }
        }
        incidenciaAyudaRepository.delete(incidencia);
    }

    public String generarNombreArchivo(Long idIncidencia, String tipo, String extension) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String fechaHora = LocalDateTime.now().format(dtf);
        return "incidencia_" + idIncidencia + "_" + tipo + "_" + fechaHora + "." + extension;
    }

    public String obtenerRutaCompleta(String anio, String mes, String dia, String nombreArchivo) {
        return "/uploads/incidencias/" + anio + "/" + mes + "/" + dia + "/" + nombreArchivo;
    }

    private void validarArchivo(MultipartFile archivo) {
        String contentType = archivo.getContentType();
        String originalName = archivo.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        }

        long size = archivo.getSize();

        if (contentType == null) {
            throw new IllegalArgumentException("El tipo de contenido del archivo es desconocido.");
        }

        if (contentType.startsWith("image/") || List.of("jpg", "jpeg", "png", "gif").contains(extension)) {
            if (size > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("La foto supera el tamaño máximo permitido de 5MB.");
            }
        }
        else if (contentType.startsWith("video/") || List.of("mp4", "avi", "mov").contains(extension)) {
            if (size > 20 * 1024 * 1024) {
                throw new IllegalArgumentException("El video supera el tamaño máximo permitido de 20MB.");
            }
        }
        else if (contentType.startsWith("audio/") || List.of("mp3", "wav", "m4a", "wma").contains(extension)) {
            if (size > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("El audio supera el tamaño máximo permitido de 10MB.");
            }
        } else {
            throw new IllegalArgumentException("Formato de archivo no soportado. Debe ser foto, video o audio.");
        }
    }

    private TipoEvidencia determinarTipoEvidencia(String contentType, String extension) {
        if (contentType != null) {
            if (contentType.startsWith("image/")) return TipoEvidencia.FOTO;
            if (contentType.startsWith("video/")) return TipoEvidencia.VIDEO;
            if (contentType.startsWith("audio/")) return TipoEvidencia.AUDIO;
        }
        if (List.of("jpg", "jpeg", "png", "gif").contains(extension)) return TipoEvidencia.FOTO;
        if (List.of("mp4", "avi", "mov").contains(extension)) return TipoEvidencia.VIDEO;
        if (List.of("mp3", "wav", "m4a").contains(extension)) return TipoEvidencia.AUDIO;
        return TipoEvidencia.FOTO;
    }
}
