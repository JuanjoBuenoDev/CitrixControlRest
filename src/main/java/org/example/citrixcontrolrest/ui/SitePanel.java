package org.example.citrixcontrolrest.ui;

import org.example.citrixcontrolrest.model.DgLoadDTO;
import org.example.citrixcontrolrest.service.CitrixService;
import org.example.citrixcontrolrest.utils.ToastNotifier;
import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.Timer;


public class SitePanel extends JPanel implements Refreshable{

    private final CitrixService citrixService;
    private final JPanel graficauser = new JPanel();  // tu panel gráfico actual
    private final JPanel rightPanel = new JPanel();   // panel derecho del bottom
    private final TimeSeries seriesUsuarios = new TimeSeries("Usuarios Conectados");
    private JPanel panelTop10Dgs;
    private DefaultTableModel licenseTableModel;
    private DefaultTableModel datastoreTableModel;
    private static final int MINUTES_TO_DISPLAY = 60;


    public SitePanel(CitrixService citrixService) {
        this.citrixService = citrixService;
        setLayout(new BorderLayout());
        setOpaque(false);


        // --- PANEL PRINCIPAL CON DIVISION ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);

        // --- PANEL SUPERIOR: 3 partes iguales ---
        JPanel topPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        topPanel.setOpaque(false);

        licenseTableModel = crearModeloVacio(8);
        JTable licenseTable = new JTable(licenseTableModel);
        licenseTable.setTableHeader(null);
        licenseTable.setRowHeight(25);

        topPanel.add(wrapWithTitledBorder(licenseTable, "Licencias"));

        datastoreTableModel = crearModeloVacio(3);
        JTable datastoreTable = new JTable(datastoreTableModel);
        datastoreTable.setTableHeader(null);
        datastoreTable.setRowHeight(25);
        topPanel.add(wrapWithTitledBorder(datastoreTable, "Data Store"));

        panelTop10Dgs = new JPanel(new BorderLayout());
        panelTop10Dgs.setOpaque(false);
        panelTop10Dgs.add(crearGraficoTop10Dgs(), BorderLayout.CENTER);
        topPanel.add(panelTop10Dgs);


        mainPanel.add(topPanel, BorderLayout.NORTH);

        // --- PANEL INFERIOR ---
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setOpaque(false);

        // Configura el panel derecho
        rightPanel.setOpaque(false);
        rightPanel.setBackground(Color.GRAY);
        rightPanel.add(new JLabel("Panel Derecho"));

        // Aquí ya usas tu graficauser como panel izquierdo
        graficauser.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();

        // izquierda (graficauser) 2/3
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5,5,5,5);
        bottomPanel.add(graficauser, gbc);


        mainPanel.add(bottomPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        inicializarGraficaUsuarios();
    }

