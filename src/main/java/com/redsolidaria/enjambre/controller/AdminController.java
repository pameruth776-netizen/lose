package com.redsolidaria.enjambre.controller;

import com.redsolidaria.enjambre.dto.AdminDTO;
import com.redsolidaria.enjambre.model.Administrador;
import com.redsolidaria.enjambre.model.Usuario;
import com.redsolidaria.enjambre.model.HistorialAyuda;
import com.redsolidaria.enjambre.model.Incidencia;
import com.redsolidaria.enjambre.model.Sancion;
import com.redsolidaria.enjambre.repository.HistorialAyudaRepository;
import com.redsolidaria.enjambre.repository.IncidenciaRepository;
import com.redsolidaria.enjambre.repository.SancionRepository;
import com.redsolidaria.enjambre.repository.AdministradorRepository;
import com.redsolidaria.enjambre.service.UsuarioService;
import com.redsolidaria.enjambre.service.EmailService;
import com.redsolidaria.enjambre.service.UsuarioBloqueadoService;
import com.redsolidaria.enjambre.service.DonacionService;
import com.redsolidaria.enjambre.model.DonacionMonetaria;
import com.redsolidaria.enjambre.model.DonacionProducto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private HistorialAyudaRepository historialAyudaRepository;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Autowired
    private SancionRepository sancionRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private com.redsolidaria.enjambre.ws.AyudaConnectionRegistry ayudaConnectionRegistry;

    @Autowired
    private UsuarioBloqueadoService usuarioBloqueadoService;

    @Autowired
    private DonacionService donacionService;

    // ========== DASHBOARD ==========
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsuarios", usuarioService.listarTodosUsuarios().size());
        model.addAttribute("totalVoluntarios", usuarioService.listarVoluntarios().size());
        model.addAttribute("totalDiscapacitados", usuarioService.listarDiscapacitados().size());
        model.addAttribute("totalAdministradores", usuarioService.listarAdministradores().size());
        model.addAttribute("totalPendientes", usuarioService.listarUsuariosPendientes().size());
        return "admin/dashboardAdm";
    }

    @GetMapping("/informe/excel")
    public void descargarExcel(HttpSession session, HttpServletResponse response) throws IOException {
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !"ADMIN".equals(admin.getRol())) {
            response.sendRedirect("/login");
            return;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Reporte_Sistema_RedSolidaria.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            // Configurar fuentes y estilos
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // 1. Hoja: Resumen
            Sheet sheetResumen = workbook.createSheet("Resumen");
            sheetResumen.setDisplayGridlines(true);
            Row rowResumenHeader = sheetResumen.createRow(0);
            Cell cellResHeader1 = rowResumenHeader.createCell(0);
            cellResHeader1.setCellValue("Métrica");
            cellResHeader1.setCellStyle(headerStyle);
            Cell cellResHeader2 = rowResumenHeader.createCell(1);
            cellResHeader2.setCellValue("Valor");
            cellResHeader2.setCellStyle(headerStyle);

            String[][] resumenDatos = {
                {"Total Usuarios", String.valueOf(usuarioService.listarTodosUsuarios().size())},
                {"Voluntarios", String.valueOf(usuarioService.listarVoluntarios().size())},
                {"Personas con Discapacidad", String.valueOf(usuarioService.listarDiscapacitados().size())},
                {"Administradores", String.valueOf(usuarioService.listarAdministradores().size())},
                {"Cuentas Pendientes", String.valueOf(usuarioService.listarUsuariosPendientes().size())},
                {"Fecha de Generación", java.time.LocalDateTime.now().format(formatter)}
            };

            for (int i = 0; i < resumenDatos.length; i++) {
                Row row = sheetResumen.createRow(i + 1);
                row.createCell(0).setCellValue(resumenDatos[i][0]);
                Cell valCell = row.createCell(1);
                valCell.setCellValue(resumenDatos[i][1]);
                if (i < resumenDatos.length - 1) {
                    valCell.setCellStyle(centerStyle);
                }
            }
            sheetResumen.autoSizeColumn(0);
            sheetResumen.autoSizeColumn(1);

            // 2. Hoja: Usuarios
            Sheet sheetUsuarios = workbook.createSheet("Usuarios");
            sheetUsuarios.setDisplayGridlines(true);
            String[] headersUsuarios = {"ID", "Nombres", "Apellidos", "Email", "Rol", "Estado", "Verificado", "Fecha de Registro"};
            Row rowUserHeader = sheetUsuarios.createRow(0);
            for (int i = 0; i < headersUsuarios.length; i++) {
                Cell cell = rowUserHeader.createCell(i);
                cell.setCellValue(headersUsuarios[i]);
                cell.setCellStyle(headerStyle);
            }
            List<Usuario> usuarios = usuarioService.listarTodosUsuarios();
            int uRowIdx = 1;
            for (Usuario u : usuarios) {
                Row row = sheetUsuarios.createRow(uRowIdx++);
                row.createCell(0).setCellValue(u.getId());
                row.createCell(1).setCellValue(u.getNombres());
                row.createCell(2).setCellValue(u.getApellidos());
                row.createCell(3).setCellValue(u.getEmail());
                row.createCell(4).setCellValue(u.getRol());
                row.createCell(5).setCellValue(u.getEstado());
                row.createCell(6).setCellValue(u.isVerificado() ? "SÍ" : "NO");
                row.createCell(7).setCellValue(u.getFechaRegistro() != null ? u.getFechaRegistro().format(formatter) : "");
                for (int i = 0; i < headersUsuarios.length; i++) {
                    if (i == 0 || i == 4 || i == 5 || i == 6 || i == 7) {
                        row.getCell(i).setCellStyle(centerStyle);
                    }
                }
            }
            for (int i = 0; i < headersUsuarios.length; i++) {
                sheetUsuarios.autoSizeColumn(i);
            }

            // 3. Hoja: Voluntarios
            Sheet sheetVoluntarios = workbook.createSheet("Voluntarios");
            sheetVoluntarios.setDisplayGridlines(true);
            String[] headersVoluntarios = {"ID", "Nombres", "Apellidos", "Email", "Código Estudiante", "Carrera", "Puntos"};
            Row rowVolHeader = sheetVoluntarios.createRow(0);
            for (int i = 0; i < headersVoluntarios.length; i++) {
                Cell cell = rowVolHeader.createCell(i);
                cell.setCellValue(headersVoluntarios[i]);
                cell.setCellStyle(headerStyle);
            }
            List<com.redsolidaria.enjambre.model.Voluntario> voluntarios = usuarioService.listarVoluntarios();
            int vRowIdx = 1;
            for (com.redsolidaria.enjambre.model.Voluntario v : voluntarios) {
                Row row = sheetVoluntarios.createRow(vRowIdx++);
                row.createCell(0).setCellValue(v.getId());
                row.createCell(1).setCellValue(v.getNombres());
                row.createCell(2).setCellValue(v.getApellidos());
                row.createCell(3).setCellValue(v.getEmail());
                row.createCell(4).setCellValue(v.getCodigo());
                row.createCell(5).setCellValue(v.getCarrera());
                row.createCell(6).setCellValue(v.getPuntos());
                for (int i = 0; i < headersVoluntarios.length; i++) {
                    if (i == 0 || i == 4 || i == 6) {
                        row.getCell(i).setCellStyle(centerStyle);
                    }
                }
            }
            for (int i = 0; i < headersVoluntarios.length; i++) {
                sheetVoluntarios.autoSizeColumn(i);
            }

            // 4. Hoja: Donaciones Monetarias
            Sheet sheetDM = workbook.createSheet("Donaciones Monetarias");
            sheetDM.setDisplayGridlines(true);
            String[] headersDM = {"ID Donación", "Donante", "Email", "Celular", "Monto (S/.)", "Código Yape", "Estado", "Fecha Donación"};
            Row rowDMHeader = sheetDM.createRow(0);
            for (int i = 0; i < headersDM.length; i++) {
                Cell cell = rowDMHeader.createCell(i);
                cell.setCellValue(headersDM[i]);
                cell.setCellStyle(headerStyle);
            }
            List<DonacionMonetaria> monetarias = donacionService.obtenerTodasMonetarias();
            int mRowIdx = 1;
            for (DonacionMonetaria dm : monetarias) {
                Row row = sheetDM.createRow(mRowIdx++);
                row.createCell(0).setCellValue(dm.getId());
                row.createCell(1).setCellValue(dm.getNombreCompleto());
                row.createCell(2).setCellValue(dm.getEmail());
                row.createCell(3).setCellValue(dm.getCelular());
                row.createCell(4).setCellValue(dm.getMonto());
                row.createCell(5).setCellValue(dm.getCodigoYape() != null ? dm.getCodigoYape() : "");
                row.createCell(6).setCellValue(dm.getEstado());
                row.createCell(7).setCellValue(dm.getFechaDonacion() != null ? dm.getFechaDonacion().format(formatter) : "");
                for (int i = 0; i < headersDM.length; i++) {
                    if (i == 0 || i == 3 || i == 4 || i == 5 || i == 6 || i == 7) {
                        row.getCell(i).setCellStyle(centerStyle);
                    }
                }
            }
            for (int i = 0; i < headersDM.length; i++) {
                sheetDM.autoSizeColumn(i);
            }

            // 5. Hoja: Donaciones Productos
            Sheet sheetDP = workbook.createSheet("Donaciones Productos");
            sheetDP.setDisplayGridlines(true);
            String[] headersDP = {"ID Donación", "Donante", "Email", "Teléfono", "Tipo Producto", "Estado Producto", "Opción Entrega", "Dirección", "Horario", "Comentarios", "Estado", "Fecha Donación"};
            Row rowDPHeader = sheetDP.createRow(0);
            for (int i = 0; i < headersDP.length; i++) {
                Cell cell = rowDPHeader.createCell(i);
                cell.setCellValue(headersDP[i]);
                cell.setCellStyle(headerStyle);
            }
            List<DonacionProducto> productos = donacionService.obtenerTodasProductos();
            int pRowIdx = 1;
            for (DonacionProducto dp : productos) {
                Row row = sheetDP.createRow(pRowIdx++);
                row.createCell(0).setCellValue(dp.getId());
                row.createCell(1).setCellValue(dp.getNombreCompleto());
                row.createCell(2).setCellValue(dp.getEmail());
                row.createCell(3).setCellValue(dp.getTelefono());
                row.createCell(4).setCellValue(dp.getTipoProducto());
                row.createCell(5).setCellValue(dp.getEstadoProducto());
                row.createCell(6).setCellValue(dp.getOpcionEntrega());
                row.createCell(7).setCellValue(dp.getDireccion() != null ? dp.getDireccion() : "");
                row.createCell(8).setCellValue(dp.getHorario() != null ? dp.getHorario() : "");
                row.createCell(9).setCellValue(dp.getComentarios() != null ? dp.getComentarios() : "");
                row.createCell(10).setCellValue(dp.getEstado());
                row.createCell(11).setCellValue(dp.getFechaDonacion() != null ? dp.getFechaDonacion().format(formatter) : "");
                for (int i = 0; i < headersDP.length; i++) {
                    if (i == 0 || i == 3 || i == 6 || i == 10 || i == 11) {
                        row.getCell(i).setCellStyle(centerStyle);
                    }
                }
            }
            for (int i = 0; i < headersDP.length; i++) {
                sheetDP.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }
    
    // ========== GESTIÓN DE USUARIOS ==========
    
    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodosUsuarios());
        return "admin/usuarios";
    }
    
    @GetMapping("/voluntarios")
    public String voluntarios(Model model) {
        model.addAttribute("voluntarios", usuarioService.listarVoluntarios());
        model.addAttribute("voluntariosBloqueados", usuarioBloqueadoService.listarBloqueadosPorRol("VOLUNTARIO"));
        return "admin/voluntarios";
    }
    
    @GetMapping("/discapacitados")
    public String discapacitados(Model model) {
        model.addAttribute("discapacitados", usuarioService.listarDiscapacitados());
        model.addAttribute("discapacitadosBloqueados", usuarioBloqueadoService.listarBloqueadosPorRol("DISCAPACITADO"));
        return "admin/discapacitados";
    }
    
    // ========== GESTIÓN DE ADMINISTRADORES ==========
    
    @GetMapping("/administradores")
    public String administradores(Model model) {
        model.addAttribute("administradores", usuarioService.listarAdministradores());
        return "admin/administradores";
    }
    
    @GetMapping("/admin/nuevo")
    public String nuevoAdmin(Model model) {
        model.addAttribute("adminDTO", new AdminDTO());
        return "admin/admin-form";
    }
    
    @PostMapping("/admin/crear")
    public String crearAdmin(@Valid @ModelAttribute AdminDTO adminDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        
        if (!adminDTO.isPasswordMatching()) {
            result.rejectValue("confirmPassword", "error", "Las contraseñas no coinciden");
            return "admin/admin-form";
        }
        
        if (result.hasErrors()) {
            return "admin/admin-form";
        }
        
        try {
            usuarioService.registrarAdministrador(
                adminDTO.getNombres(),
                adminDTO.getApellidos(),
                adminDTO.getEmail(),
                adminDTO.getPassword()
            );
            redirectAttributes.addFlashAttribute("success", "✅ Administrador creado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/administradores";
    }
    
    @GetMapping("/admin/eliminar/{id}")
    public String eliminarAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarAdministrador(id);
            redirectAttributes.addFlashAttribute("success", "✅ Administrador eliminado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/administradores";
    }
    
    @GetMapping("/foro")
    public String foro() {
        return "admin/foro";
    }
    
    @GetMapping("/usuario/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String redirectTarget = "/admin/usuarios";
        try {
            Usuario usuario = usuarioService.buscarPorId(id);
            if (usuario != null) {
                if ("VOLUNTARIO".equals(usuario.getRol())) {
                    redirectTarget = "/admin/voluntarios";
                } else if ("DISCAPACITADO".equals(usuario.getRol())) {
                    redirectTarget = "/admin/discapacitados";
                }
            }
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("success", "✅ Usuario eliminado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + redirectTarget;
    }

    // ========== ACTIVACIÓN DE USUARIOS PENDIENTES ==========

    @GetMapping("/activacion")
    public String activacion(Model model) {
        model.addAttribute("usuariosPendientes", usuarioService.listarUsuariosPendientes());
        model.addAttribute("historialActivaciones", usuarioService.listarHistorialActivaciones());
        return "admin/activacion-usuarios";
    }

    @PostMapping("/usuarios/{id}/activar")
    public String activarUsuario(@PathVariable Long id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id);
            if (usuario == null) {
                redirectAttributes.addFlashAttribute("error", "❌ Usuario no encontrado");
                return "redirect:/admin/activacion";
            }
            Long adminId = null;
            Usuario adminSesion = (Usuario) session.getAttribute("usuario");
            if (adminSesion != null) {
                adminId = adminSesion.getId();
            }
            usuarioService.activarUsuario(id, adminId);
            emailService.enviarCorreoActivacion(usuario.getEmail());
            redirectAttributes.addFlashAttribute("success", "✅ Cuenta activada exitosamente y notificación enviada por correo");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error al activar la cuenta: " + e.getMessage());
        }
        return "redirect:/admin/activacion";
    }

    @PostMapping("/usuarios/{id}/rechazar")
    public String rechazarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id);
            if (usuario == null) {
                redirectAttributes.addFlashAttribute("error", "❌ Usuario no encontrado");
                return "redirect:/admin/activacion";
            }
            String email = usuario.getEmail();
            usuarioService.eliminarUsuario(id); // Elimina físicamente de la base de datos
            emailService.enviarCorreoRechazo(email);
            redirectAttributes.addFlashAttribute("success", "✅ Cuenta rechazada, eliminada de la base de datos y notificación enviada");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error al rechazar la cuenta: " + e.getMessage());
        }
        return "redirect:/admin/activacion";
    }

    // ========== GESTIÓN DE INCIDENCIAS ==========
    
    @GetMapping("/incidencias")
    public String incidencias(Model model, HttpSession session) {
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !"ADMIN".equals(admin.getRol())) {
            return "redirect:/login";
        }

        List<Incidencia> incidencias = incidenciaRepository.findAllByOrderByFechaCreacionDesc();
        // Transición automática a PROCESANDO al ser visualizadas por el admin
        for (Incidencia inc : incidencias) {
            if ("PENDIENTE".equals(inc.getEstado())) {
                inc.setEstado("PROCESANDO");
                incidenciaRepository.save(inc);
            }
        }
        model.addAttribute("incidencias", incidencias);

        Map<Long, List<Sancion>> sancionesPorUsuario = new HashMap<>();
        for (Incidencia h : incidencias) {
            if (h.getDenunciado() != null) {
                Long volId = h.getDenunciado().getId();
                sancionesPorUsuario.putIfAbsent(volId, sancionRepository.findByUsuario_Id(volId));
            }
            if (h.getDenunciante() != null) {
                Long discId = h.getDenunciante().getId();
                sancionesPorUsuario.putIfAbsent(discId, sancionRepository.findByUsuario_Id(discId));
            }
        }
        model.addAttribute("sancionesPorUsuario", sancionesPorUsuario);

        return "admin/incidencias";
    }

    @PostMapping("/incidencias/sancionar")
    public String sancionar(@RequestParam Long historialId,
                            @RequestParam Long reportedUserId,
                            @RequestParam String tipoSancion,
                            @RequestParam(required = false) String motivo,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !"ADMIN".equals(admin.getRol())) {
            return "redirect:/login";
        }

        try {
            Usuario reportedUser = usuarioService.buscarPorId(reportedUserId);
            if (reportedUser == null) {
                redirectAttributes.addFlashAttribute("error", "❌ Usuario reportado no encontrado");
                return "redirect:/admin/incidencias";
            }

            HistorialAyuda historial = historialAyudaRepository.findById(historialId)
                .orElseThrow(() -> new IllegalArgumentException("Historial no encontrado"));

            Administrador administrador = administradorRepository.findById(admin.getId())
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado"));

            Sancion sancion = new Sancion(reportedUser, historial, tipoSancion, motivo, administrador);
            sancionRepository.save(sancion);

            // Marcar las incidencias correspondientes como RESUELTO y notificar por correo al denunciante
            List<Incidencia> incidenciasAsociadas = incidenciaRepository.findByHistorialAyuda_IdAndDenunciado_Id(historialId, reportedUserId);
            String resolucionDetalles = "";
            if ("AVISO_1".equals(tipoSancion)) {
                resolucionDetalles = "Se ha aplicado un Primer Aviso de Advertencia al usuario reportado.";
            } else if ("AVISO_2".equals(tipoSancion)) {
                resolucionDetalles = "Se ha aplicado un Segundo Aviso de Advertencia al usuario reportado.";
            } else if ("BLOQUEO".equals(tipoSancion)) {
                if (motivo == null || motivo.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "❌ Debes indicar el motivo del bloqueo");
                    return "redirect:/admin/incidencias";
                }
                resolucionDetalles = "Se ha inhabilitado permanentemente la cuenta del usuario reportado por el siguiente motivo: " + motivo;
            }

            for (Incidencia inc : incidenciasAsociadas) {
                inc.setEstado("RESUELTO");
                incidenciaRepository.save(inc);
                if (inc.getDenunciante() != null) {
                    emailService.enviarResolucionIncidencia(
                        inc.getDenunciante().getEmail(),
                        inc.getDenunciante().getNombreCompleto(),
                        inc.getDenunciado().getNombreCompleto(),
                        resolucionDetalles
                    );

                    // Notificación WebSocket al denunciante
                    try {
                        java.util.Map<String, Object> wsPayload = new java.util.HashMap<>();
                        wsPayload.put("type", "INCIDENCIA_RESUELTA");
                        wsPayload.put("incidenciaId", inc.getId());
                        wsPayload.put("resolucion", "La resolución para tu reporte contra " + inc.getDenunciado().getNombreCompleto() + " es: " + resolucionDetalles);
                        ayudaConnectionRegistry.sendToUser(inc.getDenunciante().getId(), wsPayload);
                    } catch (Exception wsEx) {
                        System.err.println("[WS] Error enviando notificación de resolución: " + wsEx.getMessage());
                    }
                }
            }

            // Notificación WebSocket al denunciado (sancionado)
            try {
                java.util.Map<String, Object> wsPayload = new java.util.HashMap<>();
                wsPayload.put("type", "SANCION_RECIBIDA");
                wsPayload.put("tipoSancion", tipoSancion);
                wsPayload.put("motivo", "Has recibido una sanción (" + tipoSancion + ") por: " + (motivo != null && !motivo.isEmpty() ? motivo : resolucionDetalles));
                ayudaConnectionRegistry.sendToUser(reportedUserId, wsPayload);
            } catch (Exception wsEx) {
                System.err.println("[WS] Error enviando notificación de sanción: " + wsEx.getMessage());
            }

            boolean isVoluntario = "VOLUNTARIO".equals(reportedUser.getRol());

            if ("AVISO_1".equals(tipoSancion)) {
                emailService.enviarPrimerAvisoIncidencia(reportedUser.getEmail(), isVoluntario);
                redirectAttributes.addFlashAttribute("success", "✅ Primer aviso registrado, incidencia resuelta y notificaciones enviadas");
            } else if ("AVISO_2".equals(tipoSancion)) {
                emailService.enviarSegundoAvisoIncidencia(reportedUser.getEmail(), isVoluntario);
                redirectAttributes.addFlashAttribute("success", "✅ Segundo aviso registrado, incidencia resuelta y notificaciones enviadas");
            } else if ("BLOQUEO".equals(tipoSancion)) {
                usuarioBloqueadoService.registrarBloqueo(reportedUser, motivo, admin.getId());
                emailService.enviarBloqueoCuentaIncidencia(reportedUser.getEmail(), isVoluntario, motivo);
                usuarioService.eliminarUsuario(reportedUserId);
                redirectAttributes.addFlashAttribute("success", "🚫 Cuenta bloqueada permanentemente, registrada en lista de bloqueados, incidencia resuelta y notificaciones enviadas");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error al aplicar sanción: " + e.getMessage());
        }

        return "redirect:/admin/incidencias";
    }
}