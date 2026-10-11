import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DoctorFrame extends JFrame {
    private final DefaultTableModel model = Ui.model("ID", "Name", "Specialty", "Fee", "Contact", "Availability");
    private final JTable table = Ui.table(model);

    private final JTextField txtId = new JTextField(14);
    private final JTextField txtName = new JTextField(14);
    private final JComboBox<String> cbSpecialty = new JComboBox<>(Doctor.SPECIALTIES);
    private final JTextField txtFee = new JTextField(14);
    private final JTextField txtContact = new JTextField(14);
    private final JComboBox<String> cbAvail = new JComboBox<>(new String[]{"Available", "On Leave"});

    private final JTextField txtSearch = new JTextField(14);
    private final JComboBox<String> cbFilterAvail = new JComboBox<>(new String[]{"All", "Available", "On Leave"});
    private final JComboBox<String> cbFilterSpec = new JComboBox<>();
    private final JLabel lblCount = new JLabel();

    public DoctorFrame() {
        super("Manage Doctors");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1050, 560);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(Ui.header("Doctor Records", "Manage doctors, specialties, consultation fees and availability"), BorderLayout.NORTH);

        txtId.setEditable(false);
        cbFilterSpec.addItem("All Specialties");
        for (String s : Doctor.SPECIALTIES) cbFilterSpec.addItem(s);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Doctor Details"));
        Ui.addRow(form, 0, "Doctor ID:", txtId);
        Ui.addRow(form, 1, "Name:", txtName);
        Ui.addRow(form, 2, "Specialty:", cbSpecialty);
        Ui.addRow(form, 3, "Consultation Fee:", txtFee);
        Ui.addRow(form, 4, "Contact (09XXXXXXXXX):", txtContact);
        Ui.addRow(form, 5, "Availability:", cbAvail);

        JButton btnAdd = Ui.button("Add", Ui.PRIMARY);
        JButton btnUpdate = Ui.button("Update", Ui.BLUE);
        JButton btnDelete = Ui.button("Delete", Ui.DANGER);
        JButton btnClear = Ui.button("Clear", Ui.GRAY);
        JPanel btns = new JPanel(new GridLayout(2, 2, 6, 6));
        btns.add(btnAdd); btns.add(btnUpdate); btns.add(btnDelete); btns.add(btnClear);
        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.add(btns, BorderLayout.NORTH);

        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
        left.setPreferredSize(new Dimension(340, 0));
        left.add(form, BorderLayout.NORTH);
        left.add(btnWrap, BorderLayout.CENTER);
        add(left, BorderLayout.WEST);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.add(new JLabel("Search (ID / name / specialty):"));
        top.add(txtSearch);
        top.add(new JLabel("Availability:"));
        top.add(cbFilterAvail);
        top.add(new JLabel("Specialty:"));
        top.add(cbFilterSpec);
        JButton btnReset = Ui.button("Reset", Ui.GRAY);
        top.add(btnReset);

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        center.add(top, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(lblCount, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> addDoctor());
        btnUpdate.addActionListener(e -> updateDoctor());
        btnDelete.addActionListener(e -> deleteDoctor());
        btnClear.addActionListener(e -> clearForm());
        btnReset.addActionListener(e -> {
            txtSearch.setText(""); cbFilterAvail.setSelectedIndex(0); cbFilterSpec.setSelectedIndex(0);
        });
        Ui.onChange(txtSearch, this::refresh);
        cbFilterAvail.addActionListener(e -> refresh());
        cbFilterSpec.addActionListener(e -> refresh());
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) fillForm(); });

        refresh();
    }

    private void refresh() {
        model.setRowCount(0);
        String q = txtSearch.getText().trim().toLowerCase();
        String av = (String) cbFilterAvail.getSelectedItem();
        String sp = (String) cbFilterSpec.getSelectedItem();
        for (Doctor d : DataStore.doctors) {
            if (!q.isEmpty() && !(d.getId().toLowerCase().contains(q)
                    || d.getName().toLowerCase().contains(q) || d.getSpecialty().toLowerCase().contains(q))) continue;
            if (!av.equals("All") && !d.getAvailability().equals(av)) continue;
            if (!sp.equals("All Specialties") && !d.getSpecialty().equals(sp)) continue;
            model.addRow(new Object[]{d.getId(), d.getName(), d.getSpecialty(),
                    String.format("%.2f", d.getFee()), d.getContact(), d.getAvailability()});
        }
        lblCount.setText("Showing " + model.getRowCount() + " of " + DataStore.doctors.size() + " doctors");
    }

    private void fillForm() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        txtId.setText(model.getValueAt(r, 0).toString());
        txtName.setText(model.getValueAt(r, 1).toString());
        cbSpecialty.setSelectedItem(model.getValueAt(r, 2));
        txtFee.setText(model.getValueAt(r, 3).toString());
        txtContact.setText(model.getValueAt(r, 4).toString());
        cbAvail.setSelectedItem(model.getValueAt(r, 5));
    }

    private void clearForm() {
        txtId.setText(""); txtName.setText(""); txtFee.setText(""); txtContact.setText("");
        cbSpecialty.setSelectedIndex(0); cbAvail.setSelectedIndex(0);
        table.clearSelection();
    }

    private Doctor readForm(String id) {
        String name = DataStore.clean(txtName.getText());
        String feeText = txtFee.getText().trim();
        String contact = txtContact.getText().trim();
        if (name.isEmpty() || feeText.isEmpty() || contact.isEmpty()) {
            Ui.error(this, "All fields are required."); return null;
        }
        if (!name.matches("[A-Za-z .'-]+")) {
            Ui.error(this, "Name may only contain letters, spaces, periods, apostrophes and hyphens."); return null;
        }
        double fee;
        try { fee = Double.parseDouble(feeText); }
        catch (NumberFormatException ex) { Ui.error(this, "Fee must be a valid number."); return null; }
        if (fee <= 0) { Ui.error(this, "Fee must be greater than zero (no negative values)."); return null; }
        if (fee > 50000) { Ui.error(this, "Fee seems too high (max 50,000)."); return null; }
        if (!contact.matches("09\\d{9}")) {
            Ui.error(this, "Contact number must be 11 digits and start with 09."); return null;
        }
        for (Doctor d : DataStore.doctors) {
            if (!d.getId().equals(id) && d.getName().equalsIgnoreCase(name)) {
                Ui.error(this, "Duplicate record: a doctor with this name already exists."); return null;
            }
        }
        return new Doctor(id, name, (String) cbSpecialty.getSelectedItem(), fee, contact, (String) cbAvail.getSelectedItem());
    }

    private void addDoctor() {
        Doctor d = readForm(DataStore.nextDoctorId());
        if (d == null) return;
        DataStore.doctors.add(d);
        DataStore.save();
        refresh();
        clearForm();
        Ui.info(this, "Doctor " + d.getId() + " added successfully.");
    }

    private void updateDoctor() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) { Ui.error(this, "Select a doctor from the table first."); return; }
        Doctor edited = readForm(id);
        if (edited == null) return;
        Doctor d = DataStore.findDoctor(id);
        if (d == null) { Ui.error(this, "Doctor no longer exists."); return; }
        if (edited.getAvailability().equals("On Leave") && DataStore.doctorHasScheduled(id)) {
            Ui.error(this, "Cannot set to On Leave: this doctor still has scheduled appointments.");
            return;
        }
        d.setName(edited.getName());
        d.setSpecialty(edited.getSpecialty());
        d.setFee(edited.getFee());
        d.setContact(edited.getContact());
        d.setAvailability(edited.getAvailability());
        DataStore.save();
        refresh();
        clearForm();
        Ui.info(this, "Doctor " + id + " updated successfully.");
    }

    private void deleteDoctor() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) { Ui.error(this, "Select a doctor from the table first."); return; }
        if (DataStore.doctorHasScheduled(id)) {
            Ui.error(this, "Cannot delete: this doctor still has scheduled appointments.");
            return;
        }
        if (!Ui.confirm(this, "Delete doctor " + id + "? This cannot be undone.")) return;
        DataStore.doctors.remove(DataStore.findDoctor(id));
        DataStore.save();
        refresh();
        clearForm();
    }
}
