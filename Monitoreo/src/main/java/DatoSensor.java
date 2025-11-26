import java.io.Serializable;
import java.sql.Timestamp;

class DatoSensor implements Serializable {
    private static final long serialVersionUID = 1L;
    private java.sql.Date fecha;
    private java.sql.Time hora;
    private int valor1;
    private int valor2;
    private int valor3;

    public DatoSensor(java.sql.Date fecha, java.sql.Time hora, int valor1, int valor2, int valor3) {
        this.fecha = fecha;
        this.hora = hora;
        this.valor1 = valor1;
        this.valor2 = valor2;
        this.valor3 = valor3;
    }

    public java.sql.Date getFecha() { return fecha; }
    public java.sql.Time getHora() { return hora; }
    public int getValor1() { return valor1; }
    public int getValor2() { return valor2; }
    public int getValor3() { return valor3; }

    // Para compatibilidad - retorna un Timestamp combinado
    public Timestamp getTimestamp() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(fecha);
        java.util.Calendar calHora = java.util.Calendar.getInstance();
        calHora.setTime(hora);
        cal.set(java.util.Calendar.HOUR_OF_DAY, calHora.get(java.util.Calendar.HOUR_OF_DAY));
        cal.set(java.util.Calendar.MINUTE, calHora.get(java.util.Calendar.MINUTE));
        cal.set(java.util.Calendar.SECOND, calHora.get(java.util.Calendar.SECOND));
        cal.set(java.util.Calendar.MILLISECOND, calHora.get(java.util.Calendar.MILLISECOND));
        return new Timestamp(cal.getTimeInMillis());
    }

    @Override
    public String toString() {
        return String.format("DatoSensor[%s %s, x=%d, y=%d, z=%d]",
                fecha, hora, valor1, valor2, valor3);
    }
}