    private DefaultTableModel crearModeloVacio(int filas) {
        String[] columns = { "", "" };
        Object[][] data = new Object[filas][2];
        for (int i = 0; i < filas; i++) {
            data[i][0] = "";  // etiqueta
            data[i][1] = "";  // valor
        }
        return new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel wrapWithTitledBorder(JTable table, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    public void actualizarDatos() {

        Object[][] licenseData = {
                { "License Edition", citrixService.getCitrixSite().getLicenseEdition() },
                { "License Server Name", citrixService.getCitrixSite().getLicenseServerName() },
                { "License Server Port", citrixService.getCitrixSite().getLicenseServerPort() },
                { "Grace Hours Left", citrixService.getCitrixSite().getLicensingGraceHoursLeft() },
                { "Grace Period Active", citrixService.getCitrixSite().isLicensingGracePeriodActive() },
                { "Local Host Cache Enabled", citrixService.getCitrixSite().isLocalHostCacheEnabled() },
                { "Peak Concurrent Users", citrixService.getCitrixSite().getPeakConcurrentLicenseUsers() },
                { "Peak Concurrent Devices", citrixService.getCitrixSite().getPeakConcurrentLicensedDevices() }
        };

        Object[][] datastoreData = {
                { "Data Store Site", citrixService.getCitrixSite().getDataStoreSite() },
                { "Data Store Monitor", citrixService.getCitrixSite().getDataStoreMonitor() },
                { "Data Store Log", citrixService.getCitrixSite().getDataStoreLog() }
        };

        actualizarModelo(licenseTableModel, licenseData);
        actualizarModelo(datastoreTableModel, datastoreData);
    }

    private void actualizarModelo(DefaultTableModel model, Object[][] data) {
        for (int i = 0; i < data.length; i++) {
            model.setValueAt(data[i][0], i, 0);
            model.setValueAt(data[i][1], i, 1);
        }
    }



    private void inicializarGraficaUsuarios() {
        // Obtener datos actuales
        int cantidadUsuarios = citrixService.getActiveUsers().size();
        Minute ultimoMinuto = new Minute();

        // Añadir o actualizar el punto actual
        seriesUsuarios.addOrUpdate(ultimoMinuto, cantidadUsuarios);

        // Configurar el dataset
        TimeSeriesCollection dataset = new TimeSeriesCollection(seriesUsuarios);

        // Crear o actualizar el gráfico
        JFreeChart chart;
        if (graficauser.getComponentCount() > 0 && graficauser.getComponent(0) instanceof ChartPanel) {
            // Reutilizar el gráfico existente
            ChartPanel existingPanel = (ChartPanel) graficauser.getComponent(0);
            chart = existingPanel.getChart();
            chart.setTitle("Carga Usuarios Activos (" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + ")");
            chart.getXYPlot().setDataset(dataset);
        } else {
            // Crear nuevo gráfico
            chart = ChartFactory.createTimeSeriesChart(
                    "Carga Usuarios Activos (" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + ")",
                    "Hora",
                    "Usuarios",
                    dataset,
                    false,
                    true,
                    false
            );

            // Configuración de estilo inicial
            chart.setBackgroundPaint(Color.WHITE);
            XYPlot plot = chart.getXYPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

            // Configurar eje Y dinámico
            ValueAxis yAxis = plot.getRangeAxis();
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(Math.max(cantidadUsuarios + 20, 50)); // Mínimo 50 para mejor visualización
            yAxis.setLabelPaint(Color.BLACK);
            yAxis.setTickLabelPaint(Color.BLACK);

            // Configurar eje X para mostrar solo la última hora
            DateAxis domainAxis = new DateAxis("Hora");
            domainAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
            domainAxis.setLabelPaint(Color.BLACK);
            domainAxis.setTickLabelPaint(Color.BLACK);
            plot.setDomainAxis(domainAxis);

            // Configurar renderizador
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesPaint(0, new Color(0, 100, 200)); // Azul más oscuro
            renderer.setSeriesShapesVisible(0, true);
            renderer.setSeriesStroke(0, new BasicStroke(2.0f)); // Línea más gruesa
            plot.setRenderer(renderer);

            // Configurar título y leyenda
            chart.getTitle().setPaint(Color.BLACK);
            if (chart.getLegend() != null) {
                chart.getLegend().setItemPaint(Color.BLACK);
            }
        }

        // Configurar el rango del eje X para mostrar la última hora
        XYPlot plot = chart.getXYPlot();
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();

        Calendar cal = Calendar.getInstance();
        Date endTime = cal.getTime();
        cal.add(Calendar.MINUTE, -MINUTES_TO_DISPLAY);
        Date startTime = cal.getTime();
        domainAxis.setRange(startTime, endTime);

        // Ajustar el eje Y según los datos actuales
        ValueAxis yAxis = plot.getRangeAxis();
        yAxis.setRange(0, Math.max(seriesUsuarios.getMaxY() + 20, 50));

        // Actualizar el panel del gráfico
        ChartPanel chartPanel;
        if (graficauser.getComponentCount() > 0 && graficauser.getComponent(0) instanceof ChartPanel) {
            chartPanel = (ChartPanel) graficauser.getComponent(0);
            chartPanel.setChart(chart);
        } else {
            chartPanel = new ChartPanel(chart) {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(800, 400);
                }
            };
            chartPanel.setOpaque(true);
            chartPanel.setBackground(Color.WHITE);
            chartPanel.setDomainZoomable(false);

            graficauser.removeAll();
            graficauser.setLayout(new BorderLayout());
            graficauser.add(chartPanel, BorderLayout.CENTER);
        }

        graficauser.revalidate();
        graficauser.repaint();
    }

    private void agregarPuntoAGrafica(LocalTime hora, int cantidadUsuarios) {
        Calendar calendar = Calendar.getInstance();
        Minute minuto = new Minute(
                hora.getMinute(),
                hora.getHour(),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)
        );

        // Eliminar datos antiguos (más de 60 minutos)
        Minute limite = new Minute(new Date(System.currentTimeMillis() - MINUTES_TO_DISPLAY * 60 * 1000));

        // Crear una lista de periodos a eliminar
        List<Minute> periodosAEliminar = new ArrayList<>();
        for (int i = 0; i < seriesUsuarios.getItemCount(); i++) {
            TimeSeriesDataItem item = seriesUsuarios.getDataItem(i);
            Minute periodo = (Minute) item.getPeriod();
            if (periodo.compareTo(limite) < 0) {
                periodosAEliminar.add(periodo);
            } else {
                break; // Los siguientes serán más recientes
            }
        }

        // Eliminar los periodos antiguos
        for (Minute periodo : periodosAEliminar) {
            seriesUsuarios.delete(periodo);
        }

        // Añadir nuevo punto
        seriesUsuarios.addOrUpdate(minuto, cantidadUsuarios);

        // Actualizar la gráfica
        inicializarGraficaUsuarios();
    }



