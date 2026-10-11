public class Appointment {
    public static final double SENIOR_DISCOUNT_RATE = 0.20; // 60+ years old

    private String id;
    private String patientId;
    private String doctorId;
    private String date;   // yyyy-MM-dd
    private String time;   // HH:mm
    private String reason;
    private String status; // Scheduled, Completed, Cancelled
    private double fee;
    private double discount;

    public Appointment(String id, String patientId, String doctorId, String date, String time,
                       String reason, String status, double fee, double discount) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.reason = reason;
        this.status = status;
        this.fee = fee;
        this.discount = discount;
    }

    /** Automatic calculation: senior citizens get 20% off the consultation fee. */
    public static double computeDiscount(Patient p, Doctor d) {
        return p.getAge() >= 60 ? d.getFee() * SENIOR_DISCOUNT_RATE : 0.0;
    }

    public double getTotal() { return fee - discount; }

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getFee() { return fee; }
    public double getDiscount() { return discount; }

    public String toCsv() {
        return String.join(",", id, patientId, doctorId, date, time, reason, status,
                String.valueOf(fee), String.valueOf(discount));
    }

    public static Appointment fromCsv(String line) {
        String[] f = line.split(",", -1);
        return new Appointment(f[0], f[1], f[2], f[3], f[4], f[5], f[6],
                Double.parseDouble(f[7]), Double.parseDouble(f[8]));
    }
}
