import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class Servidor {
    private static final int PUERTO = 5000;
    private static final String DB_URL = "jdbc:sqlite:monitorBD.db";
    private static SecretKey claveAES;
    private static int clienteContador = 0;

    public static void main(String[] args) {

        System.out.println("SERVIDOR DE MONITOREO");

        System.out.println();

        // Inicializar clave AES
        inicializarClave();

        // Inicializar base de datos
        inicializarBaseDatos();

        // Iniciar servidor
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en puerto " + PUERTO);
            System.out.println("Esperando conexiones.");
            System.out.println("═══════════════════════════════════════════");
            System.out.println();

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                clienteContador++;

                String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                System.out.println("─────────────────────────────────────────");
                System.out.println("[" + timestamp + "] 🔌 NUEVO CLIENTE CONECTADO");
                System.out.println("Cliente #" + clienteContador);
                System.out.println("IP: " + clienteSocket.getInetAddress().getHostAddress());
                System.out.println("─────────────────────────────────────────");
                System.out.println();

                // Crear hilo para manejar el cliente
                new Thread(new ManejadorCliente(clienteSocket, clienteContador)).start();
            }

        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void inicializarClave() {
        String claveBase64 = "MTIzNDU2Nzg5MDEyMzQ1Ng==";
        byte[] claveBytes = Base64.getDecoder().decode(claveBase64);
        claveAES = new SecretKeySpec(claveBytes, "AES");
    }

    private static void inicializarBaseDatos() {
        try {
            Class.forName("org.sqlite.JDBC");

            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                String sql = "CREATE TABLE IF NOT EXISTS datos_sensor (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "x INTEGER NOT NULL," +
                        "y INTEGER NOT NULL," +
                        "z INTEGER NOT NULL," +
                        "fecha_de_captura TEXT NOT NULL," +
                        "hora_de_captura TEXT NOT NULL)";

                Statement stmt = conn.createStatement();
                stmt.execute(sql);

                System.out.println("Base de datos SQLite inicializada");
                System.out.println("Base de datos: monitorBD.db");
                System.out.println("Ubicación: " + new File("monitorBD.db").getAbsolutePath());
                System.out.println("Tabla: datos_sensor");
                System.out.println("   Columnas:");
                System.out.println("   - id: INTEGER PRIMARY KEY AUTOINCREMENT");
                System.out.println("   - x: INTEGER NOT NULL");
                System.out.println("   - y: INTEGER NOT NULL");
                System.out.println("   - z: INTEGER NOT NULL");
                System.out.println("   - fecha_de_captura: TEXT (DD-MM-AAAA)");
                System.out.println("   - hora_de_captura: TEXT (HH:MM:SS)");
                System.out.println();

            } catch (SQLException e) {
                System.err.println("Error SQL: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: Driver SQLite no encontrado!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    static class ManejadorCliente implements Runnable {
        private Socket socket;
        private int numeroCliente;
        private ObjectOutputStream salida;
        private ObjectInputStream entrada;

        public ManejadorCliente(Socket socket, int numeroCliente) {
            this.socket = socket;
            this.numeroCliente = numeroCliente;
        }

        @Override
        public void run() {
            try {
                salida = new ObjectOutputStream(socket.getOutputStream());
                entrada = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    Mensaje mensaje = (Mensaje) entrada.readObject();

                    String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());

                    switch (mensaje.getTipo()) {
                        case "GUARDAR":
                            System.out.println("─────────────────────────────────────────");
                            System.out.println("[" + timestamp + "] Guardando datos");
                            System.out.println("Cliente #" + numeroCliente);

                            guardarDato(mensaje.getContenido());

                            Mensaje respuesta = new Mensaje("OK", "Dato guardado");
                            salida.writeObject(respuesta);
                            salida.flush();

                            System.out.println("Dato guardado en la base de datos");
                            System.out.println("─────────────────────────────────────────");
                            System.out.println();
                            break;

                        case "CONSULTAR":
                            System.out.println("─────────────────────────────────────────");
                            System.out.println("[" + timestamp + "] CONSULTA DE DATOS");
                            System.out.println("Cliente #" + numeroCliente);

                            String[] rango = mensaje.getContenido().split("\\|");
                            Timestamp inicio = Timestamp.valueOf(rango[0]);
                            Timestamp fin = Timestamp.valueOf(rango[1]);

                            System.out.println("Rango: " + inicio + " - " + fin);

                            List<DatoSensor> datos = consultarDatos(inicio, fin);

                            // Serializar y encriptar
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ObjectOutputStream oos = new ObjectOutputStream(baos);
                            oos.writeObject(datos);
                            oos.close();

                            String datosEncriptados = encriptar(baos.toByteArray());
                            Mensaje respuestaDatos = new Mensaje("DATOS", datosEncriptados);

                            salida.writeObject(respuestaDatos);
                            salida.flush();

                            System.out.println("Enviados " + datos.size() + " registros (encriptados)");
                            System.out.println("─────────────────────────────────────────");
                            System.out.println();
                            break;
                    }
                }

            } catch (EOFException e) {
                String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                System.out.println("─────────────────────────────────────────");
                System.out.println("[" + timestamp + "] CLIENTE DESCONECTADO");
                System.out.println("Cliente #" + numeroCliente);
                System.out.println("─────────────────────────────────────────");
                System.out.println();
            } catch (Exception e) {
                System.err.println("Error con cliente #" + numeroCliente + ": " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void guardarDato(String datosEncriptadosRed) throws Exception {
            // Desencriptar datos recibidos de la red
            byte[] datosDesencriptados = desencriptar(datosEncriptadosRed);

            // Deserializar
            ByteArrayInputStream bais = new ByteArrayInputStream(datosDesencriptados);
            ObjectInputStream ois = new ObjectInputStream(bais);
            DatoSensor dato = (DatoSensor) ois.readObject();
            ois.close();

            // Formatear fecha como DD-MM-AAAA
            java.text.SimpleDateFormat formatoFecha = new java.text.SimpleDateFormat("dd-MM-yyyy");
            String fechaTexto = formatoFecha.format(dato.getFecha());

            // Formatear hora como HH:MM:SS
            java.text.SimpleDateFormat formatoHora = new java.text.SimpleDateFormat("HH:mm:ss");
            String horaTexto = formatoHora.format(dato.getHora());

            System.out.println("Dato recibido: " + dato);
            System.out.println("Fecha formateada: " + fechaTexto);
            System.out.println("Hora formateada: " + horaTexto);

            // Guardar en BD como TEXT
            String sql = "INSERT INTO datos_sensor (x, y, z, fecha_de_captura, hora_de_captura) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, dato.getValor1());
                pstmt.setInt(2, dato.getValor2());
                pstmt.setInt(3, dato.getValor3());
                pstmt.setString(4, fechaTexto);  // Guardar como TEXT
                pstmt.setString(5, horaTexto);   // Guardar como TEXT
                pstmt.executeUpdate();

                System.out.println("Guardado → x=" + dato.getValor1() +
                        ", y=" + dato.getValor2() +
                        ", z=" + dato.getValor3());
                System.out.println("Guardado → Fecha: " + fechaTexto +
                        ", Hora: " + horaTexto);
            }
        }

        private List<DatoSensor> consultarDatos(Timestamp inicio, Timestamp fin) throws Exception {
            List<DatoSensor> datos = new ArrayList<>();

            // Formatear timestamps para comparación con TEXT
            java.text.SimpleDateFormat formatoFecha = new java.text.SimpleDateFormat("dd-MM-yyyy");
            java.text.SimpleDateFormat formatoHora = new java.text.SimpleDateFormat("HH:mm:ss");

            String fechaInicioTexto = formatoFecha.format(new Date(inicio.getTime()));
            String fechaFinTexto = formatoFecha.format(new Date(fin.getTime()));
            String horaInicioTexto = formatoHora.format(new Date(inicio.getTime()));
            String horaFinTexto = formatoHora.format(new Date(fin.getTime()));

            System.out.println("Buscando desde: " + fechaInicioTexto + " " + horaInicioTexto);
            System.out.println("Hasta: " + fechaFinTexto + " " + horaFinTexto);

            // Consulta simplificada comparando como texto
            String sql = "SELECT id, x, y, z, fecha_de_captura, hora_de_captura " +
                    "FROM datos_sensor " +
                    "WHERE (fecha_de_captura > ? OR (fecha_de_captura = ? AND hora_de_captura >= ?)) " +
                    "  AND (fecha_de_captura < ? OR (fecha_de_captura = ? AND hora_de_captura <= ?)) " +
                    "ORDER BY fecha_de_captura, hora_de_captura ASC";

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, fechaInicioTexto);
                pstmt.setString(2, fechaInicioTexto);
                pstmt.setString(3, horaInicioTexto);
                pstmt.setString(4, fechaFinTexto);
                pstmt.setString(5, fechaFinTexto);
                pstmt.setString(6, horaFinTexto);

                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    // Leer fecha y hora como TEXT
                    String fechaTexto = rs.getString("fecha_de_captura");
                    String horaTexto = rs.getString("hora_de_captura");

                    // Convertir de texto a Date y Time para el objeto
                    java.sql.Date fecha = new java.sql.Date(formatoFecha.parse(fechaTexto).getTime());
                    java.sql.Time hora = new java.sql.Time(formatoHora.parse(horaTexto).getTime());

                    DatoSensor dato = new DatoSensor(
                            fecha,
                            hora,
                            rs.getInt("x"),
                            rs.getInt("y"),
                            rs.getInt("z")
                    );
                    datos.add(dato);
                }

                System.out.println("Registros encontrados: " + datos.size());
            }

            return datos;
        }

        private String encriptar(byte[] datos) throws Exception {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, claveAES);
            byte[] encriptado = cipher.doFinal(datos);
            return Base64.getEncoder().encodeToString(encriptado);
        }

        private byte[] desencriptar(String datosEncriptados) throws Exception {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, claveAES);
            byte[] decoded = Base64.getDecoder().decode(datosEncriptados);
            return cipher.doFinal(decoded);
        }
    }
}