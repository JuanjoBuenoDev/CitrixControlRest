package org.example.citrixcontrolrest.ui;

import org.example.citrixcontrolrest.service.CitrixService;
import org.jfree.chart.*;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class SitePanel extends JPanel {

    private final CitrixService citrixService;
    private final JPanel graficauser = new JPanel();  // tu panel gráfico actual
    private final JPanel rightPanel = new JPanel();   // panel derecho del bottom
    private final TimeSeries seriesUsuarios = new TimeSeries("Usuarios Conectados");

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
        topPanel.add(new JLabel("Panel 3", SwingConstants.CENTER));

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
        iniciarActualizacionCada2Min();
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

    private void iniciarActualizacionCada2Min() {
        Timer timer = new Timer(true); // Daemon thread
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    int total = citrixService.getActiveUsers().size();
                    LocalTime ahora = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
                    agregarPuntoAGrafica(ahora, total);
                    System.out.println("🕒 [" + ahora + "] Usuarios conectados: " + total);
                });
            }
        }, 0, 2 * 60 * 1000); // Cada 2 minutos
    }
}

