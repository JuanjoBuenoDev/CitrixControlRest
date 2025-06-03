package org.example.citrixcontrolrest.ui;

import org.example.citrixcontrolrest.model.DgDTO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

import org.example.citrixcontrolrest.service.CitrixService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

public class DGPanel extends JPanel implements Refreshable {

    private final CitrixService citrixService;

    private JTable dgTable;
    private DefaultTableModel tableModel;
    private JPanel detailsPanel;
    private JFreeChart pieChart;
    private List<DgDTO> dgList;

    public DGPanel(CitrixService citrixService) {
        this.citrixService = citrixService;
        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        // Panel superior con tabla y gráfico
        JPanel topPanel = new JPanel(new BorderLayout());

        // Tabla de DGs
        initializeTable();
        JScrollPane tableScrollPane = new JScrollPane(dgTable);
        topPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Gráfico de torta
        JPanel chartPanel = createPieChartPanel();
        topPanel.add(chartPanel, BorderLayout.EAST);

        // Panel inferior con detalles
        detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Detalles del DG"));
        JScrollPane detailsScrollPane = new JScrollPane(detailsPanel);

        // Agregar componentes al panel principal
        add(topPanel, BorderLayout.NORTH);
        add(detailsScrollPane, BorderLayout.CENTER);

        // Listener para selección en la tabla
        dgTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showSelectedDGDetails();
            }
        });
    }

    private void initializeTable() {
        String[] columnNames = {"Nombre", "Estado", "Sesiones"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dgTable = new JTable(tableModel);
        dgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dgTable.getTableHeader().setReorderingAllowed(false);
    }

    private JPanel createPieChartPanel() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        pieChart = ChartFactory.createPieChart(
                "Top 10 DG por VDA",
                dataset,
                true, true, false
        );

        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new Dimension(400, 300));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createTitledBorder("Distribución de VDA"));
        return panel;
    }

    private void showSelectedDGDetails() {
        detailsPanel.removeAll();
        int selectedRow = dgTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < dgList.size()) {
            DgDTO selectedDG = dgList.get(selectedRow);

            // Panel principal para detalles (con BorderLayout)
            JPanel mainDetailsPanel = new JPanel(new BorderLayout(15, 15));
            mainDetailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Panel izquierdo - Datos del DG (sin cambios)
            JPanel dataPanel = new JPanel();
            dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
            dataPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 20));

            addDetailField(dataPanel, "UID:", selectedDG.getUID());
            addDetailField(dataPanel, "Nombre:", selectedDG.getName());
            addDetailField(dataPanel, "Estado:", selectedDG.getState());
            addDetailField(dataPanel, "Sesiones activas:", String.valueOf(selectedDG.getSessionCount()));
            addDetailField(dataPanel, "Índice de carga promedio:", selectedDG.getAverageLoadIndex());
            addDetailField(dataPanel, "Modo mantenimiento:", selectedDG.isMaintenanceMode() ? "Sí" : "No");
            addDetailField(dataPanel, "Reinicio habilitado:", selectedDG.isRebootEnabled() ? "Sí" : "No");
            addDetailField(dataPanel, "Frecuencia reinicio:", selectedDG.getRebootFrequency());
            addDetailField(dataPanel, "Días reinicio:", selectedDG.getRebootDaysOfWeek());
            addDetailField(dataPanel, "Hora inicio reinicio:", selectedDG.getRebootStartTime());
            addDetailField(dataPanel, "Duración reinicio:", selectedDG.getRebootDuration());

            // Panel derecho - Lista de VDA con mayor ancho
            JPanel vdaPanel = new JPanel(new BorderLayout());
            vdaPanel.setPreferredSize(new Dimension(350, 400)); // Ancho aumentado a 350px
            vdaPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Lista de VDAs (" + selectedDG.getVdas().size() + ")"),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            DefaultListModel<String> listModel = new DefaultListModel<>();
            selectedDG.getVdas().forEach(listModel::addElement);

            JList<String> vdaList = new JList<>(listModel);
            vdaList.setFont(new Font("Monospaced", Font.PLAIN, 13)); // Fuente monoespaciada
            vdaList.setVisibleRowCount(12);
            vdaList.setFixedCellWidth(300); // Ancho fijo para los items

            JScrollPane vdaScrollPane = new JScrollPane(vdaList);
            vdaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            vdaPanel.add(vdaScrollPane, BorderLayout.CENTER);

            // Añadir componentes al panel principal
            mainDetailsPanel.add(dataPanel, BorderLayout.CENTER);
            mainDetailsPanel.add(vdaPanel, BorderLayout.EAST);

            detailsPanel.add(mainDetailsPanel);
        }
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private void addDetailField(JPanel container, String label, String value) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        fieldPanel.add(new JLabel("<html><b>" + label + "</b></html>"));
        fieldPanel.add(new JLabel(value));
        container.add(fieldPanel);
    }

    private void refreshVdaList(DgDTO dg, DefaultListModel<String> listModel) {
        // Aquí iría la lógica para actualizar la lista de VDA
        listModel.clear();
        for (String vda : dg.getVdas()) {
            listModel.addElement(vda);
        }
    }

    private void addDetailField(String label, String value) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.add(new JLabel("<html><b>" + label + "</b></html>"));
        fieldPanel.add(new JLabel(value));
        detailsPanel.add(fieldPanel);
    }

    @Override
    public void refreshData() {
        // Aquí iría la lógica para obtener los datos actualizados
        dgList = citrixService.getDeliveryGroups().values().stream()
                .collect(Collectors.toList());

        // Actualizar tabla
        tableModel.setRowCount(0);
        for (DgDTO dg : dgList) {
            tableModel.addRow(new Object[]{
                    dg.getName(),
                    dg.getState(),
                    dg.getSessionCount()
            });
        }

        // Actualizar gráfico
        updatePieChart();
    }

    private void updatePieChart() {
        // Obtener el plot del gráfico y luego el dataset
        PiePlot plot = (PiePlot) pieChart.getPlot();
        DefaultPieDataset dataset = (DefaultPieDataset) plot.getDataset();
        dataset.clear();

        // Ordenar DGs por cantidad de VDA (descendente) y tomar los top 10
        dgList.sort((dg1, dg2) -> Integer.compare(dg2.getVdas().size(), dg1.getVdas().size()));

        int limit = Math.min(10, dgList.size());
        for (int i = 0; i < limit; i++) {
            DgDTO dg = dgList.get(i);
            dataset.setValue(dg.getName(), dg.getVdas().size());
        }
    }
}
