import javax.swing.JOptionPane;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Data storage layer: ArrayLists in memory, saved to CSV files in the "data" folder. */
public class DataStore {
    public static final List<Patient> patients = new ArrayList<>();
    public static final List<Doctor> doctors = new ArrayList<>();
    public static final List<Appointment> appointments = new ArrayList<>();

    private static final Path DIR = Paths.get("data");
    private static final Path PATIENT_FILE = DIR.resolve("patients.csv");
    private static final Path DOCTOR_FILE = DIR.resolve("doctors.csv");
    private static final Path APPT_FILE = DIR.resolve("appointments.csv");

    // ---------- Login ----------
    /** Returns the role if credentials are valid, otherwise null. */
    public static String authenticate(String user, String pass) {
        if (user.equals("admin") && pass.equals("admin123")) return "Administrator";
        if (user.equals("reception") && pass.equals("clinic123")) return "Receptionist";
        return null;
    }

    // ---------- Helpers ----------
    public static String clean(String s) {
        return s == null ? "" : s.trim().replace(",", " ").replace("\n", " ").replace("\r", " ");
    }

    private static String nextId(String prefix, List<String> ids) {
        int max = 0;
        for (String id : ids) {
            try { max = Math.max(max, Integer.parseInt(id.substring(prefix.length()))); }
            catch (Exception ignored) { }
        }
        return prefix + String.format("%03d", max + 1);
    }

    public static String nextPatientId() {
        return nextId("P", patients.stream().map(Patient::getId).collect(Collectors.toList()));
    }
    public static String nextDoctorId() {
        return nextId("D", doctors.stream().map(Doctor::getId).collect(Collectors.toList()));
    }
    public static String nextAppointmentId() {
        return nextId("A", appointments.stream().map(Appointment::getId).collect(Collectors.toList()));
    }

    public static Patient findPatient(String id) {
        for (Patient p : patients) if (p.getId().equals(id)) return p;
        return null;
    }
    public static Doctor findDoctor(String id) {
        for (Doctor d : doctors) if (d.getId().equals(id)) return d;
        return null;
    }
    public static Appointment findAppointment(String id) {
        for (Appointment a : appointments) if (a.getId().equals(id)) return a;
        return null;
    }

    public static boolean patientHasScheduled(String patientId) {
        for (Appointment a : appointments)
            if (a.getPatientId().equals(patientId) && a.getStatus().equals("Scheduled")) return true;
        return false;
    }
    public static boolean doctorHasScheduled(String doctorId) {
        for (Appointment a : appointments)
            if (a.getDoctorId().equals(doctorId) && a.getStatus().equals("Scheduled")) return true;
        return false;
    }

