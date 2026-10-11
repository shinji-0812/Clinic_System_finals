import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ReportFrame extends JFrame {
    private final JTextArea txtSummary = new JTextArea();
    private final JTextArea txtDirectory = new JTextArea();
    private final DefaultTableModel model = Ui.model("Doctor", "Specialty", "Total", "Scheduled",
            "Completed", "Cancelled", "Revenue (Completed)");

    public ReportFrame() {
        super("Reports / Summary");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(Ui.header("Reports / Summary", "Generated from the clinic's actual records"), BorderLayout.NORTH);

        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 13);
        txtSummary.setFont(mono); txtSummary.setEditable(false);
        txtDirectory.setFont(mono); txtDirectory.setEditable(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clinic Summary", new JScrollPane(txtSummary));
        tabs.addTab("By Doctor", new JScrollPane(Ui.table(model)));
        tabs.addTab("People Directory", new JScrollPane(txtDirectory));
        add(tabs, BorderLayout.CENTER);

        JButton btnRefresh = Ui.button("Refresh", Ui.BLUE);
        JButton btnExport = Ui.button("Export to clinic_report.txt", Ui.PRIMARY);
        JButton btnClose = Ui.button("Close", Ui.GRAY);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        south.add(btnRefresh); south.add(btnExport); south.add(btnClose);
        add(south, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> load());
        btnClose.addActionListener(e -> dispose());
        btnExport.addActionListener(e -> {
            try {
                Files.write(Paths.get("clinic_report.txt"), buildSummary().getBytes("UTF-8"));
                Ui.info(this, "Report saved to clinic_report.txt");
            } catch (Exception ex) {
                Ui.error(this, "Could not export report: " + ex.getMessage());
            }
        });
        load();
    }

    private void load() {
        txtSummary.setText(buildSummary());
        txtSummary.setCaretPosition(0);

        model.setRowCount(0);
        for (Doctor d : DataStore.doctors) {
            int sc = 0, co = 0, ca = 0; double rev = 0;
            for (Appointment a : DataStore.appointments) {
                if (!a.getDoctorId().equals(d.getId())) continue;
                if (a.getStatus().equals("Scheduled")) sc++;
                else if (a.getStatus().equals("Completed")) { co++; rev += a.getTotal(); }
                else ca++;
            }
            model.addRow(new Object[]{d.getName(), d.getSpecialty(), sc + co + ca, sc, co, ca, Ui.money(rev)});
        }

        // Polymorphism: Patient and Doctor are both handled as Person
        List<Person> people = new ArrayList<>();
        people.addAll(DataStore.doctors);
        people.addAll(DataStore.patients);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-9s %-6s %-24s %s%n", "ROLE", "ID", "NAME", "DETAILS"));
        sb.append("--------------------------------------------------------------------------\n");
        for (Person p : people)
            sb.append(String.format("%-9s %-6s %-24s %s%n", p.getRole(), p.getId(), p.getName(), p.getSummary()));
        txtDirectory.setText(sb.toString());
        txtDirectory.setCaretPosition(0);
    }

    private String buildSummary() {
        int sc = 0, co = 0, ca = 0, seniors = 0, available = 0;
        double revenue = 0, pending = 0, discounts = 0;
        for (Appointment a : DataStore.appointments) {
            if (a.getStatus().equals("Scheduled")) { sc++; pending += a.getTotal(); }
            else if (a.getStatus().equals("Completed")) { co++; revenue += a.getTotal(); discounts += a.getDiscount(); }
            else ca++;
        }
        for (Patient p : DataStore.patients) if (p.getAge() >= 60) seniors++;
        for (Doctor d : DataStore.doctors) if (d.getAvailability().equals("Available")) available++;
        int total = DataStore.appointments.size();
        double avg = co == 0 ? 0 : revenue / co;

        StringBuilder sb = new StringBuilder();
        sb.append("=========== CLINIC SUMMARY REPORT ===========\n");
        sb.append("Generated: ").append(java.time.LocalDateTime.now().withNano(0)).append("\n\n");
        sb.append(String.format("%-34s %d%n", "Total patients:", DataStore.patients.size()));
        sb.append(String.format("%-34s %d%n", "  Senior patients (60+):", seniors));
        sb.append(String.format("%-34s %d%n", "Total doctors:", DataStore.doctors.size()));
        sb.append(String.format("%-34s %d%n%n", "  Currently available:", available));
        sb.append(String.format("%-34s %d%n", "Total appointments:", total));
        sb.append(String.format("%-34s %d%n", "  Scheduled:", sc));
        sb.append(String.format("%-34s %d%n", "  Completed:", co));
        sb.append(String.format("%-34s %d%n%n", "  Cancelled:", ca));
        sb.append(String.format("%-34s %s%n", "Revenue (completed):", Ui.money(revenue)));
        sb.append(String.format("%-34s %s%n", "Expected revenue (scheduled):", Ui.money(pending)));
        sb.append(String.format("%-34s %s%n", "Senior discounts given:", Ui.money(discounts)));
        sb.append(String.format("%-34s %s%n", "Average bill per completed visit:", Ui.money(avg)));
        sb.append(String.format("%-34s %.1f%%%n", "Cancellation rate:", total == 0 ? 0.0 : ca * 100.0 / total));
        return sb.toString();
    }
}
