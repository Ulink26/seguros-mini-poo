package seguros;

import java.nio.file.Paths;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import seguros.persistence.TextDatabase;
import seguros.service.SeguroService;
import seguros.ui.MainFrame;

/**
 * Punto de entrada de Seguros Mini.
 */
public final class App {
    private App() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                TextDatabase database = new TextDatabase(Paths.get("data"));
                SeguroService service = new SeguroService(database);
                MainFrame frame = new MainFrame(service);
                frame.setVisible(true);
                service.startMaintenance(frame::reloadData);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "No fue posible iniciar el sistema:\n" + ex.getMessage(),
                        "Error de inicio",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
