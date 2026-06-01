package seguros;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import seguros.persistence.TextDatabase;
import seguros.service.SeguroService;

/**
 * Prueba rapida ejecutable sin interfaz grafica.
 */
public final class SmokeTest {
    private SmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        SeguroService service = new SeguroService(new TextDatabase(Paths.get("data")));
        if (service.getClientes().isEmpty() || service.getPolizas().isEmpty()
                || service.getRecibos().isEmpty()) {
            throw new IllegalStateException("Los datos iniciales no fueron creados.");
        }
        Path report = service.createReport();
        Path backup = service.createBackup();
        if (Files.size(report) == 0 || Files.size(backup) == 0) {
            throw new IllegalStateException("El reporte o el respaldo quedaron vacios.");
        }
        System.out.println("OK clientes=" + service.getClientes().size()
                + " polizas=" + service.getPolizas().size()
                + " primas=" + service.getRecibos().size());
        System.out.println("OK reporte=" + report);
        System.out.println("OK respaldo=" + backup);
        service.close();
    }
}
