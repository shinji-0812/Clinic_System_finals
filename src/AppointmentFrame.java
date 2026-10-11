import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/** Transaction module: book, complete and cancel appointments with automatic billing. */
public class AppointmentFrame extends JFrame {
    private final DefaultTableModel model = Ui.model("ID", "Patient", "Doctor", "Date", "Time",
            "Reason", "Status", "Fee", "Discount", "Total");
    private final JTable table = Ui.table(model);

    private final JComboBox<Patient> cbPatient = new JComboBox<>();
    private final JComboBox<Doctor> cbDoctor = new JComboBox<>();
    private final JTextField txtDate = new JTextField(LocalDate.now().toString(), 12);
    private final JComboBox<String> cbTime = new JComboBox<>();
    private final JTextField txtReason = new JTextField(14);
    private final JLabel lblFee = new JLabel(Ui.money(0));
    private final JLabel lblDiscount = new JLabel(Ui.money(0));
    private final JLabel lblTotal = new JLabel(Ui.money(0));

    private final JTextField txtSearch = new JTextField(14);
    private final JComboBox<String> cbStatus = new JComboBox<>(new String[]{"All", "Scheduled", "Completed", "Cancelled"});
    private final JComboBox<String> cbDate = new JComboBox<>(new String[]{"All Dates", "Today", "Upcoming", "Past"});
    private final JLabel lblCount = new JLabel();

