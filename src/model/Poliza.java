package seguros.model;
import java.io.Serializable;
import java.time.LocalDate;

public abstract class Poliza implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final int clienteId;
    private final String numero;
    private final String aseguradora;
    private final LocalDate fechaInicio;
    private final LocalDate fechaVencimiento;
    private final double prima;

    // Constructor principal
    protected Poliza(int id, int clienteId, String numero, String aseguradora,
                     LocalDate fechaInicio, LocalDate fechaVencimiento, double prima) {
        this.id = id;
        this.clienteId = clienteId;
        this.numero = numero;
        this.aseguradora = aseguradora;
        this.fechaInicio = fechaInicio;
        this.fechaVencimiento = fechaVencimiento;
        this.prima = prima;
    }

    // Métodos abstractos para las clases hijas
    public abstract String getTipo();
    public abstract String getDetalle();

    // Getters
    public int getId() { return id; }
    public int getClienteId() { return clienteId; }
    public String getNumero() { return numero; }
    public String getAseguradora() { return aseguradora; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public double getPrima() { return prima; }

    public String getEstado(LocalDate hoy) {
        return fechaVencimiento.isBefore(hoy) ? "VENCIDA" : "ACTIVA";
    }
}