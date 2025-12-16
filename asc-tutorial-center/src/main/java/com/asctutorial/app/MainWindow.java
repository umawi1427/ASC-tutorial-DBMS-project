package com.asctutorial.app;

import javax.swing.SwingUtilities;

public class MainWindow {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginWindow login = new LoginWindow();
            login.setVisible(true);
        });
    }
}
