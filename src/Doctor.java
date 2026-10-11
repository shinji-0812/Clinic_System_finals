public class Doctor extends Person {
    public static final String[] SPECIALTIES = {
        "General Medicine", "Pediatrics", "Cardiology", "Dermatology", "OB-Gynecology", "Orthopedics"
    };

    private String specialty;
    private double fee;
    private String availability; // "Available" or "On Leave"

    public Doctor(String id, String name, String specialty, double fee, String contact, String availability) {
        super(id, name, contact);
        this.specialty = specialty;
        this.fee = fee;
        this.availability = availability;
    }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    @Override public String getRole() { return "Doctor"; }
    @Override public String getSummary() { return specialty + ", fee PHP " + String.format("%,.2f", fee) + " (" + availability + ")"; }

    @Override
    public String toCsv() {
        return String.join(",", getId(), getName(), specialty, String.valueOf(fee), getContact(), availability);
    }

    public static Doctor fromCsv(String line) {
        String[] f = line.split(",", -1);
        return new Doctor(f[0], f[1], f[2], Double.parseDouble(f[3]), f[4], f[5]);
    }

    // Overrides Person.toString so combo boxes show the specialty too
    @Override
    public String toString() { return getId() + " - " + getName() + " (" + specialty + ")"; }
}
