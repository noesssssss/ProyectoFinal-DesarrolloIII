import javax.swing.*;
import java.awt.*;

public class ClienteMain extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private VistaInicio vistaInicio;
    private VistaMonitor vistaMonitor;
    private VistaHistorico vistaHistorico;

    public ClienteMain() {
        setTitle("Sistema de Monitoreo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Crear las vistas
        vistaInicio = new VistaInicio(this);
        vistaMonitor = new VistaMonitor(this);
        vistaHistorico = new VistaHistorico(this);

        // Agregar vistas al CardLayout
        mainPanel.add(vistaInicio, "INICIO");
        mainPanel.add(vistaMonitor, "MONITOR");
        mainPanel.add(vistaHistorico, "HISTORICO");

        add(mainPanel);

        // Mostrar vista de inicio
        mostrarVista("INICIO");
    }

    public void mostrarVista(String nombreVista) {
        cardLayout.show(mainPanel, nombreVista);

        // Detener monitor si se sale de esa vista
        if (!nombreVista.equals("MONITOR") && vistaMonitor != null) {
            vistaMonitor.detenerMonitoreo();
        }

        // Limpiar histórico si se sale de esa vista
        if (!nombreVista.equals("HISTORICO") && vistaHistorico != null) {
            vistaHistorico.limpiar();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteMain app = new ClienteMain();
            app.setVisible(true);
        });
    }
}

// Clase para bordes redondeados
class RoundedBorder implements javax.swing.border.Border {
    private int radius;
    private Color color;
    private int thickness;

    public RoundedBorder(int radius, Color color, int thickness) {
        this.radius = radius;
        this.color = color;
        this.thickness = thickness;
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(this.radius + thickness, this.radius + thickness,
                this.radius + thickness, this.radius + thickness);
    }

    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(thickness));
        g2d.drawRoundRect(x + thickness/2, y + thickness/2,
                width - thickness, height - thickness, radius, radius);
        g2d.dispose();
    }
}

// Panel con bordes redondeados
class RoundedPanel extends JPanel {
    private int radius;
    private Color backgroundColor;

    public RoundedPanel(int radius, Color backgroundColor) {
        this.radius = radius;
        this.backgroundColor = backgroundColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(backgroundColor);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2d.dispose();
        super.paintComponent(g);
    }
}

// Botón con bordes redondeados
class RoundedButton extends JButton {
    private int radius;

    public RoundedButton(String text, int radius) {
        super(text);
        this.radius = radius;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed()) {
            g2d.setColor(getBackground().darker());
        } else if (getModel().isRollover()) {
            g2d.setColor(getBackground().brighter());
        } else {
            g2d.setColor(getBackground());
        }

        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2d.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground().darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2d.dispose();
    }
}

// ==================== VISTA INICIO ====================
class VistaInicio extends JPanel {
    private ClienteMain ventanaPrincipal;

    // Colores Universidad de Sonora
    private static final Color AZUL_UNISON = new Color(0, 82, 158);
    private static final Color AZUL_OSCURO_UNISON = new Color(1, 82, 148);
    private static final Color DORADO_UNISON = new Color(248, 187, 0);
    private static final Color DORADO_OSCURO_UNISON = new Color(217, 158, 48);

    public VistaInicio(ClienteMain ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Panel central con información
        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setBackground(Color.WHITE);
        panelCentral.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        // Logo
        JLabel lblLogo = crearLogo();
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nombre de la universidad
        JLabel lblUniversidad = new JLabel("Universidad de Sonora", SwingConstants.CENTER);
        lblUniversidad.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblUniversidad.setForeground(AZUL_UNISON);
        lblUniversidad.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblUniversidad.setBorder(BorderFactory.createEmptyBorder(20, 0, 5, 0));

        // Título del proyecto
        JLabel lblTitulo = new JLabel("Sistema de Monitoreo", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(AZUL_OSCURO_UNISON);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        // Subtítulo
        JLabel lblSubtitulo = new JLabel("Desarrollo de Sistemas III", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitulo.setForeground(new Color(100, 100, 100));
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nombre
        JLabel lblNombre = new JLabel("Santoscoy Santillan Noe Sebastian", SwingConstants.CENTER);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblNombre.setForeground(DORADO_OSCURO_UNISON);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNombre.setBorder(BorderFactory.createEmptyBorder(30, 0, 40, 0));

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        panelBotones.setBackground(Color.WHITE);
        panelBotones.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Botón Monitor
        JButton btnMonitor = crearBotonEstilizado("Monitor", AZUL_UNISON);
        btnMonitor.addActionListener(e -> ventanaPrincipal.mostrarVista("MONITOR"));

        // Botón Histórico
        JButton btnHistorico = crearBotonEstilizado("Histórico", DORADO_UNISON);
        btnHistorico.addActionListener(e -> ventanaPrincipal.mostrarVista("HISTORICO"));

        panelBotones.add(btnMonitor);
        panelBotones.add(btnHistorico);

        // Agregar componentes
        panelCentral.add(lblLogo);
        panelCentral.add(lblUniversidad);
        panelCentral.add(lblTitulo);
        panelCentral.add(lblSubtitulo);
        panelCentral.add(lblNombre);
        panelCentral.add(panelBotones);

        add(panelCentral, BorderLayout.CENTER);
    }

    private JLabel crearLogo() {
        try {
            java.net.URL imgURL = getClass().getResource("/images/logo_unison.png");

            if (imgURL != null) {
                ImageIcon iconoOriginal = new ImageIcon(imgURL);
                Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(440, 359, Image.SCALE_SMOOTH);
                ImageIcon iconoEscalado = new ImageIcon(imagenEscalada);
                JLabel lblImagen = new JLabel(iconoEscalado, SwingConstants.CENTER);
                return lblImagen;
            } else {
                // Si no encuentra la imagen en resources, intenta archivo externo
                ImageIcon iconoOriginal = new ImageIcon("logo_unison.png");
                if (iconoOriginal.getIconWidth() > 0) {
                    Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(440, 359, Image.SCALE_SMOOTH);
                    ImageIcon iconoEscalado = new ImageIcon(imagenEscalada);
                    JLabel lblImagen = new JLabel(iconoEscalado, SwingConstants.CENTER);
                    return lblImagen;
                }
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo: " + e.getMessage());
        }
        JLabel lblLogo = new JLabel("UNISON", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 80));
        lblLogo.setForeground(AZUL_UNISON);
        return lblLogo;
    }

    private JButton crearBotonEstilizado(String texto, Color color) {
        RoundedButton boton = new RoundedButton(texto, 4);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        boton.setPreferredSize(new Dimension(220, 65));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return boton;
    }
}