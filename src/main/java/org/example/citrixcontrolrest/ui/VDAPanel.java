package org.example.citrixcontrolrest.ui;

import org.example.citrixcontrolrest.model.VdaDTO;
import org.example.citrixcontrolrest.service.CitrixService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

public class VDAPanel extends JPanel implements Refreshable {

    private final CitrixService citrixService;
    private JTable vdaTable;
    private DefaultTableModel tableModel;
    private JPanel detailsPanel;
    private JFreeChart barChart;
    private List<VdaDTO> vdaList;

    public VDAPanel(CitrixService citrixService) {
        this.citrixService = citrixService;
        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        // Panel superior con tabla y gráfico
        JPanel topPanel = new JPanel(new BorderLayout());

        // Tabla de VDAs
        initializeTable();
        JScrollPane tableScrollPane = new JScrollPane(vdaTable);
        topPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Gráfico de barras
        JPanel chartPanel = createBarChartPanel();
        topPanel.add(chartPanel, BorderLayout.EAST);

        // Panel inferior con detalles
        detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Detalles del VDA"));
        JScrollPane detailsScrollPane = new JScrollPane(detailsPanel);

        // Agregar componentes al panel principal
        add(topPanel, BorderLayout.NORTH);
        add(detailsScrollPane, BorderLayout.CENTER);

        // Listener para selección en la tabla
        vdaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showSelectedVDADetails();
            }
        });
    }

    private void initializeTable() {
        String[] columnNames = {"Machine Name", "Catalog Name", "Registration State", "Maintenance Mode"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        vdaTable = new JTable(tableModel);
        vdaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vdaTable.getTableHeader().setReorderingAllowed(false);
    }

    private JPanel createBarChartPanel() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        barChart = ChartFactory.createBarChart(
                "Top 10 VDA por Load Index",
                "VDA Machine Name",
                "Load Index",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // Obtener el CategoryPlot del gráfico
        CategoryPlot plot = (CategoryPlot) barChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);

        // Configurar el generador de tooltips personalizado con HTML
        plot.getRenderer().setDefaultToolTipGenerator(new CategoryToolTipGenerator() {
            @Override
            public String generateToolTip(CategoryDataset dataset, int row, int column) {
                String machineName = (String) dataset.getColumnKey(column);
                Number value = dataset.getValue(row, column);
                return "<html><b>" + machineName + "</b><br>Load Index: " + value + "</html>";
            }
        });

        ChartPanel chartPanel = new ChartPanel(barChart) {
            @Override
            public String getToolTipText(MouseEvent e) {
                ChartEntity entity = getEntityForPoint(e.getX(), e.getY());
                if (entity != null && entity instanceof CategoryItemEntity) {
                    return entity.getToolTipText();
                }
                return super.getToolTipText(e);
            }
        };

        chartPanel.setPreferredSize(new Dimension(700, 300));
        chartPanel.setMouseZoomable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createTitledBorder("Distribución de Load Index"));
        return panel;
    }

    private void showSelectedVDADetails() {
        detailsPanel.removeAll();
        int selectedRow = vdaTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < vdaList.size()) {
            VdaDTO selectedVDA = vdaList.get(selectedRow);

            // Panel principal para detalles
            JPanel mainDetailsPanel = new JPanel(new BorderLayout(15, 15));
            mainDetailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Panel izquierdo - Datos del VDA
            JPanel dataPanel = new JPanel();
            dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
            dataPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 20));

            addDetailField(dataPanel, "Machine Name:", selectedVDA.getMachineName());
            addDetailField(dataPanel, "Catalog Name:", selectedVDA.getCatalogName());
            addDetailField(dataPanel, "Registration State:", selectedVDA.getRegistrationState());
            addDetailField(dataPanel, "Power State:", selectedVDA.getPowerState());
            addDetailField(dataPanel, "Maintenance Mode:", selectedVDA.isInMaintenanceMode() ? "Yes" : "No");
            addDetailField(dataPanel, "Load Index:", String.valueOf(selectedVDA.getLoadIndex()));
            addDetailField(dataPanel, "Agent Version:", selectedVDA.getAgentVersion());
            addDetailField(dataPanel, "Desktop Group:", selectedVDA.getDesktopGroupName());
            addDetailField(dataPanel, "OS Type:", selectedVDA.getOsType());
            addDetailField(dataPanel, "Delivery Type:", selectedVDA.getDeliveryType());
            addDetailField(dataPanel, "IP Address:", selectedVDA.getIpAddress());
            addDetailField(dataPanel, "Physical Machine:", selectedVDA.getIsPhysical());
            addDetailField(dataPanel, "Last Registration:", selectedVDA.getLastRegistrationTime());
            addDetailField(dataPanel, "Persist Changes:", selectedVDA.getPersistUserChanges());
            addDetailField(dataPanel, "Sessions:", String.valueOf(selectedVDA.getSessionsEstablished()));

            // Panel derecho - Lista de aplicaciones
            JPanel appsPanel = new JPanel(new BorderLayout());
            appsPanel.setPreferredSize(new Dimension(350, 400));
            appsPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Applications (" + selectedVDA.getApplications().size() + ")"),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            DefaultListModel<String> listModel = new DefaultListModel<>();
            selectedVDA.getApplications().forEach(listModel::addElement);

            JList<String> appsList = new JList<>(listModel);
            appsList.setFont(new Font("Monospaced", Font.PLAIN, 13));
            appsList.setVisibleRowCount(12);
            appsList.setFixedCellWidth(300);

            JScrollPane appsScrollPane = new JScrollPane(appsList);
            appsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            appsPanel.add(appsScrollPane, BorderLayout.CENTER);

            // Añadir componentes al panel principal
            mainDetailsPanel.add(dataPanel, BorderLayout.CENTER);
            mainDetailsPanel.add(appsPanel, BorderLayout.EAST);

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

    @Override
    public void refreshData() {
        // Obtener los datos actualizados del servicio
        vdaList = citrixService.getVdas().values().stream()
                .collect(Collectors.toList());

        // Actualizar tabla
        tableModel.setRowCount(0);
        for (VdaDTO vda : vdaList) {
            tableModel.addRow(new Object[]{
                    vda.getMachineName(),
                    vda.getCatalogName(),
                    vda.getRegistrationState(),
                    vda.isInMaintenanceMode() ? "Yes" : "No"
            });
        }

        // Actualizar gráfico
        updateBarChart();
    }

    private void updateBarChart() {
        DefaultCategoryDataset dataset = (DefaultCategoryDataset) ((CategoryPlot) barChart.getPlot()).getDataset();
        dataset.clear();

        // Ordenar VDAs por load index (descendente) y tomar los top 10
        vdaList.sort((vda1, vda2) -> Integer.compare(vda2.getLoadIndex(), vda1.getLoadIndex()));

        int limit = Math.min(10, vdaList.size());
        for (int i = 0; i < limit; i++) {
            VdaDTO vda = vdaList.get(i);
            dataset.addValue(vda.getLoadIndex(), "Load Index", vda.getMachineName());
        }
    }
}
