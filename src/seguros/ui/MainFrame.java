package seguros.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import seguros.model.Cliente;
import seguros.model.Poliza;
import seguros.model.Recibo;
import seguros.service.SeguroService;

/**
 * Interfaz grafica principal construida con Swing y JFrame.
 */
public class MainFrame extends JFrame {
    private static final Color NAVY = new Color(20, 38, 63);
    private static final Color BLUE = new Color(36, 106, 173);
    private static final Color BACKGROUND = new Color(242, 246, 250);

    private final SeguroService service;
    private final DefaultTableModel clientesModel = tableModel("ID", "Nombre", "Telefono", "Correo");
    private final DefaultTableModel polizasModel = tableModel(
            "ID", "Numero", "Tipo", "Cliente", "Aseguradora", "Vencimiento", "Prima", "Estado", "Detalle");
    private final DefaultTableModel recibosModel = tableModel(
            "ID", "Poliza", "Cliente", "Vencimiento", "Monto", "Estado");
    private final JLabel clientesCount = metricLabel();
    private final JLabel activasCount = metricLabel();
    private final JLabel vencidasCount = metricLabel();
    private final JLabel primasVencidasCount = metricLabel();
    private final JTextArea alertArea = new JTextArea();
    private final JLabel status = new JLabel("Listo");

