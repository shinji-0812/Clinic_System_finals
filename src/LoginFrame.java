import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField txtUser = new JTextField(16);
    private final JPasswordField txtPass = new JPasswordField(16);

    public LoginFrame() {
        super("Clinic Management System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(460, 360);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        add(Ui.header("Clinic Management System", "Please log in to continue"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        Ui.addRow(form, 0, "Username:", txtUser);
        Ui.addRow(form, 1, "Password:", txtPass);
        add(form, BorderLayout.CENTER);

        JButton btnLogin = Ui.button("Login", Ui.PRIMARY);
        JButton btnClear = Ui.button("Clear", Ui.WARN);
        JButton btnExit = Ui.button("Exit", Ui.DANGER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btns.add(btnLogin);
        btns.add(btnClear);
        btns.add(btnExit);

        JLabel hint = new JLabel("Demo: admin / admin123   or   reception / clinic123", SwingConstants.CENTER);
        hint.setForeground(Color.GRAY);
        JPanel south = new JPanel(new BorderLayout());
        south.add(btns, BorderLayout.CENTER);
        south.add(hint, BorderLayout.SOUTH);
        south.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        add(south, BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> login());
        btnClear.addActionListener(e -> { txtUser.setText(""); txtPass.setText(""); txtUser.requestFocus(); });
        btnExit.addActionListener(e -> { if (Ui.confirm(this, "Exit the application?")) System.exit(0); });
        getRootPane().setDefaultButton(btnLogin); // Enter key = Login
    }

    private void login() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            Ui.error(this, "Username and password are required.");
            return;
        }
        String role = DataStore.authenticate(user, pass);
        if (role == null) {
            Ui.error(this, "Invalid username or password.");
            txtPass.setText("");
            return;
        }
        new DashboardFrame(user, role).setVisible(true);
        dispose();
    }
}
