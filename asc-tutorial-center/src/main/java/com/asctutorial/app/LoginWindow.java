package com.asctutorial.app;

import com.asctutorial.util.DatabaseConnection;

import javax.swing.*;
import java.awt.*;

public class LoginWindow extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;

    public LoginWindow() {
        setTitle("ASC Tutorial Center - Login");
        setSize(400, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("ASC Tutorial Center Login", SwingConstants.CENTER);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 16f));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);

        txtUsername = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);

        txtPassword = new JPasswordField();
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(txtPassword, gbc);

        // Buttons
        btnLogin = new JButton("Login");
        btnExit = new JButton("Exit");

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(btnLogin, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(btnExit, gbc);

        add(panel);

        // Actions
        btnLogin.addActionListener(e -> handleLogin());
        btnExit.addActionListener(e -> System.exit(0));
        getRootPane().setDefaultButton(btnLogin);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Username and password are required.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            PersonInfo person = DatabaseConnection.authenticateAndGetPerson(username, password);

            if (person == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid credentials.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JOptionPane.showMessageDialog(this, "Login successful!");

            String role = person.getRole();
            if (role == null) {
                role = "UNKNOWN";
            }
            role = role.toUpperCase();

            // 🔹 Open the correct dashboard by role
            switch (role) {
                case "MANAGER" -> {
                    ManagerDashboardWindow mdash = new ManagerDashboardWindow(person);
                    mdash.setVisible(true);
                }
                case "TUTOR" -> {
                    TutorDashboardWindow tdash = new TutorDashboardWindow(person);
                    tdash.setVisible(true);
                }
                case "STUDENT" -> {
                    StudentDashboardWindow sdash = new StudentDashboardWindow(person);
                    sdash.setVisible(true);
                }
                default -> {
                    // fallback generic dashboard if something is off
                    DashboardWindow dashboard = new DashboardWindow(person.getUserName());
                    dashboard.setVisible(true);
                }
            }

            // close login window
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this, "Error connecting to database:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
