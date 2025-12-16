package com.asctutorial.app;

import com.asctutorial.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class ManagerDashboardWindow extends JFrame {

    private final PersonInfo manager;

    private JLabel lblStudentCount;
    private JLabel lblTutorCount;
    private JLabel lblSessionCount;
    private JTable tblSessions;

    private JButton btnAddStudent;
    private JButton btnAddTutor;
    private JButton btnAddSubject;
    private JButton btnCancelSession;

    public ManagerDashboardWindow(PersonInfo manager) {
        this.manager = manager;
        setTitle("Manager Dashboard - ASC Tutorial Center");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadSummaryStats();
        loadSessionsTable();
    }

    // ===================== UI SETUP =====================

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel(
                "Manager Interface - Welcome " + manager.getFullName(),
                SwingConstants.CENTER
        );
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        // Left: summary
        JPanel summaryPanel = new JPanel();
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));

        lblStudentCount = new JLabel("Students: ...");
        lblTutorCount = new JLabel("Tutors: ...");
        lblSessionCount = new JLabel("Sessions: ...");

        summaryPanel.add(lblStudentCount);
        summaryPanel.add(Box.createVerticalStrut(5));
        summaryPanel.add(lblTutorCount);
        summaryPanel.add(Box.createVerticalStrut(5));
        summaryPanel.add(lblSessionCount);

        // Center: sessions table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Upcoming Sessions"));
        tblSessions = new JTable();
        JScrollPane scroll = new JScrollPane(tblSessions);
        tablePanel.add(scroll, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                summaryPanel,
                tablePanel
        );
        split.setResizeWeight(0.25);
        root.add(split, BorderLayout.CENTER);

        // Bottom: action buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnAddStudent = new JButton("Add Student");
        btnAddTutor = new JButton("Add Tutor");
        btnAddSubject = new JButton("Add Subject");
        btnCancelSession = new JButton("Cancel Session");

        bottomPanel.add(btnAddStudent);
        bottomPanel.add(btnAddTutor);
        bottomPanel.add(btnAddSubject);
        bottomPanel.add(btnCancelSession);

        root.add(bottomPanel, BorderLayout.SOUTH);

        // Actions
        btnAddStudent.addActionListener(e -> openAddStudentDialog());
        btnAddTutor.addActionListener(e -> openAddTutorDialog());
        btnAddSubject.addActionListener(e -> openAddSubjectDialog());
        btnCancelSession.addActionListener(e -> cancelSelectedSession());

        setContentPane(root);
    }

    // ===================== LOAD DATA =====================

    private void loadSummaryStats() {
        String sqlStudents = "SELECT COUNT(*) FROM Student";
        String sqlTutors = "SELECT COUNT(*) FROM Tutor";
        String sqlSessions = "SELECT COUNT(*) FROM Session";

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(sqlStudents);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblStudentCount.setText("Students: " + rs.getInt(1));
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlTutors);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblTutorCount.setText("Tutors: " + rs.getInt(1));
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlSessions);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblSessionCount.setText("Sessions: " + rs.getInt(1));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading summary stats:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadSessionsTable() {
        String[] cols = {"SessionID", "Date", "Time", "Subject", "Tutor", "Location", "Limit"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        String sql = """
                SELECT
                    s.SessionID,
                    s.SessionDate,
                    s.SessionTime,
                    subj.SubjectName,
                    CONCAT(p.FirstName, ' ', p.LastName) AS TutorName,
                    s.Location,
                    s.StudLim
                FROM Session s
                JOIN SubjectsOffered subj ON s.SubjectID = subj.SubjectID
                JOIN Tutor t ON s.SystemID = t.SystemID
                JOIN Person p ON t.SystemID = p.SystemID
                ORDER BY s.SessionDate, s.SessionTime
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("SessionID"),
                        rs.getDate("SessionDate"),
                        rs.getTime("SessionTime"),
                        rs.getString("SubjectName"),
                        rs.getString("TutorName"),
                        rs.getString("Location"),
                        rs.getInt("StudLim")
                };
                model.addRow(row);
            }
            tblSessions.setModel(model);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading sessions:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ===================== CANCEL SESSION =====================

    private void cancelSelectedSession() {
        int row = tblSessions.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a session to cancel.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int sessionId = (int) tblSessions.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel Session ID " + sessionId + "?\n"
                        + "This will remove the session and all student registrations.",
                "Confirm Cancel Session",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // First delete all registrations for this session
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Attend WHERE SessionID = ?")) {
                ps.setInt(1, sessionId);
                ps.executeUpdate();
            }

            // Then delete the session itself
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Session WHERE SessionID = ?")) {
                ps.setInt(1, sessionId);
                ps.executeUpdate();
            }

            conn.commit();

            JOptionPane.showMessageDialog(
                    this,
                    "Session " + sessionId + " cancelled successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadSummaryStats();
            loadSessionsTable();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error cancelling session:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ===================== DIALOG LAUNCHERS =====================

    private void openAddStudentDialog() {
        AddStudentDialog dlg = new AddStudentDialog(this);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadSummaryStats();
        }
    }

    private void openAddTutorDialog() {
        AddTutorDialog dlg = new AddTutorDialog(this, manager);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadSummaryStats();
        }
    }

    private void openAddSubjectDialog() {
        AddSubjectDialog dlg = new AddSubjectDialog(this);
        dlg.setVisible(true);
        // subjects not in summary; nothing else to refresh
    }

    // ===================== ADD STUDENT DIALOG =====================

    private static class AddStudentDialog extends JDialog {

        private JTextField txtFirstName;
        private JTextField txtLastName;
        private JTextField txtEmail;
        private JTextField txtPhone;
        private JTextField txtUserName;
        private JTextField txtPassword;
        private JTextField txtPreferredLanguage;
        private boolean saved = false;

        AddStudentDialog(JFrame parent) {
            super(parent, "Add Student", true);
            setSize(400, 350);
            setLocationRelativeTo(parent);
            initUI();
        }

        boolean isSaved() {
            return saved;
        }

        private void initUI() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            int row = 0;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("First Name:"), gbc);
            txtFirstName = new JTextField();
            gbc.gridx = 1;
            panel.add(txtFirstName, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Last Name:"), gbc);
            txtLastName = new JTextField();
            gbc.gridx = 1;
            panel.add(txtLastName, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Email:"), gbc);
            txtEmail = new JTextField();
            gbc.gridx = 1;
            panel.add(txtEmail, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Phone:"), gbc);
            txtPhone = new JTextField();
            gbc.gridx = 1;
            panel.add(txtPhone, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Username:"), gbc);
            txtUserName = new JTextField();
            gbc.gridx = 1;
            panel.add(txtUserName, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Password:"), gbc);
            txtPassword = new JTextField();
            gbc.gridx = 1;
            panel.add(txtPassword, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Preferred Language:"), gbc);
            txtPreferredLanguage = new JTextField("English");
            gbc.gridx = 1;
            panel.add(txtPreferredLanguage, gbc);
            row++;

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnSave = new JButton("Save");
            JButton btnCancel = new JButton("Cancel");
            btnPanel.add(btnSave);
            btnPanel.add(btnCancel);

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.EAST;
            panel.add(btnPanel, gbc);

            setContentPane(panel);

            btnSave.addActionListener(e -> saveStudent());
            btnCancel.addActionListener(e -> dispose());
        }

        private void saveStudent() {
            String firstName = txtFirstName.getText().trim();
            String lastName = txtLastName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            String username = txtUserName.getText().trim();
            String password = txtPassword.getText().trim();
            String prefLang = txtPreferredLanguage.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty()
                    || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "First name, last name, username and password are required.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                // Next SystemID
                int newSystemId = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COALESCE(MAX(SystemID), 0) + 1 AS NewID FROM Person");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        newSystemId = rs.getInt("NewID");
                    }
                }

                // Insert into Person
                String insertPerson = """
                        INSERT INTO Person
                            (SystemID, FirstName, LastName, Email, PhoneNum, UserName, Password)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """;

                try (PreparedStatement ps = conn.prepareStatement(insertPerson)) {
                    ps.setInt(1, newSystemId);
                    ps.setString(2, firstName);
                    ps.setString(3, lastName);
                    ps.setString(4, email);
                    ps.setString(5, phone);
                    ps.setString(6, username);
                    ps.setString(7, password);
                    ps.executeUpdate();
                }

                // Insert into Student
                String insertStudent = """
                        INSERT INTO Student (SystemID, PreferredLanguage)
                        VALUES (?, ?)
                        """;

                try (PreparedStatement ps = conn.prepareStatement(insertStudent)) {
                    ps.setInt(1, newSystemId);
                    ps.setString(2, prefLang);
                    ps.executeUpdate();
                }

                conn.commit();
                saved = true;

                JOptionPane.showMessageDialog(
                        this,
                        "Student added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Error adding student:\n" + ex.getMessage(),
                        "DB Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // ===================== ADD TUTOR DIALOG =====================

    private static class AddTutorDialog extends JDialog {

        private final PersonInfo manager;

        private JTextField txtFirstName;
        private JTextField txtLastName;
        private JTextField txtEmail;
        private JTextField txtPhone;
        private JTextField txtUserName;
        private JTextField txtPassword;
        private JTextField txtPreferredLanguage;
        private JTextField txtDateHired; // yyyy-mm-dd
        private boolean saved = false;

        AddTutorDialog(JFrame parent, PersonInfo manager) {
            super(parent, "Add Tutor", true);
            this.manager = manager;
            setSize(400, 380);
            setLocationRelativeTo(parent);
            initUI();
        }

        boolean isSaved() {
            return saved;
        }

        private void initUI() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            int row = 0;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("First Name:"), gbc);
            txtFirstName = new JTextField();
            gbc.gridx = 1;
            panel.add(txtFirstName, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Last Name:"), gbc);
            txtLastName = new JTextField();
            gbc.gridx = 1;
            panel.add(txtLastName, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Email:"), gbc);
            txtEmail = new JTextField();
            gbc.gridx = 1;
            panel.add(txtEmail, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Phone:"), gbc);
            txtPhone = new JTextField();
            gbc.gridx = 1;
            panel.add(txtPhone, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Username:"), gbc);
            txtUserName = new JTextField();
            gbc.gridx = 1;
            panel.add(txtUserName, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Password:"), gbc);
            txtPassword = new JTextField();
            gbc.gridx = 1;
            panel.add(txtPassword, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Preferred Language:"), gbc);
            txtPreferredLanguage = new JTextField("English");
            gbc.gridx = 1;
            panel.add(txtPreferredLanguage, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Date Hired (YYYY-MM-DD):"), gbc);
            txtDateHired = new JTextField(LocalDate.now().toString());
            gbc.gridx = 1;
            panel.add(txtDateHired, gbc);
            row++;

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnSave = new JButton("Save");
            JButton btnCancel = new JButton("Cancel");
            btnPanel.add(btnSave);
            btnPanel.add(btnCancel);

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.EAST;
            panel.add(btnPanel, gbc);

            setContentPane(panel);

            btnSave.addActionListener(e -> saveTutor());
            btnCancel.addActionListener(e -> dispose());
        }

        private void saveTutor() {
            String firstName = txtFirstName.getText().trim();
            String lastName = txtLastName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            String username = txtUserName.getText().trim();
            String password = txtPassword.getText().trim();
            String prefLang = txtPreferredLanguage.getText().trim();
            String dateHiredStr = txtDateHired.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty()
                    || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "First name, last name, username and password are required.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                // Next SystemID
                int newSystemId = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COALESCE(MAX(SystemID), 0) + 1 AS NewID FROM Person");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        newSystemId = rs.getInt("NewID");
                    }
                }

                // Insert into Person
                String insertPerson = """
                        INSERT INTO Person
                            (SystemID, FirstName, LastName, Email, PhoneNum, UserName, Password)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """;

                try (PreparedStatement ps = conn.prepareStatement(insertPerson)) {
                    ps.setInt(1, newSystemId);
                    ps.setString(2, firstName);
                    ps.setString(3, lastName);
                    ps.setString(4, email);
                    ps.setString(5, phone);
                    ps.setString(6, username);
                    ps.setString(7, password);
                    ps.executeUpdate();
                }

                // Insert into Student (Tutor.SystemID FK -> Student.SystemID)
                String insertStudent = """
                        INSERT INTO Student (SystemID, PreferredLanguage)
                        VALUES (?, ?)
                        """;

                try (PreparedStatement ps = conn.prepareStatement(insertStudent)) {
                    ps.setInt(1, newSystemId);
                    ps.setString(2, prefLang);
                    ps.executeUpdate();
                }

                // Insert into Tutor
                String insertTutor = """
                        INSERT INTO Tutor (SystemID, DateHired, ManagedBy)
                        VALUES (?, ?, ?)
                        """;

                try (PreparedStatement ps = conn.prepareStatement(insertTutor)) {
                    ps.setInt(1, newSystemId);
                    ps.setDate(2, Date.valueOf(dateHiredStr));
                    ps.setInt(3, manager.getSystemId()); // this manager manages the tutor
                    ps.executeUpdate();
                }

                conn.commit();
                saved = true;

                JOptionPane.showMessageDialog(
                        this,
                        "Tutor added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Error adding tutor:\n" + ex.getMessage(),
                        "DB Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // ===================== ADD SUBJECT DIALOG =====================

    private static class AddSubjectDialog extends JDialog {

        private JTextField txtSubjectName;
        private boolean saved = false;

        AddSubjectDialog(JFrame parent) {
            super(parent, "Add Subject", true);
            setSize(350, 180);
            setLocationRelativeTo(parent);
            initUI();
        }

        boolean isSaved() {
            return saved;
        }

        private void initUI() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            int row = 0;

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Subject Name:"), gbc);
            txtSubjectName = new JTextField();
            gbc.gridx = 1;
            panel.add(txtSubjectName, gbc);
            row++;

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnSave = new JButton("Save");
            JButton btnCancel = new JButton("Cancel");
            btnPanel.add(btnSave);
            btnPanel.add(btnCancel);

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.EAST;
            panel.add(btnPanel, gbc);

            setContentPane(panel);

            btnSave.addActionListener(e -> saveSubject());
            btnCancel.addActionListener(e -> dispose());
        }

        private void saveSubject() {
            String subjectName = txtSubjectName.getText().trim();
            if (subjectName.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Subject name is required.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {

                int newSubjectId = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COALESCE(MAX(SubjectID), 0) + 1 AS NewID FROM SubjectsOffered");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        newSubjectId = rs.getInt("NewID");
                    }
                }

                String insertSubject = """
                        INSERT INTO SubjectsOffered (SubjectID, SubjectName)
                        VALUES (?, ?)
                        """;

                try (PreparedStatement ps = conn.prepareStatement(insertSubject)) {
                    ps.setInt(1, newSubjectId);
                    ps.setString(2, subjectName);
                    ps.executeUpdate();
                }

                saved = true;

                JOptionPane.showMessageDialog(
                        this,
                        "Subject added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Error adding subject:\n" + ex.getMessage(),
                        "DB Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
