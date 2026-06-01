package seguros.persistence;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import seguros.model.Cliente;
import seguros.model.Poliza;
import seguros.model.PolizaAuto;
import seguros.model.PolizaGastosMedicos;
import seguros.model.PolizaVida;
import seguros.model.Recibo;

/**
 * Persistencia principal del sistema. Los tres archivos TXT funcionan como
 * base de datos sencilla; el respaldo BIN cubre la persistencia binaria.
 */
public class TextDatabase {
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final Path dataDirectory;
    private final Path backupDirectory;
    private final Path clientesFile;
    private final Path polizasFile;
    private final Path recibosFile;

    public TextDatabase(Path dataDirectory) throws IOException {
        this.dataDirectory = dataDirectory;
        this.backupDirectory = dataDirectory.resolve("respaldos");
        this.clientesFile = dataDirectory.resolve("clientes.txt");
        this.polizasFile = dataDirectory.resolve("polizas.txt");
        this.recibosFile = dataDirectory.resolve("recibos.txt");
        Files.createDirectories(backupDirectory);
        createIfMissing(clientesFile, "# id|nombre|telefono|email");
        createIfMissing(polizasFile,
                "# id|tipo|clienteId|numero|aseguradora|inicio|vencimiento|prima|detalle");
        createIfMissing(recibosFile, "# id|polizaId|vencimiento|monto|pagado");
    }

    public ArrayList<Cliente> loadClientes() throws IOException {
        ArrayList<Cliente> result = new ArrayList<>();
        for (String line : dataLines(clientesFile)) {
            List<String> row = TextCodec.split(line);
            requireColumns(row, 4, clientesFile);
            result.add(new Cliente(intValue(row, 0), row.get(1), row.get(2), row.get(3)));
        }
        return result;
    }

    public ArrayList<Poliza> loadPolizas() throws IOException {
        ArrayList<Poliza> result = new ArrayList<>();
        for (String line : dataLines(polizasFile)) {
            List<String> row = TextCodec.split(line);
            requireColumns(row, 9, polizasFile);
            int id = intValue(row, 0);
            int clienteId = intValue(row, 2);
            LocalDate inicio = LocalDate.parse(row.get(5));
            LocalDate vencimiento = LocalDate.parse(row.get(6));
            double prima = doubleValue(row, 7);
            switch (row.get(1)) {
                case "AUTO":
                    result.add(new PolizaAuto(id, clienteId, row.get(3), row.get(4),
                            inicio, vencimiento, prima, row.get(8)));
                    break;
                case "VIDA":
                    result.add(new PolizaVida(id, clienteId, row.get(3), row.get(4),
                            inicio, vencimiento, prima, row.get(8)));
                    break;
                case "GASTOS_MEDICOS":
                    result.add(new PolizaGastosMedicos(id, clienteId, row.get(3), row.get(4),
                            inicio, vencimiento, prima, row.get(8)));
                    break;
                default:
                    throw new IOException("Tipo de poliza desconocido: " + row.get(1));
            }
        }
        return result;
    }

    public ArrayList<Recibo> loadRecibos() throws IOException {
        ArrayList<Recibo> result = new ArrayList<>();
        for (String line : dataLines(recibosFile)) {
            List<String> row = TextCodec.split(line);
            requireColumns(row, 5, recibosFile);
            result.add(new Recibo(
                    intValue(row, 0),
                    intValue(row, 1),
                    LocalDate.parse(row.get(2)),
                    doubleValue(row, 3),
                    Boolean.parseBoolean(row.get(4))));
        }
        return result;
    }

