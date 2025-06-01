package org.example.citrixcontrolrest.utils;

import javax.swing.*;
import java.awt.*;

public class LoadingDialog extends JDialog {
    public LoadingDialog(JFrame parent, String mensaje) {
        super(parent, "Cargando", true); // modal
        setLayout(new BorderLayout(10, 10));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        JLabel label = new JLabel(mensaje, JLabel.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));

        add(label, BorderLayout.NORTH);
        add(progressBar, BorderLayout.CENTER);

        setUndecorated(true); // sin bordes de ventana
        pack();
        setSize(250, 100);
        setLocationRelativeTo(parent);
    }
}