    public AppointmentFrame() {
        super("Appointments");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1200, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(Ui.header("Appointment Transactions", "Book, complete or cancel appointments - the bill is computed automatically"),
                BorderLayout.NORTH);

        for (int m = 8 * 60; m <= 16 * 60 + 30; m += 30) cbTime.addItem(String.format("%02d:%02d", m / 60, m % 60));
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 16f));
        lblTotal.setForeground(Ui.PRIMARY);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("New Appointment"));
        Ui.addRow(form, 0, "Patient:", cbPatient);
        Ui.addRow(form, 1, "Doctor (available):", cbDoctor);
        Ui.addRow(form, 2, "Date (yyyy-MM-dd):", txtDate);
        Ui.addRow(form, 3, "Time:", cbTime);
        Ui.addRow(form, 4, "Reason:", txtReason);
        Ui.addRow(form, 5, "Consultation Fee:", lblFee);
        Ui.addRow(form, 6, "Senior Discount (20%):", lblDiscount);
        Ui.addRow(form, 7, "TOTAL BILL:", lblTotal);

        JButton btnBook = Ui.button("Book Appointment", Ui.PRIMARY);
        JButton btnComplete = Ui.button("Mark Completed", Ui.BLUE);
        JButton btnCancel = Ui.button("Cancel Appointment", Ui.DANGER);
        JButton btnClear = Ui.button("Clear Form", Ui.GRAY);
        JPanel btns = new JPanel(new GridLayout(2, 2, 6, 6));
        btns.add(btnBook); btns.add(btnComplete); btns.add(btnCancel); btns.add(btnClear);
        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.add(btns, BorderLayout.NORTH);

        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
        left.setPreferredSize(new Dimension(390, 0));
        left.add(form, BorderLayout.NORTH);
        left.add(btnWrap, BorderLayout.CENTER);
        add(left, BorderLayout.WEST);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.add(new JLabel("Search:"));
        top.add(txtSearch);
        top.add(new JLabel("Status:"));
        top.add(cbStatus);
        top.add(new JLabel("Date:"));
        top.add(cbDate);
        JButton btnReset = Ui.button("Reset", Ui.GRAY);
        top.add(btnReset);

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        center.add(top, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(lblCount, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        loadCombos();
        cbPatient.addActionListener(e -> updateBill());
        cbDoctor.addActionListener(e -> updateBill());
        updateBill();

        btnBook.addActionListener(e -> book());
        btnComplete.addActionListener(e -> changeStatus("Completed"));
        btnCancel.addActionListener(e -> changeStatus("Cancelled"));
        btnClear.addActionListener(e -> {
            txtDate.setText(LocalDate.now().toString()); txtReason.setText("");
            cbTime.setSelectedIndex(0); table.clearSelection();
        });
        btnReset.addActionListener(e -> {
            txtSearch.setText(""); cbStatus.setSelectedIndex(0); cbDate.setSelectedIndex(0);
        });
        Ui.onChange(txtSearch, this::refresh);
        cbStatus.addActionListener(e -> refresh());
        cbDate.addActionListener(e -> refresh());

        // keep combos in sync if patients/doctors were edited in another window
        addWindowListener(new WindowAdapter() {
            @Override public void windowActivated(WindowEvent e) { loadCombos(); refresh(); }
        });
        refresh();
    }

    private void loadCombos() {
        Object selP = cbPatient.getSelectedItem();
        Object selD = cbDoctor.getSelectedItem();
        cbPatient.removeAllItems();
        for (Patient p : DataStore.patients) cbPatient.addItem(p);
        cbDoctor.removeAllItems();
        for (Doctor d : DataStore.doctors) if (d.getAvailability().equals("Available")) cbDoctor.addItem(d);
        if (selP != null) cbPatient.setSelectedItem(selP);
        if (selD != null) cbDoctor.setSelectedItem(selD);
    }

    // ---------- AUTOMATIC CALCULATION ----------
    private void updateBill() {
        Patient p = (Patient) cbPatient.getSelectedItem();
        Doctor d = (Doctor) cbDoctor.getSelectedItem();
        if (p == null || d == null) {
            lblFee.setText(Ui.money(0)); lblDiscount.setText(Ui.money(0)); lblTotal.setText(Ui.money(0));
            return;
        }
        double fee = d.getFee();
        double disc = Appointment.computeDiscount(p, d);
        lblFee.setText(Ui.money(fee));
        lblDiscount.setText(Ui.money(disc));
        lblTotal.setText(Ui.money(fee - disc));
    }

    // ---------- READ + SEARCH + FILTER ----------
    private void refresh() {
        model.setRowCount(0);
        String q = txtSearch.getText().trim().toLowerCase();
        String st = (String) cbStatus.getSelectedItem();
        int di = cbDate.getSelectedIndex();
        LocalDate today = LocalDate.now();
        for (Appointment a : DataStore.appointments) {
            Patient p = DataStore.findPatient(a.getPatientId());
            Doctor d = DataStore.findDoctor(a.getDoctorId());
            String pn = p == null ? "(deleted)" : p.getName();
            String dn = d == null ? "(deleted)" : d.getName();
            if (!q.isEmpty() && !(a.getId().toLowerCase().contains(q) || pn.toLowerCase().contains(q)
                    || dn.toLowerCase().contains(q) || a.getReason().toLowerCase().contains(q))) continue;
            if (!st.equals("All") && !a.getStatus().equals(st)) continue;
            LocalDate ad = LocalDate.parse(a.getDate());
            if (di == 1 && !ad.equals(today)) continue;
            if (di == 2 && ad.isBefore(today)) continue;
            if (di == 3 && !ad.isBefore(today)) continue;
            model.addRow(new Object[]{a.getId(), pn, dn, a.getDate(), a.getTime(), a.getReason(), a.getStatus(),
                    String.format("%,.2f", a.getFee()), String.format("%,.2f", a.getDiscount()),
                    String.format("%,.2f", a.getTotal())});
        }
        lblCount.setText("Showing " + model.getRowCount() + " of " + DataStore.appointments.size() + " appointments");
    }

    // ---------- TRANSACTION: BOOK ----------
    private void book() {
        Patient p = (Patient) cbPatient.getSelectedItem();
        Doctor d = (Doctor) cbDoctor.getSelectedItem();
        if (p == null || d == null) { Ui.error(this, "Select a patient and an available doctor."); return; }

        String ds = txtDate.getText().trim();
        String reason = DataStore.clean(txtReason.getText());
        if (ds.isEmpty() || reason.isEmpty()) { Ui.error(this, "Date and reason are required."); return; }
        if (reason.length() > 60) { Ui.error(this, "Reason must be 60 characters or less."); return; }

        LocalDate date;
        try { date = LocalDate.parse(ds); }
        catch (DateTimeParseException ex) { Ui.error(this, "Date must be a valid date in yyyy-MM-dd format."); return; }
        String time = (String) cbTime.getSelectedItem();
        if (LocalDateTime.of(date, LocalTime.parse(time)).isBefore(LocalDateTime.now())) {
            Ui.error(this, "Cannot book an appointment in the past."); return;
        }
        if (date.isAfter(LocalDate.now().plusMonths(6))) {
            Ui.error(this, "Appointments can only be booked up to 6 months ahead."); return;
        }

        for (Appointment a : DataStore.appointments) {
            if (!a.getStatus().equals("Scheduled") || !a.getDate().equals(date.toString()) || !a.getTime().equals(time)) continue;
            if (a.getDoctorId().equals(d.getId())) {
                Ui.error(this, d.getName() + " is already booked on " + date + " at " + time + "."); return;
            }
            if (a.getPatientId().equals(p.getId())) {
                Ui.error(this, p.getName() + " already has an appointment on " + date + " at " + time + "."); return;
            }
        }

        double disc = Appointment.computeDiscount(p, d);
        Appointment a = new Appointment(DataStore.nextAppointmentId(), p.getId(), d.getId(), date.toString(),
                time, reason, "Scheduled", d.getFee(), disc);
        DataStore.appointments.add(a);
        DataStore.save();
        refresh();
        txtReason.setText("");
        Ui.info(this, "Appointment " + a.getId() + " booked!\n\nPatient: " + p.getName() + "\nDoctor: " + d.getName()
                + "\nSchedule: " + date + " " + time + "\nFee: " + Ui.money(a.getFee())
                + "\nDiscount: " + Ui.money(a.getDiscount()) + "\nTOTAL: " + Ui.money(a.getTotal()));
    }

    // ---------- TRANSACTION: COMPLETE / CANCEL ----------
    private void changeStatus(String newStatus) {
        int r = table.getSelectedRow();
        if (r < 0) { Ui.error(this, "Select an appointment from the table first."); return; }
        String id = model.getValueAt(r, 0).toString();
        Appointment a = DataStore.findAppointment(id);
        if (a == null) { Ui.error(this, "Appointment not found."); return; }
        if (!a.getStatus().equals("Scheduled")) {
            Ui.error(this, "Invalid transaction: only Scheduled appointments can be " + newStatus.toLowerCase() + ".");
            return;
        }
        if (!Ui.confirm(this, "Mark appointment " + id + " as " + newStatus + "?")) return;
        a.setStatus(newStatus);
        DataStore.save();
        refresh();
    }
}
