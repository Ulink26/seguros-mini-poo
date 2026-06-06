package seguros.model;
import java.io.Serializable;
import java.time.LocalDate;

//este código es para determinar si la prima asociada a una poliza está pagada o pendiente

public class Recibo implements Serializable{
    private static final long serialVersionUID=1L;
    //datos del recibo
    private final int id;
    private final int polizaId;
    private final LocalDate fechaVencimiento;
    private final double monto;
    private boolean pagado;//estado del pago

    //constructor del recibo
    public Recibo(int id,int polizaId, LocalDate fechaVencimiento,double monto,boolean pagado){
        this.id=id;
        this.polizaId=polizaId;
        this.fechaVencimiento=fechaVencimiento;
        this.monto=monto;
        this.pagado=pagado;
    }
    //getters

    public int getId() {
        return id;
    }

    public int getPolizaId() {
        return polizaId;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public double getMonto() {
        return monto;
    }

    public boolean isPagado() {
        return pagado;
    }
    public void marcarPagado(){
        pagado=true;
    }
    public String getEstado(LocalDate hoy){
        if(pagado)return "PAGADO";
        return fechaVencimiento.isBefore(hoy)?"VENCIDO":"PENDIENTE";
    }
}