package org.example.citrixcontrolrest.ui;

import lombok.SneakyThrows;
import org.example.citrixcontrolrest.controller.NavigationController;
import org.example.citrixcontrolrest.model.CitrixSiteDTO;
import org.example.citrixcontrolrest.model.Config;
import org.example.citrixcontrolrest.service.CitrixService;
import org.example.citrixcontrolrest.utils.LoadingDialog;
import org.example.citrixcontrolrest.utils.Serializar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class ConfigPanel extends JPanel {

    private JTextField siteNameField;
    private JTextField limitFailField;
    private JTextField ddcField;
    private JButton addDdcButton;
    private JButton removeDdcButton;
    private DefaultListModel<String> ddcListModel;
    private JList<String> ddcList;
    private JComboBox<String> siteSelector;
    private JButton acceptButton;
    private JButton deleteButton;

    private final CitrixService citrixService;
    private final MainFrame mainFrame;
    private final NavigationController navigationController;
    private final Serializar serializar = new Serializar();
    private List<Config> sites;

    public ConfigPanel(CitrixService citrixService, MainFrame mainFrame, NavigationController navigationController) {
        this.citrixService = citrixService;
        this.mainFrame = mainFrame;
        this.navigationController = navigationController;
        initUI();
    }

    private void initUI() {
        sites = serializar.leerSites();
        System.out.println(sites);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(25, 40, 25, 40));
        setBackground(UIManager.getColor("Panel.background"));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        topPanel.setBackground(getBackground());

        JLabel selectorLabel = new JLabel("Seleccionar configuración:");
        selectorLabel.setFont(selectorLabel.getFont().deriveFont(Font.BOLD, 14));

        siteSelector = new JComboBox<>();
        if (sites != null) {
            for (Config site : sites) {
                siteSelector.addItem(site.getName());
            }
            siteSelector.addItem("Nuevo Site");
        }

        siteSelector.addActionListener(e -> {
            String selected = (String) siteSelector.getSelectedItem();
            if (selected == null) return;

            if (selected.equals("Nuevo Site")) {
                // Campos vacíos para nuevo site
                siteNameField.setText("");
                limitFailField.setText("");
                ddcListModel.clear();
            } else {
                // Buscar el site correspondiente y rellenar los campos
                Config selectedSite = sites.stream()
                        .filter(site -> site.getName().equals(selected))
                        .findFirst()
                        .orElse(null);

                if (selectedSite != null) {
                    siteNameField.setText(selectedSite.getName());
                    limitFailField.setText(String.valueOf(selectedSite.getLimitFail()));
                    ddcListModel.clear();
                    for (String ddc : selectedSite.getDdcs()) {
                        ddcListModel.addElement(ddc);
                    }
                }
            }
        });


        customizeComboBox(siteSelector);
        siteSelector.setPreferredSize(new Dimension(300, 38));

        topPanel.add(selectorLabel);
        topPanel.add(siteSelector);

        return topPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        centerPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        centerPanel.setBackground(getBackground());

        centerPanel.add(createFormPanel());
        centerPanel.add(createListPanel());

        return centerPanel;
    }

    private JPanel createFormPanel() {
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBackground(getBackground());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(getBackground());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        siteNameField = createModernLabeledTextField(formPanel, "Nombre del Site:");
        setupSiteNameFieldValidation();

        limitFailField = createModernLabeledTextField(formPanel, "Limit Fail:");
        setupLimitFailFieldValidation();

        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(createDdcInputPanel());

        formWrapper.add(formPanel, BorderLayout.CENTER);
        return formWrapper;
    }

    private JPanel createDdcInputPanel() {
        JPanel ddcContainer = new JPanel();
        ddcContainer.setLayout(new BoxLayout(ddcContainer, BoxLayout.Y_AXIS));
        ddcContainer.setBackground(getBackground());
        ddcContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel ddcLabel = new JLabel("Nuevo DDC:");
        ddcLabel.setFont(ddcLabel.getFont().deriveFont(Font.BOLD, 13));
        ddcLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        ddcContainer.add(ddcLabel);
        ddcContainer.add(Box.createVerticalStrut(5));

        JPanel ddcInputPanel = new JPanel(new BorderLayout(8, 0));
        ddcInputPanel.setBackground(getBackground());
        ddcInputPanel.setMaximumSize(new Dimension(500, 38));

        ddcField = new JTextField();
        styleTextField(ddcField);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonsPanel.setOpaque(false);

        addDdcButton = createButton("Añadir", new Color(65, 131, 215), 38);
        removeDdcButton = createButton("Eliminar", new Color(220, 80, 80), 38);

        addDdcButton.addActionListener(this::addDdcAction);
        removeDdcButton.addActionListener(this::removeDdcAction);

        buttonsPanel.add(addDdcButton);
        buttonsPanel.add(removeDdcButton);

        ddcInputPanel.add(ddcField, BorderLayout.CENTER);
        ddcInputPanel.add(buttonsPanel, BorderLayout.EAST);

        ddcContainer.add(ddcInputPanel);
        return ddcContainer;
    }

    private JPanel createListPanel() {
        JPanel listWrapper = new JPanel(new BorderLayout());
        listWrapper.setBackground(getBackground());

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(getBackground());
        listPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        ddcListModel = new DefaultListModel<>();
        ddcList = new JList<>(ddcListModel);
        styleJList(ddcList);

        JScrollPane scrollPane = new JScrollPane(ddcList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        listPanel.add(scrollPane, BorderLayout.CENTER);

        listWrapper.add(listPanel, BorderLayout.CENTER);
        return listWrapper;
    }

    @SneakyThrows
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        bottomPanel.setBackground(getBackground());

        acceptButton = createButton("Aceptar", new Color(65, 131, 215), 42);
        acceptButton.addActionListener(this::handleAcceptAction);
        deleteButton = createButton("Eliminar", new Color(220, 80, 80), 42);
        deleteButton.addActionListener(this::deleteSiteAction);

        bottomPanel.add(acceptButton);
        bottomPanel.add(deleteButton);

        return bottomPanel;
    }

    private JButton createButton(String text, Color bgColor, int height) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setPreferredSize(new Dimension(120, height));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void addDdcAction(ActionEvent e) {
        String ddcText = ddcField.getText().trim();

        if (ddcText.isEmpty()) {
            showFieldError(ddcField);
            return;
        }

        ddcListModel.addElement(ddcText);
        ddcField.setText("");
        ddcField.requestFocusInWindow();
    }

    private void removeDdcAction(ActionEvent e) {
        int selectedIndex = ddcList.getSelectedIndex();
        if (selectedIndex != -1) {
            ddcListModel.remove(selectedIndex);
        } else {
            JOptionPane.showMessageDialog(mainFrame,
                    "Seleccione un DDC de la lista para eliminar",
                    "Ningún elemento seleccionado",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showFieldError(JTextField field) {
        field.setBackground(new Color(255, 230, 230));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 100, 100)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        Timer timer = new Timer(1000, ev -> {
            field.setBackground(Color.WHITE);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void styleJList(JList<String> list) {
        list.setVisibleRowCount(10);
        list.setBackground(UIManager.getColor("List.background"));
        list.setSelectionBackground(new Color(70, 130, 180));
        list.setFixedCellHeight(32);
        list.setFont(list.getFont().deriveFont(13f));
    }

    private void setupSiteNameFieldValidation() {
        ((AbstractDocument) siteNameField.getDocument()).setDocumentFilter(new DocumentFilter() {
            private final String regex = "^[a-zA-Z0-9\\sáéíóúÁÉÍÓÚñÑ]*$";

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string.matches(regex)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text.matches(regex)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    private void setupLimitFailFieldValidation() {
        ((AbstractDocument) limitFailField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
                if (newText.matches("\\d+") && Integer.parseInt(newText) > 0) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

                if (newText.isEmpty() || (newText.matches("\\d+") && Integer.parseInt(newText) > 0)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    private JTextField createModernLabeledTextField(JPanel parent, String labelText) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(getBackground());
        container.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField textField = new JTextField();
        styleTextField(textField);
        textField.setMaximumSize(new Dimension(500, 38));

        container.add(label);
        container.add(Box.createVerticalStrut(5));
        container.add(textField);

        parent.add(Box.createVerticalStrut(10));
        parent.add(container);

        return textField;
    }

    private void styleTextField(JTextField textField) {
        textField.setMaximumSize(new Dimension(500, 38));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        textField.setFont(textField.getFont().deriveFont(14f));
    }

    private void customizeComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(comboBox.getFont().deriveFont(14f));
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
    }

    private void handleAcceptAction(ActionEvent e) {
        String siteName = siteNameField.getText().trim();
        String limitFail = limitFailField.getText().trim();

        if (siteName.isEmpty() || limitFail.isEmpty() || ddcListModel.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Todos los campos son obligatorios (Site Name, Limit Fail y al menos un DDC).",
                    "Campos vacíos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> ddcs = new ArrayList<>();
        for (int i = 0; i < ddcListModel.size(); i++) {
            ddcs.add(ddcListModel.get(i));
        }

        LoadingDialog loading = new LoadingDialog(mainFrame, "Cargando información...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    // Validar DDCs y refrescar info en hilo background
                    boolean valid = citrixService.firstDDCs(ddcs.get(0));
                    if (!valid) {
                        return false;
                    }

                    if (siteSelector.getSelectedItem() != null && siteSelector.getSelectedItem().equals("Nuevo Site")) {
                        Config config = new Config();
                        config.setName(siteName);
                        config.setLimitFail(Integer.parseInt(limitFail));
                        config.setDdcs(ddcs);
                        System.out.println(config);
                        sites.add(config);
                        System.out.println(sites);
                        serializar.guardarSites(sites);
                    } else {
                        Config config = sites.get(siteSelector.getSelectedIndex());
                        config.setName(siteName);
                        config.setLimitFail(Integer.parseInt(limitFail));
                        config.setDdcs(ddcs);
                        sites.set(siteSelector.getSelectedIndex(), config);
                        serializar.guardarSites(sites);
                    }

                    citrixService.refreshCitrixSite(ddcs.get(0));
                    citrixService.iniciarScheduler();

                    return true;
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                    // Puedes guardar el error para mostrarlo luego
                    return null;
                }
            }

            @Override
            protected void done() {
                loading.dispose(); // Cierra el diálogo modal

                try {
                    Boolean success = get();

                    if (success == null) {
                        JOptionPane.showMessageDialog(mainFrame,
                                "Error al actualizar los DDCs (excepción).",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    } else if (!success) {
                        JOptionPane.showMessageDialog(mainFrame,
                                "Alguno de los DDCs no es válido.",
                                "DDC no válido",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(mainFrame,
                                "DDCs actualizados correctamente.",
                                "Éxito",
                                JOptionPane.INFORMATION_MESSAGE);

                        // Cambiar pantalla solo si todo fue OK
                        navigationController.showPanel("SITE");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(mainFrame,
                            "Error inesperado: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        loading.setVisible(true); // Muestra el diálogo modal (bloquea la UI)
    }


    public void deleteSiteAction(ActionEvent e) {
        int selectedIndex = siteSelector.getSelectedIndex();
        if (selectedIndex != -1 && !siteSelector.getSelectedItem().toString().equals("Nuevo Site")) {
            sites.remove(selectedIndex);
            serializar.guardarSites(sites);
            siteSelector.remove(selectedIndex);
        } else {
            JOptionPane.showMessageDialog(mainFrame,
                    "Seleccione un sitio de la lista para eliminar",
                    "Ningún elemento seleccionado",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}