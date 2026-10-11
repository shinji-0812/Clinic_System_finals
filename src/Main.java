import java.awt.GraphicsEnvironment;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("This application requires a graphical desktop environment. It cannot run in a headless environment.");
            return;
        }

        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) { }
        DataStore.load();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
