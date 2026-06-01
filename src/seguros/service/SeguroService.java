package seguros.service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import seguros.model.Cliente;
import seguros.model.Poliza;
import seguros.model.PolizaAuto;
import seguros.model.PolizaGastosMedicos;
import seguros.model.PolizaVida;
import seguros.model.Recibo;
import seguros.persistence.TextDatabase;

/**
 * Reglas del negocio. Mantiene colecciones dinamicas ArrayList y coordina
 * persistencia, validaciones y mantenimiento en segundo plano.
 */
public class SeguroService {
    private final TextDatabase database;
    private final ArrayList<Cliente> clientes;
    private final ArrayList<Poliza> polizas;
    private final ArrayList<Recibo> recibos;
    private ScheduledExecutorService scheduler;

    public SeguroService(TextDatabase database) throws IOException {
        this.database = database;
        this.clientes = database.loadClientes();
        this.polizas = database.loadPolizas();
        this.recibos = database.loadRecibos();
        seedIfEmpty();
    }

    public synchronized ArrayList<Cliente> getClientes() {
        return new ArrayList<>(clientes);
    }

    public synchronized ArrayList<Poliza> getPolizas() {
        return new ArrayList<>(polizas);
    }

    public synchronized ArrayList<Recibo> getRecibos() {
        return new ArrayList<>(recibos);
    }

    public synchronized Cliente addCliente(String nombre, String telefono, String email) throws IOException {
        require(nombre, "El nombre del cliente es obligatorio.");
        if (!email.trim().isEmpty() && !email.contains("@")) {
            throw new IllegalArgumentException("El correo debe contener @.");
        }
        Cliente cliente = new Cliente(nextClienteId(), nombre.trim(), telefono.trim(), email.trim());
        clientes.add(cliente);
        save();
        return cliente;
    }

    public synchronized Poliza addPoliza(String tipo, Cliente cliente, String numero,
            String aseguradora, LocalDate inicio, LocalDate vencimiento, double prima, String detalle)
            throws IOException {
        if (cliente == null) {
            throw new IllegalArgumentException("Selecciona un cliente.");
        }
        require(numero, "El numero de poliza es obligatorio.");
        require(aseguradora, "La aseguradora es obligatoria.");
        require(detalle, "Captura el detalle de la poliza.");
        if (vencimiento.isBefore(inicio)) {
            throw new IllegalArgumentException("El vencimiento no puede ser anterior al inicio.");
        }
        if (prima <= 0) {
            throw new IllegalArgumentException("La prima debe ser mayor a cero.");
        }
        for (Poliza existing : polizas) {
            if (existing.getNumero().equalsIgnoreCase(numero.trim())) {
                throw new IllegalArgumentException("Ya existe una poliza con ese numero.");
            }
        }

        int id = nextPolizaId();
        Poliza poliza;
        switch (tipo) {
            case "AUTO":
                poliza = new PolizaAuto(id, cliente.getId(), numero.trim(), aseguradora.trim(),
                        inicio, vencimiento, prima, detalle.trim());
                break;
            case "VIDA":
                poliza = new PolizaVida(id, cliente.getId(), numero.trim(), aseguradora.trim(),
                        inicio, vencimiento, prima, detalle.trim());
                break;
            case "GASTOS_MEDICOS":
                poliza = new PolizaGastosMedicos(id, cliente.getId(), numero.trim(), aseguradora.trim(),
                        inicio, vencimiento, prima, detalle.trim());
                break;
            default:
                throw new IllegalArgumentException("Tipo de poliza desconocido.");
        }
        polizas.add(poliza);
        recibos.add(new Recibo(nextReciboId(), id, vencimiento, prima, false));
        save();
        return poliza;
    }

    public synchronized void payReceipt(int receiptId) throws IOException {
        for (Recibo recibo : recibos) {
            if (recibo.getId() == receiptId) {
                recibo.marcarPagado();
                save();
                return;
            }
        }
        throw new IllegalArgumentException("No se encontro el recibo seleccionado.");
    }

    public synchronized Cliente findCliente(int id) {
        for (Cliente cliente : clientes) {
            if (cliente.getId() == id) {
                return cliente;
            }
        }
        return null;
    }

    public synchronized Poliza findPoliza(int id) {
        for (Poliza poliza : polizas) {
            if (poliza.getId() == id) {
                return poliza;
            }
        }
        return null;
    }

    public synchronized int countRecibos(String estado) {
        int count = 0;
        LocalDate today = LocalDate.now();
        for (Recibo recibo : recibos) {
            if (estado.equals(recibo.getEstado(today))) {
                count++;
            }
        }
        return count;
    }

    public synchronized int countPolizas(String estado) {
        int count = 0;
        LocalDate today = LocalDate.now();
        for (Poliza poliza : polizas) {
            if (estado.equals(poliza.getEstado(today))) {
                count++;
            }
        }
        return count;
    }

    public synchronized Path createBackup() throws IOException {
        return database.createBinaryBackup(clientes, polizas, recibos);
    }

    public synchronized Path createReport() throws IOException {
        return database.createTextReport(clientes, polizas, recibos);
    }

    /**
     * El hilo programado ejemplifica concurrencia: respalda la informacion sin
     * bloquear la interfaz y solicita un refresco visual mediante Swing.
     */
    public synchronized void startMaintenance(Runnable uiRefresh) {
        if (scheduler != null) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "seguros-mini-maintenance");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                createBackup();
                SwingUtilities.invokeLater(uiRefresh);
            } catch (IOException ex) {
                System.err.println("No fue posible crear el respaldo automatico: " + ex.getMessage());
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    public synchronized void close() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private void seedIfEmpty() throws IOException {
        if (!clientes.isEmpty() || !polizas.isEmpty() || !recibos.isEmpty()) {
            return;
        }
        LocalDate today = LocalDate.now();
        Cliente ana = new Cliente(1, "Ana Martinez", "4491234567", "ana@email.com");
        Cliente jorge = new Cliente(2, "Jorge Ramirez", "4497654321", "jorge@email.com");
        clientes.add(ana);
        clientes.add(jorge);

        polizas.add(new PolizaAuto(1, ana.getId(), "AUTO-001", "GNP",
                today.minusMonths(10), today.plusMonths(2), 8500, "Toyota Corolla 2021"));
        polizas.add(new PolizaVida(2, jorge.getId(), "VIDA-001", "MetLife",
                today.minusYears(1), today.minusDays(5), 12000, "Laura Ramirez"));
        polizas.add(new PolizaGastosMedicos(3, ana.getId(), "GM-001", "AXA",
                today.minusMonths(3), today.plusDays(15), 9500, "Ana Martinez"));

        recibos.add(new Recibo(1, 1, today.plusMonths(2), 8500, false));
        recibos.add(new Recibo(2, 2, today.minusDays(5), 12000, false));
        recibos.add(new Recibo(3, 3, today.plusDays(15), 9500, false));
        save();
    }

    private void save() throws IOException {
        database.saveAll(clientes, polizas, recibos);
    }

    private int nextClienteId() {
        int max = 0;
        for (Cliente value : clientes) {
            max = Math.max(max, value.getId());
        }
        return max + 1;
    }

    private int nextPolizaId() {
        int max = 0;
        for (Poliza value : polizas) {
            max = Math.max(max, value.getId());
        }
        return max + 1;
    }

    private int nextReciboId() {
        int max = 0;
        for (Recibo value : recibos) {
            max = Math.max(max, value.getId());
        }
        return max + 1;
    }

    private void require(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