    public MainFrame(SeguroService service) {
        super("Seguros Mini - Gestion de polizas");
        this.service = service;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                service.close();
                dispose();
            }
        });
        setContentPane(buildContent());
        reloadData();
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(), BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        root.add(status, BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("SEGUROS MINI");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(title);
        header.add(titles, BorderLayout.WEST);
        return header;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Resumen", buildDashboard());
        tabs.addTab("Clientes", buildClientesPanel());
        tabs.addTab("Polizas", buildPolizasPanel());
        tabs.addTab("Primas", buildRecibosPanel());
        return tabs;
    }

    private JPanel buildDashboard() {
        JPanel panel = page();
        JPanel metrics = new JPanel(new GridLayout(1, 4, 12, 12));
        metrics.setOpaque(false);
        metrics.add(metricCard("Clientes", clientesCount));
        metrics.add(metricCard("Polizas activas", activasCount));
        metrics.add(metricCard("Polizas vencidas", vencidasCount));
        metrics.add(metricCard("Primas vencidas", primasVencidasCount));

        alertArea.setEditable(false);
        alertArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        alertArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane alerts = new JScrollPane(alertArea);
        alerts.setBorder(BorderFactory.createTitledBorder("Alertas de primas"));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        JButton backup = button("Crear respaldo BIN", event -> createBackup());
        JButton report = button("Generar reporte TXT", event -> createReport());
        JButton refresh = button("Actualizar", event -> reloadData());
        actions.add(backup);
        actions.add(report);
        actions.add(refresh);

        panel.add(metrics, BorderLayout.NORTH);
        panel.add(alerts, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildClientesPanel() {
        JPanel panel = page();
        JTable table = new JTable(clientesModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel actions = actionPanel();
        actions.add(button("Nuevo cliente", event -> showClienteDialog()));
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildPolizasPanel() {
        JPanel panel = page();
        JTable table = new JTable(polizasModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel actions = actionPanel();
        actions.add(button("Nueva poliza", event -> showPolizaDialog()));
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildRecibosPanel() {
        JPanel panel = page();
        JTable table = new JTable(recibosModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel actions = actionPanel();
        actions.add(button("Marcar como pagada", event -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showError("Selecciona una prima.");
                return;
            }
            try {
                service.payReceipt((Integer) recibosModel.getValueAt(row, 0));
                setStatus("Prima marcada como pagada.");
                reloadData();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }));
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    public final void reloadData() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::reloadData);
            return;
        }
        clientesModel.setRowCount(0);
        for (Cliente cliente : service.getClientes()) {
            clientesModel.addRow(new Object[]{
                cliente.getId(), cliente.getNombre(), cliente.getTelefono(), cliente.getEmail()
            });
        }

        polizasModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (Poliza poliza : service.getPolizas()) {
            Cliente cliente = service.findCliente(poliza.getClienteId());
            polizasModel.addRow(new Object[]{
                poliza.getId(), poliza.getNumero(), poliza.getTipo(), nameOf(cliente),
                poliza.getAseguradora(), poliza.getFechaVencimiento(),
                money(poliza.getPrima()), poliza.getEstado(today), poliza.getDetalle()
            });
        }

        recibosModel.setRowCount(0);
        StringBuilder alerts = new StringBuilder();
        for (Recibo recibo : service.getRecibos()) {
            Poliza poliza = service.findPoliza(recibo.getPolizaId());
            Cliente cliente = poliza == null ? null : service.findCliente(poliza.getClienteId());
            String state = recibo.getEstado(today);
            recibosModel.addRow(new Object[]{
                recibo.getId(), poliza == null ? "?" : poliza.getNumero(), nameOf(cliente),
                recibo.getFechaVencimiento(), money(recibo.getMonto()), state
            });
            if ("VENCIDO".equals(state) || ("PENDIENTE".equals(state)
                    && !recibo.getFechaVencimiento().isAfter(today.plusDays(30)))) {
                alerts.append(state).append(" | ")
                        .append(poliza == null ? "?" : poliza.getNumero()).append(" | ")
                        .append(nameOf(cliente)).append(" | vence ")
                        .append(recibo.getFechaVencimiento()).append(" | ")
                        .append(money(recibo.getMonto())).append('\n');
            }
        }
        alertArea.setText(alerts.length() == 0 ? "Sin alertas por el momento." : alerts.toString());
        clientesCount.setText(String.valueOf(service.getClientes().size()));
        activasCount.setText(String.valueOf(service.countPolizas("ACTIVA")));
        vencidasCount.setText(String.valueOf(service.countPolizas("VENCIDA")));
        primasVencidasCount.setText(String.valueOf(service.countRecibos("VENCIDO")));
    }

    private void showClienteDialog() {
        JTextField nombre = new JTextField();
        JTextField telefono = new JTextField();
        JTextField email = new JTextField();
        Object[] fields = {
            "Nombre completo:", nombre,
            "Telefono:", telefono,
            "Correo:", email
        };
        if (JOptionPane.showConfirmDialog(this, fields, "Nuevo cliente",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            service.addCliente(nombre.getText(), telefono.getText(), email.getText());
            setStatus("Cliente registrado.");
            reloadData();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void showPolizaDialog() {
        ArrayList<Cliente> clients = service.getClientes();
        if (clients.isEmpty()) {
            showError("Registra al menos un cliente antes de crear una poliza.");
            return;
        }
        JComboBox<Cliente> cliente = new JComboBox<>(clients.toArray(new Cliente[0]));
        JComboBox<String> tipo = new JComboBox<>(new String[]{"AUTO", "GASTOS_MEDICOS", "VIDA"});
        JTextField numero = new JTextField("POL-" + (service.getPolizas().size() + 1));
        JTextField aseguradora = new JTextField();
        JTextField inicio = new JTextField(LocalDate.now().toString());
        JTextField vencimiento = new JTextField(LocalDate.now().plusYears(1).toString());
        JTextField prima = new JTextField();
        JTextField detalle = new JTextField();
        Object[] fields = {
            "Cliente:", cliente,
            "Tipo:", tipo,
            "Numero:", numero,
            "Aseguradora:", aseguradora,
            "Inicio (AAAA-MM-DD):", inicio,
            "Vencimiento (AAAA-MM-DD):", vencimiento,
            "Prima:", prima,
            "Detalle (vehiculo, asegurado o beneficiario):", detalle
        };
        if (JOptionPane.showConfirmDialog(this, fields, "Nueva poliza",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            service.addPoliza((String) tipo.getSelectedItem(), (Cliente) cliente.getSelectedItem(),
                    numero.getText(), aseguradora.getText(), LocalDate.parse(inicio.getText()),
                    LocalDate.parse(vencimiento.getText()), Double.parseDouble(prima.getText()),
                    detalle.getText());
            setStatus("Poliza y prima registradas.");
            reloadData();
        } catch (DateTimeParseException ex) {
            showError("Usa fechas con formato AAAA-MM-DD.");
        } catch (NumberFormatException ex) {
            showError("La prima debe ser numerica.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void createBackup() {
        try {
            Path output = service.createBackup();
            setStatus("Respaldo creado: " + output);
            JOptionPane.showMessageDialog(this, "Respaldo binario creado:\n" + output);
        } catch (IOException ex) {
            showError(ex.getMessage());
        }
    }

    private void createReport() {
        try {
            Path output = service.createReport();
            setStatus("Reporte creado: " + output);
            JOptionPane.showMessageDialog(this, "Reporte de texto creado:\n" + output);
        } catch (IOException ex) {
            showError(ex.getMessage());
        }
    }

    private JPanel page() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return panel;
    }

    private JPanel actionPanel() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        return actions;
    }

    private JButton button(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(BLUE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.addActionListener(listener);
        return button;
    }

    private JPanel metricCard(String title, JLabel value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 225, 235)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(85, 100, 115));
        card.add(value, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        return card;
    }

    private static JLabel metricLabel() {
        JLabel label = new JLabel("0", SwingConstants.LEFT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 28f));
        label.setForeground(NAVY);
        return label;
    }

    private static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private String nameOf(Cliente cliente) {
        return cliente == null ? "Cliente no encontrado" : cliente.getNombre();
    }

    private String money(double amount) {
        return String.format("$%,.2f", amount);
    }

    private void setStatus(String message) {
        status.setText(message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Revisa los datos", JOptionPane.ERROR_MESSAGE);
    }
}
