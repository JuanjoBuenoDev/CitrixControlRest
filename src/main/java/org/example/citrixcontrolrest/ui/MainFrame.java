package org.example.citrixcontrolrest.ui;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.citrixcontrolrest.service.CitrixService;
import org.example.citrixcontrolrest.utils.LoadingDialog;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private final CitrixService citrixService;

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final JLabel pageTitle;
    private final JButton refreshButton;

    private final JPanel navPanel;
    private final JPanel bottomBar;
    private final Map<String, JButton> navButtons = new HashMap<>();
    private final Map<String, JPanel> panelMap = new HashMap<>();

    public MainFrame(CitrixService citrixService) {
        super("Citrix Control");
        this.citrixService = citrixService;
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);

        FlatLightLaf.install();
        Color lightBlue = new Color(173, 216, 230);

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(lightBlue);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        pageTitle = new JLabel("", SwingConstants.CENTER);
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        pageTitle.setForeground(Color.BLACK);

        JPanel centerTitlePanel = new JPanel(new GridBagLayout());
        centerTitlePanel.setOpaque(false);
        centerTitlePanel.add(pageTitle);
        topBar.add(centerTitlePanel, BorderLayout.CENTER);

        refreshButton = new JButton("Refresh");
        topBar.add(refreshButton, BorderLayout.EAST);

        // Bottom bar (nav)
        bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(lightBlue);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        navPanel = new JPanel(new GridLayout(1, 1, 10, 0)); // ajustado dinámicamente
        navPanel.setOpaque(false);
        bottomBar.add(navPanel, BorderLayout.CENTER);

        // Frame layout
        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        setSize(1650, 1050);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initListeners();
    }

    private void initListeners() {
        refreshButton.addActionListener(e -> {
            LoadingDialog loading = new LoadingDialog(this, "Actualizando datos...");
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    citrixService.reiniciarScheduler();
                    return null;
                }

                @Override
                protected void done() {
                    loading.dispose();
                    try {
                        get();
                        refreshAllPanels();
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

    public void addPanel(String name, JPanel panel) {
        if (!panelMap.containsKey(name)) {
            panelMap.put(name, panel);
            mainPanel.add(panel, name);
            addNavButton(name);
        }
    }

    public void showPanel(String name) {
        if (!panelMap.containsKey(name)) return;
        cardLayout.show(mainPanel, name);
        setPageTitle(name);
        updateNavSelection(name);
    }

    private void addNavButton(String name) {
        if (navButtons.containsKey(name)) return;
        JButton btn = new JButton(name);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 18));
        btn.setPreferredSize(new Dimension(0, 50));
        btn.addActionListener(e -> showPanel(name));
        navButtons.put(name, btn);
        navPanel.add(btn);

        navPanel.setLayout(new GridLayout(1, navButtons.size(), 10, 0));
        navPanel.revalidate();
        navPanel.repaint();
    }

    private void updateNavSelection(String activeName) {
        for (var entry : navButtons.entrySet()) {
            entry.getValue().setEnabled(!entry.getKey().equals(activeName));
        }
    }

    public void refreshAllPanels() {
        for (JPanel panel : panelMap.values()) {
            if (panel instanceof Refreshable) {
                ((Refreshable) panel).refreshData();
            }
        }
    }

    public void setPageTitle(String title) {
        pageTitle.setText(title);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public CardLayout getCardLayout() {
        return cardLayout;
    }
}


