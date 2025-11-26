import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Random;

public class VistaMonitor extends JPanel {
    private ClienteMain ventanaPrincipal;
    private final XYSeries s1 = new XYSeries("Dato 1");
    private final XYSeries s2 = new XYSeries("Dato 2");
    private final XYSeries s3 = new XYSeries("Dato 3");
    private volatile int sampleIndex = 0;
    private final StringBuilder lineBuf = new StringBuilder();
    private ChartPanel chartPanel;
    private JTextArea log;
    private SerialPort serialPort;
    private ClienteSocket clienteSocket;
    private JButton btnVolver;
    private JButton btnIniciar;
    private JButton btnSimular;
    private JLabel lblEstado;
    private Thread simuladorThread;
    private volatile boolean simuladorActivo = false;
    private Random random;

    // Colores Universidad de Sonora
    private static final Color AZUL_UNISON = new Color(0, 82, 158);
    private static final Color DORADO_UNISON = new Color(248, 187, 0);
    private static final Color DORADO_OSCURO = new Color(217, 158, 48);

    public VistaMonitor(ClienteMain ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
        this.random = new Random();
        setLayout(new BorderLayout());

        // Panel superior con controles
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnVolver = new RoundedButton("← Volver", 4);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVolver.setBackground(Color.WHITE);
        btnVolver.setForeground(AZUL_UNISON);
        btnVolver.setBorder(new RoundedBorder(4, AZUL_UNISON, 2));
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> ventanaPrincipal.mostrarVista("INICIO"));

        btnIniciar = new RoundedButton("Arduino Real", 4);
        btnIniciar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnIniciar.setBackground(AZUL_UNISON);
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIniciar.addActionListener(e -> iniciarMonitoreo());

        btnSimular = new RoundedButton("Modo Simulación", 4);
        btnSimular.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSimular.setBackground(DORADO_UNISON);
        btnSimular.setForeground(Color.WHITE);
        btnSimular.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimular.addActionListener(e -> iniciarSimulacion());

        lblEstado = new JLabel("Estado: Detenido");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(lblEstado);
        panelBotones.add(btnSimular);
        panelBotones.add(btnIniciar);

        panelSuperior.add(btnVolver, BorderLayout.WEST);
        panelSuperior.add(panelBotones, BorderLayout.EAST);

