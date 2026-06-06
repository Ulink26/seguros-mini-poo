package seguros.model;
import java.time.LocalDate;
public class PolizaGastosMedicos extends Poliza{
    private static final long serialVersionUID=1L;
    private final String asegurado;
    //constructor
    public PolizaGastosMedicos(int id, int clienteId, String numero, String aseguradora, LocalDate fechaInicio, LocalDate fechaVencimiento, double prima, String asegurado) {
        super(id, clienteId, numero, aseguradora, fechaInicio, fechaVencimiento, prima);
        this.asegurado = asegurado;
    }
    @Override
    public String getTipo(){
        return "GASTOS_MEDICOS";
    }
    @Override
    public String getDetalle(){
        return "Asegurado: "+asegurado;
    }
}