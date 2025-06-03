package org.example.citrixcontrolrest.ui;

import org.example.citrixcontrolrest.model.UserDTO;
import org.example.citrixcontrolrest.service.CitrixService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class UserPanel extends JPanel implements Refreshable {

    private final CitrixService citrixService;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JPanel detailsPanel;
    private List<UserDTO> userList;

    public UserPanel(CitrixService citrixService) {
        this.citrixService = citrixService;
        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        // Panel superior con tabla
        JPanel topPanel = new JPanel(new BorderLayout());

        // Tabla de usuarios
        initializeTable();
        JScrollPane tableScrollPane = new JScrollPane(userTable);
        topPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Panel inferior con las tres listas
        detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Detalles del Usuario"));

        add(topPanel, BorderLayout.NORTH);
        add(detailsPanel, BorderLayout.CENTER);

        // Listener para selección en la tabla
        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showSelectedUserDetails();
            }
        });
    }

    private void initializeTable() {
        String[] columnNames = {"Usuario", "Última máquina usada", "Último error de conexión"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setReorderingAllowed(false);
    }

    private void showSelectedUserDetails() {
        detailsPanel.removeAll();
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < userList.size()) {
            UserDTO selectedUser = userList.get(selectedRow);

            // Panel para las tres listas
            JPanel listsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
            listsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Lista 1: Aplicaciones en uso
            JPanel appsPanel = createListPanel(
                    "Aplicaciones en uso (" + selectedUser.getAplicacionesEnUso().size() + ")",
                    selectedUser.getAplicacionesEnUso()
            );

            // Lista 2: Máquinas
            JPanel machinesPanel = createListPanel(
                    "Máquinas (" + selectedUser.getMaquinas().size() + ")",
                    selectedUser.getMaquinas()
            );

            // Lista 3: Desktop Groups
            JPanel dgPanel = createListPanel(
                    "Desktop Groups (" + selectedUser.getDesktopGroups().size() + ")",
                    selectedUser.getDesktopGroups()
            );

            listsPanel.add(appsPanel);
            listsPanel.add(machinesPanel);
            listsPanel.add(dgPanel);

            // Panel inferior con información adicional
            JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            infoPanel.add(new JLabel("<html><b>Último error:</b> " +
                    (selectedUser.getLastConnectionFailureReason() != null ?
                            selectedUser.getLastConnectionFailureReason() : "Ninguno") + "</html>"));
            infoPanel.add(new JLabel("<html><b>Fecha error:</b> " +
                    (selectedUser.getLastFailureEndTime() != null ?
                            selectedUser.getLastFailureEndTime() : "N/A") + "</html>"));

            // Añadir componentes al panel principal
            detailsPanel.add(listsPanel, BorderLayout.CENTER);
            detailsPanel.add(infoPanel, BorderLayout.SOUTH);
        }
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private JPanel createListPanel(String title, List<String> items) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        items.forEach(listModel::addElement);

        JList<String> itemList = new JList<>(listModel);
        itemList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(itemList);
        scrollPane.setPreferredSize(new Dimension(250, 200));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void refreshData() {
        // Obtener datos actualizados del servicio
        userList = citrixService.getActiveUsers().values().stream()
                .collect(Collectors.toList());

        // Actualizar tabla
        tableModel.setRowCount(0);
        for (UserDTO user : userList) {
            tableModel.addRow(new Object[]{
                    user.getUsername(),
                    user.getLastMachineUsed() != null ? user.getLastMachineUsed() : "N/A",
                    user.getLastConnectionFailureReason() != null ? user.getLastConnectionFailureReason() : "Ninguno"
            });
        }
    }
}
