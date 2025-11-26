import java.io.Serializable;

class Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;
    private String tipo;
    private String contenido;

    public Mensaje(String tipo, String contenido) {
        this.tipo = tipo;
        this.contenido = contenido;
    }

    public String getTipo() { return tipo; }
    public String getContenido() { return contenido; }
}