    private void actualizarGraficoTop10Dgs() {
        SwingUtilities.invokeLater(() -> {
            panelTop10Dgs.removeAll();
            panelTop10Dgs.add(crearGraficoTop10Dgs(), BorderLayout.CENTER);
            panelTop10Dgs.revalidate();
            panelTop10Dgs.repaint();
        });
    }

    public List<DgLoadDTO> getTop10DeliveryGroupsByLoad() {
        return citrixService.getDeliveryGroups().values().stream()
                .filter(dg -> dg.getAverageLoadIndex() != null && !dg.getAverageLoadIndex().isBlank())
                .sorted((a, b) -> {
                    double loadA = parseLoadIndex(a.getAverageLoadIndex());
                    double loadB = parseLoadIndex(b.getAverageLoadIndex());
                    return Double.compare(loadB, loadA); // Descendente
                })
                .limit(10)
                .map(dg -> new DgLoadDTO(dg.getName(), dg.getAverageLoadIndex()))
                .toList(); // ✅ Java 16+
    }

    private double parseLoadIndex(String load) {
        try {
            return Double.parseDouble(load);
        } catch (NumberFormatException e) {
            return -1; // Carga inválida tratada como muy baja
        }
    }

    private JPanel crearGraficoTop10Dgs() {
        List<DgLoadDTO> top10Dgs = getTop10DeliveryGroupsByLoad();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (DgLoadDTO dto : top10Dgs) {
            try {
                double load = Double.parseDouble(dto.getAverageLoadIndex());
                dataset.addValue(load, "Carga", dto.getName());

                // Mostrar notificación si la carga es > 85%
                if (load > 75) {
                    String message = "¡Alerta! DG " + dto.getName() + " con carga alta: " + load + "%";
                    ToastNotifier.showToast(message, ToastNotifier.ToastType.WARNING, 5000);
                }
            } catch (NumberFormatException ignored) {}
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Top 10 DG por Carga",
                "Delivery Group",
                "Carga (%)",
                dataset,
                PlotOrientation.HORIZONTAL,
                false, true, false
        );

        // Personalización visual
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        chart.setBackgroundPaint(null);
        chart.getPlot().setBackgroundPaint(null);
        chart.getPlot().setOutlinePaint(null);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0x007acc)); // Azul moderno
        renderer.setItemMargin(0.00);
        renderer.setMaximumBarWidth(0.07);

        CategoryAxis categoryAxis = plot.getDomainAxis();
        categoryAxis.setCategoryMargin(0.0);  // Reduce espacio entre categorías
        categoryAxis.setLowerMargin(0.00);    // Margen al principio del eje
        categoryAxis.setUpperMargin(0.00);    // Margen al final del eje
        categoryAxis.setMaximumCategoryLabelLines(10);

        // Estética general
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setPaint(Color.BLACK);

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(300, 500));
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        return panel;
    }


    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            // Actualizar gráfico de usuarios conectados
            int total = citrixService.getActiveUsers().size();
            LocalTime ahora = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
            agregarPuntoAGrafica(ahora, total);
            actualizarDatos();

            // Actualizar gráfico de Top 10 DGs
            actualizarGraficoTop10Dgs();

        });
    }
}