    // ---------- Load / Save ----------
    public static void load() {
        patients.clear(); doctors.clear(); appointments.clear();
        try {
            if (!Files.exists(PATIENT_FILE) || !Files.exists(DOCTOR_FILE) || !Files.exists(APPT_FILE)) {
                seed();
                save();
                return;
            }
            for (String line : Files.readAllLines(PATIENT_FILE)) if (!line.trim().isEmpty()) patients.add(Patient.fromCsv(line));
            for (String line : Files.readAllLines(DOCTOR_FILE)) if (!line.trim().isEmpty()) doctors.add(Doctor.fromCsv(line));
            for (String line : Files.readAllLines(APPT_FILE)) if (!line.trim().isEmpty()) appointments.add(Appointment.fromCsv(line));
        } catch (Exception e) {
            // corrupted file -> rebuild with sample data
            patients.clear(); doctors.clear(); appointments.clear();
            seed();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(DIR);
            Files.write(PATIENT_FILE, patients.stream().map(Patient::toCsv).collect(Collectors.toList()));
            Files.write(DOCTOR_FILE, doctors.stream().map(Doctor::toCsv).collect(Collectors.toList()));
            Files.write(APPT_FILE, appointments.stream().map(Appointment::toCsv).collect(Collectors.toList()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not save data: " + e.getMessage(),
                    "Storage Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Sample data (20 patients, 6 doctors, 22 appointments) ----------
    private static void seed() {
        String[][] pat = {
            {"Juan Dela Cruz", "35", "Male"}, {"Maria Clara Santos", "28", "Female"},
            {"Pedro Penduko", "62", "Male"}, {"Ana Liza Reyes", "45", "Female"},
            {"Jose Mercado", "19", "Male"}, {"Carmen Villanueva", "67", "Female"},
            {"Miguel Torres", "8", "Male"}, {"Sofia Lopez", "5", "Female"},
            {"Ramon Bautista", "54", "Male"}, {"Teresa Navarro", "31", "Female"},
            {"Antonio Cruz", "71", "Male"}, {"Rosario Aquino", "59", "Female"},
            {"Daniel Castillo", "12", "Male"}, {"Isabel Garcia", "24", "Female"},
            {"Fernando Ocampo", "40", "Male"}, {"Lorna Domingo", "63", "Female"},
            {"Mark Anthony Flores", "17", "Male"}, {"Jenny Rose Pascual", "33", "Female"},
            {"Roberto Salazar", "48", "Male"}, {"Patricia Gomez", "3", "Female"}
        };
        String[] addr = {"Brgy. Poblacion", "Brgy. San Isidro", "Brgy. Mabini", "Brgy. Rizal", "Brgy. Del Pilar"};
        for (int i = 0; i < pat.length; i++) {
            patients.add(new Patient(String.format("P%03d", i + 1), pat[i][0], Integer.parseInt(pat[i][1]),
                    pat[i][2], String.format("0917%07d", 1000000 + (i + 1) * 111111), addr[i % addr.length]));
        }

        doctors.add(new Doctor("D001", "Dr. Maria Santos", "General Medicine", 500, "09171234501", "Available"));
        doctors.add(new Doctor("D002", "Dr. Jose Ramirez", "Pediatrics", 600, "09171234502", "Available"));
        doctors.add(new Doctor("D003", "Dr. Ana Reyes", "Cardiology", 1200, "09171234503", "Available"));
        doctors.add(new Doctor("D004", "Dr. Carlo Mendoza", "Dermatology", 800, "09171234504", "Available"));
        doctors.add(new Doctor("D005", "Dr. Liza Fernandez", "OB-Gynecology", 900, "09171234505", "Available"));
        doctors.add(new Doctor("D006", "Dr. Paolo Garcia", "Orthopedics", 1000, "09171234506", "On Leave"));

        // {patient#, doctor#, day offset from today, time, reason, status}
        Object[][] ap = {
            {1, 1, -10, "09:00", "Fever and cough", "Completed"},
            {2, 5, -9, "10:00", "Prenatal checkup", "Completed"},
            {3, 3, -8, "08:30", "High blood pressure", "Completed"},
            {7, 2, -8, "09:30", "Child vaccination", "Completed"},
            {4, 4, -7, "13:00", "Skin rash", "Completed"},
            {6, 3, -7, "14:00", "Chest pain follow-up", "Completed"},
            {9, 6, -6, "15:00", "Knee pain", "Completed"},
            {8, 2, -6, "10:30", "Cough and colds", "Cancelled"},
            {10, 1, -5, "11:00", "General checkup", "Completed"},
            {11, 3, -5, "08:00", "Heart monitoring", "Completed"},
            {12, 1, -4, "09:00", "Diabetes consultation", "Completed"},
            {13, 2, -3, "14:30", "Asthma checkup", "Completed"},
            {14, 5, -3, "10:00", "Irregular menstruation", "Cancelled"},
            {15, 4, -2, "15:30", "Acne treatment", "Completed"},
            {16, 3, -2, "09:00", "Blood pressure check", "Completed"},
            {17, 1, -1, "13:30", "Sore throat", "Completed"},
            {18, 5, 0, "16:00", "Pregnancy consultation", "Scheduled"},
            {19, 1, 1, "09:00", "Back pain", "Scheduled"},
            {20, 2, 1, "10:00", "Fever", "Scheduled"},
            {3, 3, 2, "08:30", "Cardiac follow-up", "Scheduled"},
            {5, 4, 3, "11:00", "Allergy consultation", "Scheduled"},
            {6, 1, 4, "14:00", "Annual checkup", "Scheduled"}
        };
        for (int i = 0; i < ap.length; i++) {
            Patient p = patients.get((Integer) ap[i][0] - 1);
            Doctor d = doctors.get((Integer) ap[i][1] - 1);
            String date = LocalDate.now().plusDays((Integer) ap[i][2]).toString();
            appointments.add(new Appointment(String.format("A%03d", i + 1), p.getId(), d.getId(), date,
                    (String) ap[i][3], (String) ap[i][4], (String) ap[i][5],
                    d.getFee(), Appointment.computeDiscount(p, d)));
        }
    }
}
