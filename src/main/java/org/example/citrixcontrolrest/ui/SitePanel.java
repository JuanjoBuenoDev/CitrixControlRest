package org.example.citrixcontrolrest.ui;

import org.example.citrixcontrolrest.model.DgLoadDTO;
import org.example.citrixcontrolrest.service.CitrixService;
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
        topPanel.add(new JLabel("Panel 1", SwingConstants.CENTER));
        topPanel.add(new JLabel("Panel 2", SwingConstants.CENTER));
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
        gbc.weightx = 0.66;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5,5,5,5);
        bottomPanel.add(graficauser, gbc);

        // derecha 1/3
        gbc.gridx = 1;
        gbc.weightx = 0.34;
        bottomPanel.add(rightPanel, gbc);

        mainPanel.add(bottomPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        inicializarGraficaUsuarios();
    }

    private void inicializarGraficaUsuarios() {

        // Añadir el primer punto con el valor actual de usuarios y el instante actual (Minute())
        int cantidadUsuarios = citrixService.getActiveUsers().size();
        seriesUsuarios.addOrUpdate(new Minute(), cantidadUsuarios);

        TimeSeriesCollection dataset = new TimeSeriesCollection(seriesUsuarios);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Carga Usuarios Activos",
                "Hora",
                "Usuarios",
                dataset,
                false,
                true,
                false
        );

        // Fondo blanco
        chart.setBackgroundPaint(Color.WHITE);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Eje Y: desde 0 hasta el máximo + margen
        ValueAxis yAxis = plot.getRangeAxis();
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(cantidadUsuarios + 100);
        yAxis.setLabelPaint(Color.BLACK);
        yAxis.setTickLabelPaint(Color.BLACK);

        // Eje X: formato hora legible
        DateAxis domainAxis = new DateAxis("Hora");
        domainAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm")); // Ejemplo: 14:30
        domainAxis.setLabelPaint(Color.BLACK);
        domainAxis.setTickLabelPaint(Color.BLACK);
        plot.setDomainAxis(domainAxis);

        // Línea azul, puntos visibles
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);

        // Título en negro
        chart.getTitle().setPaint(Color.BLACK);
        if (chart.getLegend() != null) {
            chart.getLegend().setItemPaint(Color.BLACK);
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(true);
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(800, 400));

        graficauser.removeAll();
        graficauser.setLayout(new BorderLayout());
        graficauser.add(chartPanel, BorderLayout.CENTER);
        graficauser.revalidate();
        graficauser.repaint();
        graficauser.updateUI();
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
        seriesUsuarios.addOrUpdate(minuto, cantidadUsuarios);
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

            // Actualizar gráfico de Top 10 DGs
            actualizarGraficoTop10Dgs();

        });
    }
}

