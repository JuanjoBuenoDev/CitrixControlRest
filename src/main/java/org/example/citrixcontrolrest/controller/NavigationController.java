package org.example.citrixcontrolrest.controller;

import org.example.citrixcontrolrest.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class NavigationController {

    private final MainFrame mainFrame;
    private final JPanel mainPanel; // Panel con CardLayout
    private final CardLayout cardLayout;

    // Map: nombre -> JPanel
    private final Map<String, JPanel> panels;

    public NavigationController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.mainPanel = mainFrame.getMainPanel();
        this.cardLayout = mainFrame.getCardLayout();
        this.panels = new LinkedHashMap<>();
    }

    public void addPanel(String name, JPanel panel) {
        if (!panels.containsKey(name)) {
            panels.put(name, panel);
            mainPanel.add(panel, name);
            mainFrame.addNavButton(name); // Añadir botón al navBar
        }
    }

    public void removePanel(String name) {
        JPanel panel = panels.remove(name);
        if (panel != null) {
            mainPanel.remove(panel);
            mainFrame.removeNavButton(name);
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }

    public void showPanel(String name) {
        if (panels.containsKey(name)) {
            cardLayout.show(mainPanel, name);
            mainFrame.setPageTitle(name);
            mainFrame.updateNavSelection(name);
        }
    }

    public Map<String, JPanel> getPanels() {
        return panels;
    }
}

