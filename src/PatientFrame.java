import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PatientFrame extends JFrame {
    private final DefaultTableModel model = Ui.model("ID", "Name", "Age", "Gender", "Contact", "Address");
    private final JTable table = Ui.table(model);

    private final JTextField txtId = new JTextField(14);
    private final JTextField txtName = new JTextField(14);
    private final JTextField txtAge = new JTextField(14);
    private final JComboBox<String> cbGender = new JComboBox<>(new String[]{"Male", "Female"});
    private final JTextField txtContact = new JTextField(14);
    private final JTextField txtAddress = new JTextField(14);

    private final JTextField txtSearch = new JTextField(14);
    private final JComboBox<String> cbFilterGender = new JComboBox<>(new String[]{"All", "Male", "Female"});
    private final JComboBox<String> cbFilterAge = new JComboBox<>(
            new String[]{"All Ages", "Child (0-12)", "Teen (13-19)", "Adult (20-59)", "Senior (60+)"});
    private final JLabel lblCount = new JLabel();

    public PatientFrame() {
        super("Manage Patients");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1050, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(Ui.header("Patient Records", "Create, view, update and delete patient information"), BorderLayout.NORTH);

        txtId.setEditable(false);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Patient Details"));
        Ui.addRow(form, 0, "Patient ID:", txtId);
        Ui.addRow(form, 1, "Full Name:", txtName);
        Ui.addRow(form, 2, "Age:", txtAge);
        Ui.addRow(form, 3, "Gender:", cbGender);
        Ui.addRow(form, 4, "Contact (09XXXXXXXXX):", txtContact);
        Ui.addRow(form, 5, "Address:", txtAddress);

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
        top.add(new JLabel("Search (ID / name / contact):"));
        top.add(txtSearch);
        top.add(new JLabel("Gender:"));
        top.add(cbFilterGender);
        top.add(new JLabel("Age group:"));
        top.add(cbFilterAge);
        JButton btnReset = Ui.button("Reset", Ui.GRAY);
        top.add(btnReset);

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        center.add(top, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(lblCount, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> addPatient());
        btnUpdate.addActionListener(e -> updatePatient());
        btnDelete.addActionListener(e -> deletePatient());
        btnClear.addActionListener(e -> clearForm());
        btnReset.addActionListener(e -> {
            txtSearch.setText(""); cbFilterGender.setSelectedIndex(0); cbFilterAge.setSelectedIndex(0);
        });
        Ui.onChange(txtSearch, this::refresh);
        cbFilterGender.addActionListener(e -> refresh());
        cbFilterAge.addActionListener(e -> refresh());
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) fillForm(); });

        refresh();
    }

    // ---------- READ + SEARCH + FILTER ----------
    private void refresh() {
        model.setRowCount(0);
        String q = txtSearch.getText().trim().toLowerCase();
        String g = (String) cbFilterGender.getSelectedItem();
        int ageIdx = cbFilterAge.getSelectedIndex();
        for (Patient p : DataStore.patients) {
            if (!q.isEmpty() && !(p.getId().toLowerCase().contains(q)
                    || p.getName().toLowerCase().contains(q) || p.getContact().contains(q))) continue;
            if (!g.equals("All") && !p.getGender().equals(g)) continue;
            if (!ageMatches(p.getAge(), ageIdx)) continue;
            model.addRow(new Object[]{p.getId(), p.getName(), p.getAge(), p.getGender(), p.getContact(), p.getAddress()});
        }
        lblCount.setText("Showing " + model.getRowCount() + " of " + DataStore.patients.size() + " patients");
    }

    private boolean ageMatches(int age, int idx) {
        switch (idx) {
            case 1: return age <= 12;
            case 2: return age >= 13 && age <= 19;
            case 3: return age >= 20 && age <= 59;
            case 4: return age >= 60;
            default: return true;
        }
    }

    private void fillForm() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        txtId.setText(model.getValueAt(r, 0).toString());
        txtName.setText(model.getValueAt(r, 1).toString());
        txtAge.setText(model.getValueAt(r, 2).toString());
        cbGender.setSelectedItem(model.getValueAt(r, 3));
        txtContact.setText(model.getValueAt(r, 4).toString());
        txtAddress.setText(model.getValueAt(r, 5).toString());
    }

    private void clearForm() {
        txtId.setText(""); txtName.setText(""); txtAge.setText(""); txtContact.setText(""); txtAddress.setText("");
        cbGender.setSelectedIndex(0);
        table.clearSelection();
    }

    // ---------- VALIDATION ----------
    /** Reads and validates the form. Returns a Patient object, or null if input is invalid. */
    private Patient readForm(String id) {
        String name = DataStore.clean(txtName.getText());
        String ageText = txtAge.getText().trim();
        String contact = txtContact.getText().trim();
        String address = DataStore.clean(txtAddress.getText());

        if (name.isEmpty() || ageText.isEmpty() || contact.isEmpty() || address.isEmpty()) {
            Ui.error(this, "All fields are required."); return null;
        }
        if (!name.matches("[A-Za-z .'-]+")) {
            Ui.error(this, "Name may only contain letters, spaces, periods, apostrophes and hyphens."); return null;
        }
        int age;
        try { age = Integer.parseInt(ageText); }
        catch (NumberFormatException ex) { Ui.error(this, "Age must be a whole number."); return null; }
        if (age < 0) { Ui.error(this, "Age cannot be negative."); return null; }
        if (age > 120) { Ui.error(this, "Age must not be more than 120."); return null; }
        if (!contact.matches("09\\d{9}")) {
            Ui.error(this, "Contact number must be 11 digits and start with 09 (e.g. 09171234567)."); return null;
        }
        for (Patient p : DataStore.patients) {
            if (!p.getId().equals(id) && p.getName().equalsIgnoreCase(name) && p.getContact().equals(contact)) {
                Ui.error(this, "Duplicate record: a patient with the same name and contact already exists."); return null;
            }
        }
        return new Patient(id, name, age, (String) cbGender.getSelectedItem(), contact, address);
    }

    // ---------- CREATE ----------
    private void addPatient() {
        Patient p = readForm(DataStore.nextPatientId());
        if (p == null) return;
        DataStore.patients.add(p);
        DataStore.save();
        refresh();
        clearForm();
        Ui.info(this, "Patient " + p.getId() + " added successfully.");
    }

    // ---------- UPDATE ----------
    private void updatePatient() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) { Ui.error(this, "Select a patient from the table first."); return; }
        Patient edited = readForm(id);
        if (edited == null) return;
        Patient p = DataStore.findPatient(id);
        if (p == null) { Ui.error(this, "Patient no longer exists."); return; }
        p.setName(edited.getName());
        p.setAge(edited.getAge());
        p.setGender(edited.getGender());
        p.setContact(edited.getContact());
        p.setAddress(edited.getAddress());
        DataStore.save();
        refresh();
        clearForm();
        Ui.info(this, "Patient " + id + " updated successfully.");
    }

    // ---------- DELETE ----------
    private void deletePatient() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) { Ui.error(this, "Select a patient from the table first."); return; }
        if (DataStore.patientHasScheduled(id)) {
            Ui.error(this, "Cannot delete: this patient still has a scheduled appointment. Cancel it first.");
            return;
        }
        if (!Ui.confirm(this, "Delete patient " + id + "? This cannot be undone.")) return;
        DataStore.patients.remove(DataStore.findPatient(id));
        DataStore.save();
        refresh();
        clearForm();
    }
}
