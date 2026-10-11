import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Small helper class so every form has the same look. */
public class Ui {
    public static final Color PRIMARY = new Color(0, 121, 107);
    public static final Color DANGER = new Color(198, 40, 40);
    public static final Color WARN = new Color(239, 108, 0);
    public static final Color BLUE = new Color(25, 118, 210);
    public static final Color GRAY = new Color(96, 125, 139);

    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD));
        return b;
    }

    public static JPanel header(String title, String subtitle) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBackground(PRIMARY);
        p.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel t = new JLabel(title);
        t.setForeground(Color.WHITE);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 22f));
        JLabel s = new JLabel(subtitle);
        s.setForeground(new Color(200, 230, 225));
        p.add(t);
        p.add(s);
        return p;
    }

    public static void addRow(JPanel p, int row, String label, JComponent c) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row;
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        p.add(new JLabel(label), g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        p.add(c, g);
    }

    public static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static JTable table(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(24);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    public static void onChange(JTextField f, Runnable r) {
        f.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { r.run(); }
            public void removeUpdate(DocumentEvent e) { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        });
    }

    public static String money(double v) { return String.format("PHP %,.2f", v); }

    public static void error(Component c, String msg) {
        JOptionPane.showMessageDialog(c, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
    public static void info(Component c, String msg) {
        JOptionPane.showMessageDialog(c, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    public static boolean confirm(Component c, String msg) {
        return JOptionPane.showConfirmDialog(c, msg, "Confirm", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
