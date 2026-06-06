package seguros.model;
import java.time.LocalDate;

public class PolizaAuto extends Poliza{
    private static final long serialVersionUID=1L;
    private final String vehiculo;
    //constructor
    public PolizaAuto(int id, int clienteId, String numero, String aseguradora,
                      LocalDate fechaInicio, LocalDate fechaVencimiento, double prima,
                      String vehiculo) {
        super(id, clienteId, numero, aseguradora, fechaInicio, fechaVencimiento, prima);
        this.vehiculo = vehiculo;
    }
    @Override
    public String getTipo() {
        return "AUTO";
    }
    @Override
    public String getDetalle(){
        return vehiculo;
    }
}