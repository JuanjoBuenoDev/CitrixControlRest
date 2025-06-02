package org.example.citrixcontrolrest.utils;

import javax.swing.*;
import java.awt.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class ToastNotifier {

    public enum ToastType {
        SUCCESS, ERROR, INFO, WARNING
    }

    // Mostrar el toast + enviar correo si es tipo ERROR
    public static void showToast(String message, ToastType type, int durationMillis) {
        showToastVisual(message, type, durationMillis);

//        if (type == ToastType.ERROR) {
//            sendErrorEmail(message);
//        }
    }

    private static final java.util.List<JWindow> activeToasts = new java.util.ArrayList<>();

    private static void showToastVisual(String message, ToastType type, int durationMillis) {
        SwingUtilities.invokeLater(() -> {
            JWindow toast = new JWindow();

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            panel.setBackground(getBackgroundColor(type));

            JLabel iconLabel = new JLabel(getIcon(type));
            panel.add(iconLabel, BorderLayout.WEST);

            JLabel messageLabel = new JLabel(message);
            messageLabel.setForeground(Color.WHITE);
            messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            panel.add(messageLabel, BorderLayout.CENTER);

            toast.add(panel);
            toast.pack();

            Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration().getBounds();

            int baseX = screenBounds.x + screenBounds.width - toast.getWidth() - 30;

            int verticalOffset;
            synchronized (activeToasts) {
                verticalOffset = activeToasts.size() * (toast.getHeight() + 10);
                activeToasts.add(toast);
            }

            int y = screenBounds.y + screenBounds.height - toast.getHeight() - 50 - verticalOffset;
            toast.setLocation(baseX, y);

            toast.setAlwaysOnTop(true);
            toast.setVisible(true);
            toast.setOpacity(1f);

            Timer fadeTimer = new Timer(50, null);
            final float[] opacity = {1.0f};
            int[] elapsed = {0};

            fadeTimer.addActionListener(e -> {
                elapsed[0] += 50;
                if (elapsed[0] >= durationMillis) {
                    opacity[0] -= 0.05f;
                    if (opacity[0] <= 0f) {
                        toast.setVisible(false);
                        toast.dispose();
                        fadeTimer.stop();

                        synchronized (activeToasts) {
                            activeToasts.remove(toast);
                            repositionToasts();
                        }
                    } else {
                        toast.setOpacity(opacity[0]);
                    }
                }
            });

            fadeTimer.start();
        });
    }

    private static void repositionToasts() {
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration().getBounds();

        int baseX, baseY;
        synchronized (activeToasts) {
            int index = 0;
            for (JWindow toast : activeToasts) {
                int height = toast.getHeight();
                baseX = screenBounds.x + screenBounds.width - toast.getWidth() - 30;
                baseY = screenBounds.y + screenBounds.height - height - 50 - (index * (height + 10));
                toast.setLocation(baseX, baseY);
                index++;
            }
        }
    }




    private static Color getBackgroundColor(ToastType type) {
        switch (type) {
            case SUCCESS: return new Color(76, 175, 80);     // Verde
            case ERROR: return new Color(244, 67, 54);        // Rojo
            case INFO: return new Color(33, 150, 243);        // Azul
            case WARNING: return new Color(255, 152, 0);      // Naranja
            default: return Color.GRAY;
        }
    }

    private static Icon getIcon(ToastType type) {
        UIManager.put("OptionPane.messageFont", new Font("SansSerif", Font.BOLD, 16));
        switch (type) {
            case SUCCESS: return UIManager.getIcon("OptionPane.informationIcon");
            case ERROR: return UIManager.getIcon("OptionPane.errorIcon");
            case INFO: return UIManager.getIcon("OptionPane.informationIcon");
            case WARNING: return UIManager.getIcon("OptionPane.warningIcon");
            default: return null;
        }
    }

    // Enviar correo si el tipo es ERROR
    //Pendiente de incluir autentificacion para enviar correo
    private static void sendErrorEmail(String message) {
        // Configuración simple de ejemplo
        final String from = "notificaciones@app.com";
        final String to = "admin@empresa.com";
        final String host = "smtp.gmail.com";
        final String username = "tuemail@gmail.com";
        final String password = "tu_clave_o_token_app";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject("🚨 Notificación Crítica en Citrix");
            msg.setText("Ha ocurrido un error crítico:\n\n" + message);
            Transport.send(msg);
        } catch (MessagingException e) {
            e.printStackTrace(); // O también podrías mostrar otro toast de error
        }
    }
}