    public void saveAll(List<Cliente> clientes, List<Poliza> polizas, List<Recibo> recibos)
            throws IOException {
        List<String> clienteRows = new ArrayList<>();
        clienteRows.add("# id|nombre|telefono|email");
        for (Cliente cliente : clientes) {
            clienteRows.add(TextCodec.row(cliente.getId(), cliente.getNombre(),
                    cliente.getTelefono(), cliente.getEmail()));
        }

        List<String> polizaRows = new ArrayList<>();
        polizaRows.add("# id|tipo|clienteId|numero|aseguradora|inicio|vencimiento|prima|detalle");
        for (Poliza poliza : polizas) {
            polizaRows.add(TextCodec.row(poliza.getId(), poliza.getTipo(), poliza.getClienteId(),
                    poliza.getNumero(), poliza.getAseguradora(), poliza.getFechaInicio(),
                    poliza.getFechaVencimiento(), poliza.getPrima(), rawDetail(poliza)));
        }

        List<String> reciboRows = new ArrayList<>();
        reciboRows.add("# id|polizaId|vencimiento|monto|pagado");
        for (Recibo recibo : recibos) {
            reciboRows.add(TextCodec.row(recibo.getId(), recibo.getPolizaId(),
                    recibo.getFechaVencimiento(), recibo.getMonto(), recibo.isPagado()));
        }

        atomicWrite(clientesFile, clienteRows);
        atomicWrite(polizasFile, polizaRows);
        atomicWrite(recibosFile, reciboRows);
    }

    public Path createBinaryBackup(List<Cliente> clientes, List<Poliza> polizas, List<Recibo> recibos)
            throws IOException {
        Path output = backupDirectory.resolve("seguros-" + FILE_DATE.format(LocalDateTime.now()) + ".bin");
        try (ObjectOutputStream stream = new ObjectOutputStream(Files.newOutputStream(output))) {
            stream.writeObject(new Snapshot(clientes, polizas, recibos));
        }
        return output;
    }

    public Path createTextReport(List<Cliente> clientes, List<Poliza> polizas, List<Recibo> recibos)
            throws IOException {
        LocalDate today = LocalDate.now();
        Path output = backupDirectory.resolve("reporte-" + FILE_DATE.format(LocalDateTime.now()) + ".txt");
        List<String> lines = new ArrayList<>();
        lines.add("SEGUROS MINI - REPORTE");
        lines.add("Generado: " + LocalDateTime.now());
        lines.add("");
        lines.add("Clientes: " + clientes.size());
        lines.add("Polizas: " + polizas.size());
        lines.add("Primas: " + recibos.size());
        lines.add("");
        lines.add("PRIMAS VENCIDAS");
        for (Recibo recibo : recibos) {
            if ("VENCIDO".equals(recibo.getEstado(today))) {
                lines.add("Recibo " + recibo.getId() + " | poliza " + recibo.getPolizaId()
                        + " | vence " + recibo.getFechaVencimiento() + " | $" + recibo.getMonto());
            }
        }
        atomicWrite(output, lines);
        return output;
    }

    private String rawDetail(Poliza poliza) {
        String detail = poliza.getDetalle();
        int separator = detail.indexOf(": ");
        return separator >= 0 ? detail.substring(separator + 2) : detail;
    }

    private List<String> dataLines(Path file) throws IOException {
        List<String> result = new ArrayList<>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (!line.trim().isEmpty() && !line.startsWith("#")) {
                result.add(line);
            }
        }
        return result;
    }

    private void atomicWrite(Path file, List<String> lines) throws IOException {
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(temporary, lines, StandardCharsets.UTF_8);
        try {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void createIfMissing(Path file, String header) throws IOException {
        if (!Files.exists(file)) {
            Files.write(file, Arrays.asList(header), StandardCharsets.UTF_8);
        }
    }

    private void requireColumns(List<String> row, int columns, Path file) throws IOException {
        if (row.size() != columns) {
            throw new IOException("Fila invalida en " + file + ": se esperaban " + columns + " columnas.");
        }
    }

    private int intValue(List<String> row, int index) throws IOException {
        try {
            return Integer.parseInt(row.get(index));
        } catch (NumberFormatException ex) {
            throw new IOException("Numero entero invalido: " + row.get(index), ex);
        }
    }

    private double doubleValue(List<String> row, int index) throws IOException {
        try {
            return Double.parseDouble(row.get(index));
        } catch (NumberFormatException ex) {
            throw new IOException("Importe invalido: " + row.get(index), ex);
        }
    }

    private static class Snapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        private final ArrayList<Cliente> clientes;
        private final ArrayList<Poliza> polizas;
        private final ArrayList<Recibo> recibos;

        Snapshot(List<Cliente> clientes, List<Poliza> polizas, List<Recibo> recibos) {
            this.clientes = new ArrayList<>(clientes);
            this.polizas = new ArrayList<>(polizas);
            this.recibos = new ArrayList<>(recibos);
        }
    }
}
