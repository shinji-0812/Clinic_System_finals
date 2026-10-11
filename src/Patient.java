public class Patient extends Person {
    private int age;
    private String gender;
    private String address;

    public Patient(String id, String name, int age, String gender, String contact, String address) {
        super(id, name, contact);
        this.age = age;
        this.gender = gender;
        this.address = address;
    }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override public String getRole() { return "Patient"; }
    @Override public String getSummary() { return age + " y/o " + gender + ", " + address; }

    @Override
    public String toCsv() {
        return String.join(",", getId(), getName(), String.valueOf(age), gender, getContact(), address);
    }

    public static Patient fromCsv(String line) {
        String[] f = line.split(",", -1);
        return new Patient(f[0], f[1], Integer.parseInt(f[2]), f[3], f[4], f[5]);
    }
}
