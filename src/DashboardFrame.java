import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;

public class DashboardFrame extends JFrame {
    private final JLabel lblPatients = card();
    private final JLabel lblDoctors = card();
    private final JLabel lblToday = card();
    private final JLabel lblScheduled = card();

    public DashboardFrame(String username, String role) {
        super("Clinic Management System - Dashboard");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(820, 560);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { exitApp(); }
        });

        // Menu bar
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem miLogout = new JMenuItem("Logout");
        JMenuItem miExit = new JMenuItem("Exit");
        file.add(miLogout);
        file.add(miExit);
        JMenu modules = new JMenu("Modules");
        JMenuItem miPat = new JMenuItem("Patients");
        JMenuItem miDoc = new JMenuItem("Doctors");
        JMenuItem miApp = new JMenuItem("Appointments");
        JMenuItem miRep = new JMenuItem("Reports");
        modules.add(miPat); modules.add(miDoc); modules.add(miApp); modules.add(miRep);
        bar.add(file);
        bar.add(modules);
        setJMenuBar(bar);

        add(Ui.header("Dashboard", "Welcome, " + username + " (" + role + ")"), BorderLayout.NORTH);

        // Summary cards
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        cards.add(wrap("Patients", lblPatients, Ui.BLUE));
        cards.add(wrap("Doctors", lblDoctors, Ui.PRIMARY));
        cards.add(wrap("Today's Appointments", lblToday, Ui.WARN));
        cards.add(wrap("Scheduled", lblScheduled, Ui.GRAY));

        // Module buttons
        JButton btnPat = Ui.button("Manage Patients", Ui.BLUE);
        JButton btnDoc = Ui.button("Manage Doctors", Ui.PRIMARY);
        JButton btnApp = Ui.button("Appointments (Transactions)", Ui.WARN);
        JButton btnRep = Ui.button("Reports / Summary", Ui.GRAY);
        JButton btnLogout = Ui.button("Logout", Ui.GRAY);
        JButton btnExit = Ui.button("Exit", Ui.DANGER);
        for (JButton b : new JButton[]{btnPat, btnDoc, btnApp, btnRep, btnLogout, btnExit})
            b.setFont(b.getFont().deriveFont(Font.BOLD, 16f));

        JPanel grid = new JPanel(new GridLayout(3, 2, 14, 14));
        grid.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        grid.add(btnPat); grid.add(btnDoc); grid.add(btnApp);
        grid.add(btnRep); grid.add(btnLogout); grid.add(btnExit);

        JPanel center = new JPanel(new BorderLayout());
        center.add(cards, BorderLayout.NORTH);
        center.add(grid, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        btnPat.addActionListener(e -> open(new PatientFrame()));
        btnDoc.addActionListener(e -> open(new DoctorFrame()));
        btnApp.addActionListener(e -> open(new AppointmentFrame()));
        btnRep.addActionListener(e -> open(new ReportFrame()));
        miPat.addActionListener(e -> open(new PatientFrame()));
        miDoc.addActionListener(e -> open(new DoctorFrame()));
        miApp.addActionListener(e -> open(new AppointmentFrame()));
        miRep.addActionListener(e -> open(new ReportFrame()));
        btnLogout.addActionListener(e -> logout());
        miLogout.addActionListener(e -> logout());
        btnExit.addActionListener(e -> exitApp());
        miExit.addActionListener(e -> exitApp());

        refreshStats();
    }

    private static JLabel card() {
        JLabel l = new JLabel("0", SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 30f));
        return l;
    }

    private JPanel wrap(String title, JLabel value, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createLineBorder(color, 2));
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setOpaque(true);
        t.setBackground(color);
        t.setForeground(Color.WHITE);
        t.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        value.setForeground(color);
        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(0, 90));
        return p;
    }

    private void open(JFrame f) {
        f.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { refreshStats(); }
        });
        f.setVisible(true);
    }

    public void refreshStats() {
        lblPatients.setText(String.valueOf(DataStore.patients.size()));
        lblDoctors.setText(String.valueOf(DataStore.doctors.size()));
        String today = LocalDate.now().toString();
        int t = 0, s = 0;
        for (Appointment a : DataStore.appointments) {
            if (a.getDate().equals(today) && a.getStatus().equals("Scheduled")) t++;
            if (a.getStatus().equals("Scheduled")) s++;
        }
        lblToday.setText(String.valueOf(t));
        lblScheduled.setText(String.valueOf(s));
    }

    private void logout() {
        if (!Ui.confirm(this, "Are you sure you want to log out?")) return;
        for (Window w : Window.getWindows()) {
            if (w != this && w instanceof JFrame) w.dispose();
        }
        new LoginFrame().setVisible(true);
        dispose();
    }

    private void exitApp() {
        if (Ui.confirm(this, "Exit the application?")) {
            DataStore.save();
            System.exit(0);
        }
    }
}
