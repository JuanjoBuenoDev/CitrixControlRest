package org.example.citrixcontrolrest.ui;

import org.example.citrixcontrolrest.model.AppDTO;
import org.example.citrixcontrolrest.service.CitrixService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class APPPanel extends JPanel implements Refreshable {

    private final CitrixService citrixService;
    private JTable appTable;
    private DefaultTableModel tableModel;
    private JPanel detailsPanel;
    private JFreeChart barChart;
    private List<AppDTO> appList;

    public APPPanel(CitrixService citrixService) {
        this.citrixService = citrixService;
        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        // Panel superior con tabla y gráfico
        JPanel topPanel = new JPanel(new BorderLayout());

        // Tabla de aplicaciones
        initializeTable();
        JScrollPane tableScrollPane = new JScrollPane(appTable);
        topPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Gráfico de barras
        JPanel chartPanel = createBarChartPanel();
        topPanel.add(chartPanel, BorderLayout.EAST);

        // Panel inferior con detalles
        detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Detalles de la Aplicación"));
        JScrollPane detailsScrollPane = new JScrollPane(detailsPanel);

        // Agregar componentes al panel principal
        add(topPanel, BorderLayout.NORTH);
        add(detailsScrollPane, BorderLayout.CENTER);

        // Listener para selección en la tabla
        appTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showSelectedAppDetails();
            }
        });
    }

    private void initializeTable() {
        String[] columnNames = {"Nombre", "Carpeta Usuario", "Habilitada"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appTable = new JTable(tableModel);
        appTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appTable.getTableHeader().setReorderingAllowed(false);
    }

    private JPanel createBarChartPanel() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        barChart = ChartFactory.createBarChart(
                "Top 10 Apps por Uso",
                "Aplicación",
                "Cantidad",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // Personalizar colores y espaciado de barras
        CategoryPlot plot = barChart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(79, 129, 189)); // Azul para VDAs
        renderer.setSeriesPaint(1, new Color(155, 187, 89));  // Verde para usuarios activos
        renderer.setItemMargin(-0.5); // Barras más juntas para la misma categoría

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(500, 300));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createTitledBorder("Distribución de Uso"));
        return panel;
    }

    private void showSelectedAppDetails() {
        detailsPanel.removeAll();
        int selectedRow = appTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < appList.size()) {
            AppDTO selectedApp = appList.get(selectedRow);

            // Panel principal para detalles (con BorderLayout)
            JPanel mainDetailsPanel = new JPanel(new BorderLayout(10, 10));
            mainDetailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Panel izquierdo - Datos básicos de la aplicación
            JPanel dataPanel = new JPanel();
            dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
            dataPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 20));

            addDetailField(dataPanel, "UID:", selectedApp.getUid());
            addDetailField(dataPanel, "Nombre:", selectedApp.getName());
            addDetailField(dataPanel, "Nombre aplicación:", selectedApp.getApplicationName());
            addDetailField(dataPanel, "Nombre publicado:", selectedApp.getPublishedName());
            addDetailField(dataPanel, "Ruta ejecutable:", selectedApp.getExecutablePath());
            addDetailField(dataPanel, "Argumentos:", selectedApp.getCommandLineArguments());
            addDetailField(dataPanel, "Habilitada:", selectedApp.getEnabled() ? "Sí" : "No");
            addDetailField(dataPanel, "Instancias máx. totales:", String.valueOf(selectedApp.getMaxTotalInstances()));
            addDetailField(dataPanel, "Instancias máx. por usuario:", String.valueOf(selectedApp.getMaxPerUserInstances()));

            // Panel derecho - Tres listas en un panel con GridLayout (1 fila x 3 columnas)
            JPanel listsPanel = new JPanel(new GridLayout(1, 3, 10, 0)); // 1 fila, 3 columnas, espacio horizontal 10
            listsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

            // Lista 1: Desktop Groups
            JPanel dgPanel = createListPanel("Desktop Groups (" + selectedApp.getDesktopGroups().size() + ")",
                    selectedApp.getDesktopGroups());

            // Lista 2: VDAs
            JPanel vdaPanel = createListPanel("VDAs (" + selectedApp.getVdas().size() + ")",
                    selectedApp.getVdas());

            // Lista 3: Usuarios activos
            JPanel usersPanel = createListPanel("Usuarios activos (" + selectedApp.getActiveUsers().size() + ")",
                    selectedApp.getActiveUsers());

            listsPanel.add(dgPanel);
            listsPanel.add(vdaPanel);
            listsPanel.add(usersPanel);

            // Añadir componentes al panel principal
            mainDetailsPanel.add(dataPanel, BorderLayout.CENTER);
            mainDetailsPanel.add(listsPanel, BorderLayout.EAST);

            detailsPanel.add(mainDetailsPanel);
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
        panel.setPreferredSize(new Dimension(200, 300)); // Mismo ancho para las tres listas

        DefaultListModel<String> listModel = new DefaultListModel<>();
        items.forEach(listModel::addElement);

        JList<String> itemList = new JList<>(listModel);
        itemList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(itemList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void addDetailField(JPanel container, String label, String value) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        fieldPanel.add(new JLabel("<html><b>" + label + "</b></html>"));
        fieldPanel.add(new JLabel(value != null ? value : "N/A"));
        container.add(fieldPanel);
    }

    @Override
    public void refreshData() {
        // Obtener datos actualizados del servicio
        appList = citrixService.getApps().values().stream()
                .collect(Collectors.toList());

        // Actualizar tabla
        tableModel.setRowCount(0);
        for (AppDTO app : appList) {
            tableModel.addRow(new Object[]{
                    app.getPublishedName(),
                    app.getUserFolder(),
                    app.getEnabled() ? "Sí" : "No"
            });
        }

        // Actualizar gráfico de barras
        updateBarChart();
    }

    private void updateBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Ordenar aplicaciones por cantidad de usuarios activos (descendente) y tomar top 10
        List<AppDTO> topApps = appList.stream()
                .sorted(Comparator.comparingInt(app -> -app.getActiveUsers().size()))
                .limit(10)
                .collect(Collectors.toList());

        // Agregar datos al dataset: VDAs y Usuarios activos por cada app
        for (AppDTO app : topApps) {
            String appName = app.getPublishedName();
            dataset.addValue(app.getVdas().size(), "VDAs", appName);
            dataset.addValue(app.getActiveUsers().size(), "Usuarios Activos", appName);
        }

        // Actualizar el dataset del gráfico
        CategoryPlot plot = (CategoryPlot) barChart.getPlot();
        plot.setDataset(dataset);
    }
}