package seguros.model;
import java.time.LocalDate;
public class PolizaVida extends Poliza{
    private static final long serialVersionUID=1L;
    private final String beneficiario;
    //constructor
    public PolizaVida(int id, int clienteId, String numero, String aseguradora, LocalDate fechaInicio, LocalDate fechaVencimiento, double prima, String beneficiario){
        super(id, clienteId, numero, aseguradora, fechaInicio, fechaVencimiento, prima);
        this.beneficiario = beneficiario;
    }
    @Override
    public String getTipo(){
        return "VIDA";
    }
    @Override
    public String getDetalle(){
        return "Beneficiario: "+beneficiario;
    }
}