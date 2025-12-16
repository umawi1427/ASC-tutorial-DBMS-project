package com.asctutorial.app;

import javax.swing.*;
import java.awt.*;

public class DashboardWindow extends JFrame {

    private final String username;

    public DashboardWindow(String username) {
        this.username = username;

        setTitle("ASC Tutorial Center - Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel lblWelcome = new JLabel(
                "Welcome to the ASC Tutorial Center Dashboard, " + username + "!",
                SwingConstants.CENTER
        );
        lblWelcome.setFont(lblWelcome.getFont().deriveFont(Font.BOLD, 18f));

        panel.add(lblWelcome, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.add(new JLabel("TODO: Add student, tutor, appointment management here."));
        panel.add(center, BorderLayout.CENTER);

        add(panel);
    }
}
