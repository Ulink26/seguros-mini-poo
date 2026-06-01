package seguros.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Prima pendiente o pagada asociada a una poliza.
 */
public class Recibo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final int polizaId;
    private final LocalDate fechaVencimiento;
    private final double monto;
    private boolean pagado;

    public Recibo(int id, int polizaId, LocalDate fechaVencimiento, double monto, boolean pagado) {
        this.id = id;
        this.polizaId = polizaId;
        this.fechaVencimiento = fechaVencimiento;
        this.monto = monto;
        this.pagado = pagado;
    }

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

    public void marcarPagado() {
        pagado = true;
    }

    public String getEstado(LocalDate hoy) {
        if (pagado) {
            return "PAGADO";
        }
        return fechaVencimiento.isBefore(hoy) ? "VENCIDO" : "PENDIENTE";
    }
}