        // Crear gráfica
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(s1);
        dataset.addSeries(s2);
        dataset.addSeries(s3);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Monitoreo en Tiempo Real",
                "Muestras",
                "Valor",
                dataset
        );

        XYPlot plot = chart.getXYPlot();
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        xAxis.setAutoRange(true);
        yAxis.setAutoRange(true);
        xAxis.setAutoRangeIncludesZero(false);
        yAxis.setAutoRangeIncludesZero(false);

        chartPanel = new ChartPanel(chart);

        // Log de datos
        log = new JTextArea(8, 60);
        log.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(log);

        // Panel central
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.add(chartPanel, BorderLayout.CENTER);
        panelCentral.add(scrollLog, BorderLayout.SOUTH);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
    }

    // ========== MODO SIMULACIÓN (SIN ARDUINO) ==========
    private void iniciarSimulacion() {
        if (simuladorThread != null && simuladorThread.isAlive()) {
            detenerMonitoreo();
            return;
        }

        // Conectar al servidor
        clienteSocket = new ClienteSocket("localhost", 5000);
        if (!clienteSocket.conectar()) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar al servidor. Asegúrate de que esté ejecutándose.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        lblEstado.setText("Estado: SIMULANDO (sin Arduino)");
        lblEstado.setForeground(DORADO_OSCURO);
        btnSimular.setText("Detener Simulación");
        btnSimular.setBackground(new Color(231, 76, 60));
        btnIniciar.setEnabled(false);

        log.append("=== MODO SIMULACIÓN INICIADO ===\n");
        log.append("Generando datos sintéticos cada 1 segundo...\n\n");

        // Limpiar series
        s1.clear();
        s2.clear();
        s3.clear();
        sampleIndex = 0;

        // Marcar como activo
        simuladorActivo = true;

        // Crear y ejecutar Thread
        simuladorThread = new Thread(() -> {
            System.out.println("🧵 Thread de simulación iniciado");

            try {
                while (simuladorActivo) {
                    // Generar datos aleatorios
                    int x = random.nextInt(101);
                    int y = random.nextInt(101);
                    int z = random.nextInt(101);

                    String linea = String.format("x:%d,y:%d,z:%d", x, y, z);
                    handleLine(linea);

                    // Esperar 1 segundo
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.out.println("Thread de simulación interrumpido");
            } finally {
                System.out.println("Thread de simulación finalizado");
            }
        });

        // Darle un nombre al thread (útil para debugging)
        simuladorThread.setName("Simulador-Arduino");

        // Iniciar el thread
        simuladorThread.start();
    }

    // ========== MODO REAL (CON ARDUINO) ==========
    private void iniciarMonitoreo() {
        if (serialPort != null && serialPort.isOpen()) {
            detenerMonitoreo();
            return;
        }

        // Conectar al servidor primero
        clienteSocket = new ClienteSocket("localhost", 5000);
        if (!clienteSocket.conectar()) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar al servidor. Asegúrate de que esté ejecutándose.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Seleccionar puerto
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "No hay puertos COM disponibles.\n\n" +
                            "Usa el botón 'Modo Simulación' para probar sin Arduino.",
                    "Sin Puertos", JOptionPane.WARNING_MESSAGE);
            clienteSocket.desconectar();
            return;
        }

        String[] options = new String[ports.length];
        for (int i = 0; i < ports.length; i++) {
            options[i] = ports[i].getSystemPortName() + " - " + ports[i].getDescriptivePortName();
        }

        String choice = (String) JOptionPane.showInputDialog(
                this, "Selecciona el puerto del Arduino:", "Puertos disponibles",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == null) {
            clienteSocket.desconectar();
            return;
        }

        String portName = choice.split(" ")[0];
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(9600);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);

        if (!serialPort.openPort()) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo abrir el puerto " + portName,
                    "Error", JOptionPane.ERROR_MESSAGE);
            clienteSocket.desconectar();
            return;
        }

        lblEstado.setText("Estado: Monitoreando Arduino Real");
        lblEstado.setForeground(new Color(46, 204, 113));
        btnIniciar.setText("⏸ Detener");
        btnIniciar.setBackground(new Color(231, 76, 60));
        btnSimular.setEnabled(false);

        log.append("=== CONECTADO A ARDUINO REAL ===\n");
        log.append("Puerto: " + portName + "\n\n");

        // Limpiar series
        s1.clear();
        s2.clear();
        s3.clear();
        sampleIndex = 0;

        // Listener para datos del puerto
        serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;

                byte[] buf = new byte[serialPort.bytesAvailable()];
                int read = serialPort.readBytes(buf, buf.length);
                if (read <= 0) return;

                String chunk = new String(buf, 0, read, StandardCharsets.US_ASCII);

                synchronized (lineBuf) {
                    lineBuf.append(chunk);
                    int idx;
                    while ((idx = indexOfLineBreak(lineBuf)) >= 0) {
                        String line = lineBuf.substring(0, idx).trim();
                        int skip = (lineBuf.length() > idx + 1 && isLineBreakPair(lineBuf, idx)) ? 2 : 1;
                        lineBuf.delete(0, idx + skip);
                        if (!line.isEmpty()) handleLine(line);
                    }
                }
            }
        });
    }

    public void detenerMonitoreo() {
        // Detener modo real
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            serialPort = null;
        }

        // Detener simulación
        if (simuladorThread != null && simuladorThread.isAlive()) {
            simuladorActivo = false;  // Señal para que el thread termine
            try {
                simuladorThread.join(2000);  // Espera máximo 2 segundos
            } catch (InterruptedException e) {
                simuladorThread.interrupt();  // Fuerza la interrupción
            }
            simuladorThread = null;
        }

        if (clienteSocket != null) {
            clienteSocket.desconectar();
            clienteSocket = null;
        }

        lblEstado.setText("Estado: Detenido");
        lblEstado.setForeground(Color.BLACK);
        btnIniciar.setText("Arduino Real");
        btnIniciar.setBackground(AZUL_UNISON);
        btnIniciar.setEnabled(true);
        btnSimular.setText("Modo Simulación");
        btnSimular.setBackground(DORADO_UNISON);
        btnSimular.setEnabled(true);

        log.append("\n=== MONITOREO DETENIDO ===\n\n");
    }

    private int indexOfLineBreak(CharSequence s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n' || c == '\r') return i;
        }
        return -1;
    }

    private boolean isLineBreakPair(CharSequence s, int i) {
        if (i + 1 >= s.length()) return false;
        char a = s.charAt(i), b = s.charAt(i + 1);
        return (a == '\r' && b == '\n') || (a == '\n' && b == '\r');
    }

    private void handleLine(String line) {
        SwingUtilities.invokeLater(() -> {
            log.append(line + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });

        String[] tokens = line.split("[,;\\s]+");
        double[] vals = new double[Math.min(tokens.length, 3)];
        int n = 0;

        for (String t : tokens) {
            String cleaned = t.replaceAll("[^0-9\\-\\.]", "");
            if (!cleaned.isEmpty()) {
                try {
                    vals[n++] = Double.parseDouble(cleaned);
                } catch (NumberFormatException ignored) {}
                if (n == 3) break;
            }
        }

        if (n == 0) return;

        final int x = sampleIndex++;

        if (n >= 1) s1.add(x, vals[0]);
        if (n >= 2) s2.add(x, vals[1]);
        if (n >= 3) s3.add(x, vals[2]);

        trimSeries(s1, 400);
        trimSeries(s2, 400);
        trimSeries(s3, 400);

        SwingUtilities.invokeLater(chartPanel::repaint);

        // Enviar datos al servidor (encriptados)
        if (clienteSocket != null) {
            // Crear fecha y hora por separado correctamente
            java.util.Calendar cal = java.util.Calendar.getInstance();

            // Fecha: solo año, mes, día (hora en 00:00:00)
            java.util.Calendar calFecha = java.util.Calendar.getInstance();
            calFecha.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calFecha.set(java.util.Calendar.MINUTE, 0);
            calFecha.set(java.util.Calendar.SECOND, 0);
            calFecha.set(java.util.Calendar.MILLISECOND, 0);
            java.sql.Date fecha = new java.sql.Date(calFecha.getTimeInMillis());

            // Hora: solo hora, minuto, segundo (fecha en 1970-01-01)
            java.util.Calendar calHora = java.util.Calendar.getInstance();
            calHora.set(1970, java.util.Calendar.JANUARY, 1);
            java.sql.Time hora = new java.sql.Time(calHora.getTimeInMillis());

            DatoSensor dato = new DatoSensor(fecha, hora,
                    n >= 1 ? (int)vals[0] : 0,
                    n >= 2 ? (int)vals[1] : 0,
                    n >= 3 ? (int)vals[2] : 0);
            clienteSocket.enviarDato(dato);
        }
    }

    private void trimSeries(XYSeries s, int maxPoints) {
        while (s.getItemCount() > maxPoints) s.remove(0);
    }
}