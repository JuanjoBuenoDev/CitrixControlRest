package org.example.citrixcontrolrest.ui;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.citrixcontrolrest.service.CitrixService;
import org.example.citrixcontrolrest.utils.LoadingDialog;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private CitrixService citrixService;

    private JLabel pageTitle;
    private JButton refreshButton;

    // Para manejar botones de navegación dinámicos
    private JPanel navPanel;
    private JPanel bottomBar;
    private Map<String, JButton> navButtons = new HashMap<>();


    public MainFrame(CitrixService citrixService) {
        super("Citrix Control");
        this.citrixService = citrixService;
        initUI();
        initListeners();
    }

    private void initUI() {

        FlatLightLaf.install();

        Color lightBlue = new Color(173, 216, 230);

        // Barra superior
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(lightBlue);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        pageTitle = new JLabel("DDC", SwingConstants.CENTER);
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        pageTitle.setForeground(Color.BLACK);
        JPanel centerTitlePanel = new JPanel(new GridBagLayout());
        centerTitlePanel.setOpaque(false);
        centerTitlePanel.add(pageTitle);
        topBar.add(centerTitlePanel, BorderLayout.CENTER);

        refreshButton = new JButton("Refresh");
        topBar.add(refreshButton, BorderLayout.EAST);

        // Panel principal con CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Barra inferior con fondo celeste
        bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(lightBlue);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // margen horizontal mayor

        navPanel = new JPanel(new GridLayout(1, 7, 10, 0)); // empezamos con 7 columnas, pero luego se puede ajustar
        navPanel.setOpaque(false); // para que herede el fondo celeste de bottomBar

        bottomBar.add(navPanel, BorderLayout.CENTER);

        // Layout JFrame
        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        setSize(1650, 1050);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void setPageTitle(String title) {
        pageTitle.setText(title);
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    // Añade botón de navegación nuevo
    public void addNavButton(String name) {
        if (navButtons.containsKey(name)) return;
        JButton btn = new JButton(name);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 18));
        btn.setPreferredSize(new Dimension(0, 50));
        btn.addActionListener(e -> {
            cardLayout.show(mainPanel, name);
            setPageTitle(name);
            updateNavSelection(name);
        });
        navButtons.put(name, btn);
        navPanel.add(btn);

        // Ajustar GridLayout columnas dinámicamente
        navPanel.setLayout(new GridLayout(1, navButtons.size(), 10, 0));

        navPanel.revalidate();
        navPanel.repaint();
    }

    // Elimina botón de navegación
    public void removeNavButton(String name) {
        JButton btn = navButtons.remove(name);
        if (btn != null) {
            navPanel.remove(btn);
            navPanel.setLayout(new GridLayout(1, navButtons.size(), 10, 0));
            navPanel.revalidate();
            navPanel.repaint();
        }
    }

    // Opcional: resaltar botón activo
    public void updateNavSelection(String activeName) {
        for (var entry : navButtons.entrySet()) {
            entry.getValue().setEnabled(!entry.getKey().equals(activeName));
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public CardLayout getCardLayout() {
        return cardLayout;
    }

    private void initListeners() {
        refreshButton.addActionListener(e -> {
            LoadingDialog loading = new LoadingDialog(this, "Actualizando datos...");

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    citrixService.stopScheduler();
                    citrixService.iniciarScheduler();
                    return null;
                }

                @Override
                protected void done() {
                    loading.dispose();
                    try {
                        get();
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Datos actualizados correctamente.",
                                "Éxito",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Error al actualizar: " + ex.getCause().getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        System.out.println(ex);
                    }
                }
            };

            worker.execute();
            loading.setVisible(true);
        });
    }
}