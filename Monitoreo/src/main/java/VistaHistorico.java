import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VistaHistorico extends JPanel {
    private ClienteMain ventanaPrincipal;
    private ChartPanel chartPanel;
    private JSpinner spinnerFechaInicio;
    private JSpinner spinnerFechaFin;
    private JSpinner spinnerHoraInicio;
    private JSpinner spinnerHoraFin;
    private JButton btnConsultar;
    private JButton btnVolver;
    private XYSeries series1, series2, series3;
    private ClienteSocket clienteSocket;

    // Colores Universidad de Sonora
    private static final Color AZUL_UNISON = new Color(0, 82, 158);
    private static final Color DORADO_UNISON = new Color(248, 187, 0);
    private static final Color DORADO_OSCURO = new Color(217, 158, 48);

    public VistaHistorico(ClienteMain ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
        setLayout(new BorderLayout());

        // Panel superior
        JPanel panelSuperior = crearPanelSuperior();

        // Panel de filtros
        JPanel panelFiltros = crearPanelFiltros();

        // Gráfica
        series1 = new XYSeries("Dato 1");
        series2 = new XYSeries("Dato 2");
        series3 = new XYSeries("Dato 3");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(series3);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Datos Históricos",
                "Registro",
                "Valor",
                dataset
        );

        chartPanel = new ChartPanel(chart);

        // Panel central
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.add(chartPanel, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelFiltros, BorderLayout.WEST);
        add(panelCentral, BorderLayout.CENTER);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        btnVolver = new RoundedButton("← Volver", 4);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVolver.setBackground(Color.WHITE);
        btnVolver.setForeground(AZUL_UNISON);
        btnVolver.setBorder(new RoundedBorder(4, AZUL_UNISON, 2));
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> ventanaPrincipal.mostrarVista("INICIO"));

        JLabel lblTitulo = new JLabel("Consulta de Datos Históricos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(AZUL_UNISON);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(btnVolver, BorderLayout.WEST);
        panel.add(lblTitulo, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelFiltros() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createTitledBorder("Filtros de Consulta")
        ));
        panel.setPreferredSize(new Dimension(300, 0));

        // Fecha inicio
        JLabel lblFechaInicio = new JLabel("Fecha Inicio:");
        lblFechaInicio.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblFechaInicio.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        spinnerFechaInicio = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorFechaInicio = new JSpinner.DateEditor(spinnerFechaInicio, "dd/MM/yyyy");
        spinnerFechaInicio.setEditor(editorFechaInicio);
        spinnerFechaInicio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        spinnerFechaInicio.setAlignmentX(Component.LEFT_ALIGNMENT);
        spinnerFechaInicio.setBorder(new RoundedBorder(4, new Color(180, 180, 180), 1));
        ((JSpinner.DefaultEditor) spinnerFechaInicio.getEditor()).getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) spinnerFechaInicio.getEditor()).getTextField().setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Hora inicio
        JLabel lblHoraInicio = new JLabel("Hora Inicio:");
        lblHoraInicio.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblHoraInicio.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        spinnerHoraInicio = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorHoraInicio = new JSpinner.DateEditor(spinnerHoraInicio, "HH:mm:ss");
        spinnerHoraInicio.setEditor(editorHoraInicio);
        spinnerHoraInicio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        spinnerHoraInicio.setAlignmentX(Component.LEFT_ALIGNMENT);
        spinnerHoraInicio.setBorder(new RoundedBorder(4, new Color(180, 180, 180), 1));
        ((JSpinner.DefaultEditor) spinnerHoraInicio.getEditor()).getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) spinnerHoraInicio.getEditor()).getTextField().setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Fecha fin
        JLabel lblFechaFin = new JLabel("Fecha Fin:");
        lblFechaFin.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblFechaFin.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        spinnerFechaFin = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorFechaFin = new JSpinner.DateEditor(spinnerFechaFin, "dd/MM/yyyy");
        spinnerFechaFin.setEditor(editorFechaFin);
        spinnerFechaFin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        spinnerFechaFin.setAlignmentX(Component.LEFT_ALIGNMENT);
        spinnerFechaFin.setBorder(new RoundedBorder(4, new Color(180, 180, 180), 1));
        ((JSpinner.DefaultEditor) spinnerFechaFin.getEditor()).getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) spinnerFechaFin.getEditor()).getTextField().setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Hora fin
        JLabel lblHoraFin = new JLabel("Hora Fin:");
        lblHoraFin.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblHoraFin.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        spinnerHoraFin = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorHoraFin = new JSpinner.DateEditor(spinnerHoraFin, "HH:mm:ss");
        spinnerHoraFin.setEditor(editorHoraFin);
        spinnerHoraFin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        spinnerHoraFin.setAlignmentX(Component.LEFT_ALIGNMENT);
        spinnerHoraFin.setBorder(new RoundedBorder(4, new Color(180, 180, 180), 1));
        ((JSpinner.DefaultEditor) spinnerHoraFin.getEditor()).getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) spinnerHoraFin.getEditor()).getTextField().setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Botón consultar
        btnConsultar = new RoundedButton("Consultar", 4);
        btnConsultar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConsultar.setBackground(DORADO_UNISON);
        btnConsultar.setForeground(Color.WHITE);
        btnConsultar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConsultar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnConsultar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnConsultar.addActionListener(e -> consultarDatos());

        // Agregar componentes
        panel.add(lblFechaInicio);
        panel.add(spinnerFechaInicio);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lblHoraInicio);
        panel.add(spinnerHoraInicio);
        panel.add(Box.createVerticalStrut(20));

        panel.add(lblFechaFin);
        panel.add(spinnerFechaFin);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lblHoraFin);
        panel.add(spinnerHoraFin);
        panel.add(Box.createVerticalStrut(30));

        panel.add(btnConsultar);

        return panel;
    }

    private void consultarDatos() {
        // Obtener fechas
        Date fechaInicio = (Date) spinnerFechaInicio.getValue();
        Date horaInicio = (Date) spinnerHoraInicio.getValue();
        Date fechaFin = (Date) spinnerFechaFin.getValue();
        Date horaFin = (Date) spinnerHoraFin.getValue();

        // Combinar fecha y hora
        java.util.Calendar calInicio = java.util.Calendar.getInstance();
        calInicio.setTime(fechaInicio);
        java.util.Calendar calHoraInicio = java.util.Calendar.getInstance();
        calHoraInicio.setTime(horaInicio);
        calInicio.set(java.util.Calendar.HOUR_OF_DAY, calHoraInicio.get(java.util.Calendar.HOUR_OF_DAY));
        calInicio.set(java.util.Calendar.MINUTE, calHoraInicio.get(java.util.Calendar.MINUTE));
        calInicio.set(java.util.Calendar.SECOND, calHoraInicio.get(java.util.Calendar.SECOND));

        java.util.Calendar calFin = java.util.Calendar.getInstance();
        calFin.setTime(fechaFin);
        java.util.Calendar calHoraFin = java.util.Calendar.getInstance();
        calHoraFin.setTime(horaFin);
        calFin.set(java.util.Calendar.HOUR_OF_DAY, calHoraFin.get(java.util.Calendar.HOUR_OF_DAY));
        calFin.set(java.util.Calendar.MINUTE, calHoraFin.get(java.util.Calendar.MINUTE));
        calFin.set(java.util.Calendar.SECOND, calHoraFin.get(java.util.Calendar.SECOND));

        java.sql.Timestamp tsInicio = new java.sql.Timestamp(calInicio.getTimeInMillis());
        java.sql.Timestamp tsFin = new java.sql.Timestamp(calFin.getTimeInMillis());

        // Validar rango
        if (tsInicio.after(tsFin)) {
            JOptionPane.showMessageDialog(this,
                    "La fecha/hora de inicio debe ser anterior a la fecha/hora de fin",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mostrar mensaje de cargando
        btnConsultar.setEnabled(false);
        btnConsultar.setText("Cargando...");

        // Limpiar series
        series1.clear();
        series2.clear();
        series3.clear();

        // Cargar datos en un hilo
        Thread hiloCarga = new Thread(() -> {
            try {
                // Conectar al servidor
                clienteSocket = new ClienteSocket("localhost", 5000);
                if (!clienteSocket.conectar()) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "No se pudo conectar al servidor",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        btnConsultar.setEnabled(true);
                        btnConsultar.setText("Consultar");
                    });
                    return;
                }

                // Consultar datos
                List<DatoSensor> datos = clienteSocket.consultarDatos(tsInicio, tsFin);

                // Actualizar gráfica
                SwingUtilities.invokeLater(() -> {
                    if (datos != null && !datos.isEmpty()) {
                        for (int i = 0; i < datos.size(); i++) {
                            DatoSensor dato = datos.get(i);
                            series1.add(i, dato.getValor1());
                            series2.add(i, dato.getValor2());
                            series3.add(i, dato.getValor3());
                        }

                        String mensaje = String.format(
                                "Se cargaron %d registros correctamente\n\n" +
                                        "Primer registro:\n" +
                                        "  Fecha: %s\n" +
                                        "  Hora: %s\n" +
                                        "  Valores: x=%d, y=%d, z=%d\n\n" +
                                        "Último registro:\n" +
                                        "  Fecha: %s\n" +
                                        "  Hora: %s\n" +
                                        "  Valores: x=%d, y=%d, z=%d",
                                datos.size(),
                                datos.get(0).getFecha(),
                                datos.get(0).getHora(),
                                datos.get(0).getValor1(),
                                datos.get(0).getValor2(),
                                datos.get(0).getValor3(),
                                datos.get(datos.size()-1).getFecha(),
                                datos.get(datos.size()-1).getHora(),
                                datos.get(datos.size()-1).getValor1(),
                                datos.get(datos.size()-1).getValor2(),
                                datos.get(datos.size()-1).getValor3()
                        );

                        JOptionPane.showMessageDialog(this,
                                mensaje,
                                "Consulta Exitosa", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No se encontraron datos en el rango especificado",
                                "Sin Resultados", JOptionPane.INFORMATION_MESSAGE);
                    }
                    btnConsultar.setEnabled(true);
                    btnConsultar.setText("Consultar");
                    chartPanel.repaint();
                });

                clienteSocket.desconectar();

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Error al cargar datos: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    btnConsultar.setEnabled(true);
                    btnConsultar.setText("Consultar");
                });
            }
        });

        hiloCarga.start();
    }

    public void limpiar() {
        series1.clear();
        series2.clear();
        series3.clear();
    }
}