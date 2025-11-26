import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ClienteSocket {
    private String host;
    private int puerto;
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private SecretKey claveAES;

    public ClienteSocket(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
        inicializarClave();
    }

    private void inicializarClave() {
        // Clave AES fija para simplificar (en producción usar intercambio de claves)
        String claveBase64 = "MTIzNDU2Nzg5MDEyMzQ1Ng=="; // "1234567890123456" en Base64
        byte[] claveBytes = Base64.getDecoder().decode(claveBase64);
        claveAES = new SecretKeySpec(claveBytes, "AES");
    }

    public boolean conectar() {
        try {
            socket = new Socket(host, puerto);
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
            System.out.println("Conectado al servidor " + host + ":" + puerto);
            return true;
        } catch (IOException e) {
            System.err.println("Error al conectar: " + e.getMessage());
            return false;
        }
    }

    public void desconectar() {
        try {
            if (salida != null) salida.close();
            if (entrada != null) entrada.close();
            if (socket != null) socket.close();
            System.out.println("🔌 Desconectado del servidor");
        } catch (IOException e) {
            System.err.println("Error al desconectar: " + e.getMessage());
        }
    }

    public void enviarDato(DatoSensor dato) {
        try {
            // Serializar el objeto
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(dato);
            oos.close();
            byte[] datosSerializados = baos.toByteArray();

            // Encriptar
            String datosEncriptados = encriptar(datosSerializados);

            // Crear mensaje
            Mensaje mensaje = new Mensaje("GUARDAR", datosEncriptados);

            // Enviar
            salida.writeObject(mensaje);
            salida.flush();

            // Recibir confirmación
            Mensaje respuesta = (Mensaje) entrada.readObject();
            if (!"OK".equals(respuesta.getTipo())) {
                System.err.println("Error al guardar: " + respuesta.getContenido());
            }

        } catch (Exception e) {
            System.err.println("Error al enviar dato: " + e.getMessage());
        }
    }

    public List<DatoSensor> consultarDatos(Timestamp inicio, Timestamp fin) {
        try {
            // Crear solicitud
            String solicitud = inicio.toString() + "|" + fin.toString();
            Mensaje mensaje = new Mensaje("CONSULTAR", solicitud);

            // Enviar solicitud
            salida.writeObject(mensaje);
            salida.flush();

            // Recibir respuesta
            Mensaje respuesta = (Mensaje) entrada.readObject();

            if ("DATOS".equals(respuesta.getTipo())) {
                // Desencriptar
                byte[] datosDesencriptados = desencriptar(respuesta.getContenido());

                // Deserializar
                ByteArrayInputStream bais = new ByteArrayInputStream(datosDesencriptados);
                ObjectInputStream ois = new ObjectInputStream(bais);
                @SuppressWarnings("unchecked")
                List<DatoSensor> datos = (List<DatoSensor>) ois.readObject();
                ois.close();

                return datos;
            } else {
                System.err.println("Error en consulta: " + respuesta.getContenido());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            System.err.println("Error al consultar datos: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
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