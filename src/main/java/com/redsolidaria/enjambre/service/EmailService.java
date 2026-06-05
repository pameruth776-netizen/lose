package com.redsolidaria.enjambre.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;

    private String getFormattedFrom() {
        return "Red Solidaria UTP <" + emailFrom + ">";
    }

    @Async
    public void enviarCodigoVerificacion(String emailDestino, String codigo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getFormattedFrom());
        message.setTo(emailDestino);
        message.setSubject("Codigo de verificacion - Red Solidaria UTP");
        message.setText("Hola,\n\nTu codigo de verificacion es: " + codigo +
                        "\n\nEste codigo expira en 10 minutos.\n\n" +
                        "Si no solicitaste este codigo, ignora este mensaje.\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        try {
            mailSender.send(message);
            System.out.println("✓ Correo enviado a: " + emailDestino + " | Código: " + codigo);
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar correo de verificación a " + emailDestino + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void enviarCorreoActivacion(String emailDestino) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getFormattedFrom());
        message.setTo(emailDestino);
        message.setSubject("Tu cuenta ha sido activada - Red Solidaria UTP");
        message.setText("Hola,\n\nTu cuenta ha sido activada con exito. Ya puedes iniciar sesion en la plataforma.\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        try {
            mailSender.send(message);
            System.out.println("✓ Correo de activación enviado a: " + emailDestino);
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar correo de activación a " + emailDestino + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void enviarCorreoRechazo(String emailDestino) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getFormattedFrom());
        message.setTo(emailDestino);
        message.setSubject("❌ Tu cuenta no fue activada - Red Solidaria UTP");
        message.setText("Hola,\n\nTu cuenta no fue activada porque no cumple los requisitos. Puedes volver a registrarte corrigiendo la información.\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        try {
            mailSender.send(message);
            System.out.println("✓ Correo de rechazo enviado a: " + emailDestino);
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar correo de rechazo a " + emailDestino + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void enviarConfirmacionMonetaria(String emailDestino, String nombre, Double monto) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getFormattedFrom());
        message.setTo(emailDestino);
        message.setSubject("💖 ¡Tu donación monetaria ha sido confirmada! - Red Solidaria UTP");
        message.setText("Hola " + nombre + ",\n\n" +
                        "Queremos agradecerte de todo corazón por tu generosa donación monetaria de S/. " + String.format("%.2f", monto) + ".\n" +
                        "Tu contribución ha sido verificada y confirmada con éxito. Gracias a ti, podremos seguir brindando apoyo y adquiriendo productos de primera necesidad para quienes más lo necesitan.\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        try {
            mailSender.send(message);
            System.out.println("✓ Correo de confirmación monetaria enviado a: " + emailDestino + " | Monto: S/. " + monto);
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar correo de confirmación monetaria a " + emailDestino + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void enviarRechazoMonetaria(String emailDestino, String nombre) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getFormattedFrom());
        message.setTo(emailDestino);
        message.setSubject("⚠️ Actualización sobre tu donación monetaria - Red Solidaria UTP");
        message.setText("Hola " + nombre + ",\n\n" +
                        "Lamentamos informarte que no hemos podido verificar el código de tu donación monetaria.\n" +
                        "Por este motivo, la donación ha sido marcada como rechazada. Si crees que se trata de un error, por favor ponte en contacto con nosotros o intenta registrarla nuevamente.\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        try {
            mailSender.send(message);
            System.out.println("✓ Correo de rechazo monetaria enviado a: " + emailDestino);
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar correo de rechazo monetaria a " + emailDestino + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void enviarConfirmacionProductoRecoger(String emailDestino, String nombre, String producto, String horario) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getFormattedFrom());
        message.setTo(emailDestino);
        message.setSubject("📦 ¡Tu donación de producto ha sido aprobada! (Recojo en domicilio) - Red Solidaria UTP");
        message.setText("Hola " + nombre + ",\n\n" +
                        "Nos alegra informarte que tu donación de producto (" + producto + ") ha sido aprobada.\n" +
                        "Hemos coordinado la entrega bajo la opción de: Recoger en domicilio.\n" +
                        "Un miembro de nuestro equipo se acercará a la dirección proporcionada dentro del horario seleccionado:\n" +
                        "⏰ Horario de recojo: " + horario + "\n\n" +
                        "Por favor, ten el producto listo. ¡Muchas gracias por tu valioso apoyo!\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        try {
            mailSender.send(message);
            System.out.println("✓ Correo de recojo de producto enviado a: " + emailDestino + " | Producto: " + producto);
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar correo de recojo de producto a " + emailDestino + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void enviarConfirmacionProductoLlevar(String emailDestino, String nombre, String producto, String direccionSede, String horarioAtencion) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getFormattedFrom());
        message.setTo(emailDestino);
        message.setSubject("📦 ¡Tu donación de producto ha sido aprobada! (Llevar a sede) - Red Solidaria UTP");
        message.setText("Hola " + nombre + ",\n\n" +
                        "Nos alegra informarte que tu donación de producto (" + producto + ") ha sido aprobada.\n" +
                        "Puedes acercarte a nuestra sede para realizar la entrega:\n" +
                        "📍 Dirección de la sede: " + direccionSede + "\n" +
                        "⏰ Horario de atención: " + horarioAtencion + "\n\n" +
                        "¡Muchas gracias por tu valioso apoyo para nuestra comunidad!\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        try {
            mailSender.send(message);
            System.out.println("✓ Correo de entrega de producto en sede enviado a: " + emailDestino + " | Producto: " + producto);
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar correo de entrega en sede a " + emailDestino + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void enviarRechazoProducto(String emailDestino, String nombre) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getFormattedFrom());
        message.setTo(emailDestino);
        message.setSubject("⚠️ Actualización sobre tu donación de producto - Red Solidaria UTP");
        message.setText("Hola " + nombre + ",\n\n" +
                        "Agradecemos enormemente tu intención de donar.\n" +
                        "Lamentablemente, en esta ocasión no podemos recibir el producto propuesto debido a políticas internas o falta de capacidad de almacenamiento para este tipo de implemento.\n" +
                        "Esperamos poder contar con tu ayuda en futuras oportunidades.\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        try {
            mailSender.send(message);
            System.out.println("✓ Correo de rechazo producto enviado a: " + emailDestino);
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar correo de rechazo de producto a " + emailDestino + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}