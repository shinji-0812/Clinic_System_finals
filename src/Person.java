/** Abstract base class (Inheritance + Polymorphism): Patient and Doctor extend it. */
public abstract class Person {
    private String id;
    private String name;
    private String contact;

    public Person(String id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    // Polymorphic methods - each subclass provides its own version
    public abstract String getRole();
    public abstract String getSummary();
    public abstract String toCsv();

    @Override
    public String toString() { return id + " - " + name; }
}
