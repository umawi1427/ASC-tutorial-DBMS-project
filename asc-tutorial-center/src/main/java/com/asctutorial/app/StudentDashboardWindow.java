package com.asctutorial.app;

import com.asctutorial.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.sql.Date;
import java.sql.Time;

// Simple student dashboard with "New Appointment" button
public class StudentDashboardWindow extends JFrame {
    private final PersonInfo student;
    private JTable tblStudentSessions;
    private JButton btnNewAppointment;

    public StudentDashboardWindow(PersonInfo student) {
        this.student = student;
        setTitle("Student Dashboard - ASC Tutorial Center");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        loadStudentSessions();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel(
                "Student Interface - Welcome " + student.getFullName(),
                SwingConstants.CENTER
        );
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Your Registered Sessions"));
        tblStudentSessions = new JTable();
        tablePanel.add(new JScrollPane(tblStudentSessions), BorderLayout.CENTER);
        root.add(tablePanel, BorderLayout.CENTER);

        // Bottom panel with button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNewAppointment = new JButton("New Appointment");
        bottomPanel.add(btnNewAppointment);
        root.add(bottomPanel, BorderLayout.SOUTH);

        // Button action
        btnNewAppointment.addActionListener(e -> openNewAppointmentDialog());

        setContentPane(root);
    }

    private void loadStudentSessions() {
        String[] cols = {"SessionID", "Date", "Time", "Subject", "Language", "Location", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        // Use the actual column name 'Status'
        String sql = """
                SELECT
                    s.SessionID,
                    s.SessionDate,
                    s.SessionTime,
                    subj.SubjectName,
                    lang.Language,
                    s.Location,
                    a.Status AS Status
                FROM Attend a
                JOIN Session s ON a.SessionID = s.SessionID
                JOIN SubjectsOffered subj ON s.SubjectID = subj.SubjectID
                JOIN AvailableLanguage lang ON s.LanguageID = lang.LanguageID
                WHERE a.SystemID = ?
                ORDER BY s.SessionDate, s.SessionTime
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, student.getSystemId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                            rs.getInt("SessionID"),
                            rs.getDate("SessionDate"),
                            rs.getTime("SessionTime"),
                            rs.getString("SubjectName"),
                            rs.getString("Language"),
                            rs.getString("Location"),
                            rs.getString("Status")
                    };
                    model.addRow(row);
                }
            }
            tblStudentSessions.setModel(model);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading student sessions:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void openNewAppointmentDialog() {
        NewAppointmentDialog dlg = new NewAppointmentDialog(this, student);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            // refresh table after a successful new appointment
            loadStudentSessions();
        }
    }

    /**
     * Small helper class for combo box items (id + label).
     */
    private static class ComboItem {
        private final int id;
        private final String label;

        ComboItem(int id, String label) {
            this.id = id;
            this.label = label;
        }

        int getId() {
            return id;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * Dialog to create a new appointment (Session + Attend).
     */
    private static class NewAppointmentDialog extends JDialog {

        private final PersonInfo student;
        private JComboBox<ComboItem> cboTutor;
        private JComboBox<ComboItem> cboSubject;
        private JComboBox<ComboItem> cboLanguage;
        private JTextField txtDate;     // yyyy-mm-dd
        private JTextField txtTime;     // HH:mm:ss
        private JTextField txtLocation; // e.g. Room 101
        private boolean saved = false;

        NewAppointmentDialog(JFrame parent, PersonInfo student) {
            super(parent, "New Appointment", true);
            this.student = student;

            setSize(450, 300);
            setLocationRelativeTo(parent);
            initUI();
            loadComboData();
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

            // Tutor
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Tutor:"), gbc);

            cboTutor = new JComboBox<>();
            gbc.gridx = 1;
            panel.add(cboTutor, gbc);
            row++;

            // Subject
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Subject:"), gbc);

            cboSubject = new JComboBox<>();
            gbc.gridx = 1;
            panel.add(cboSubject, gbc);
            row++;

            // Language
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Language:"), gbc);

            cboLanguage = new JComboBox<>();
            gbc.gridx = 1;
            panel.add(cboLanguage, gbc);
            row++;

            // Date
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);

            txtDate = new JTextField();
            txtDate.setText(LocalDate.now().toString()); // default to today
            gbc.gridx = 1;
            panel.add(txtDate, gbc);
            row++;

            // Time
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Time (HH:MM:SS):"), gbc);

            txtTime = new JTextField("10:00:00");
            gbc.gridx = 1;
            panel.add(txtTime, gbc);
            row++;

            // Location
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Location:"), gbc);

            txtLocation = new JTextField("Room 101");
            gbc.gridx = 1;
            panel.add(txtLocation, gbc);
            row++;

            // Buttons
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

            btnSave.addActionListener(e -> saveAppointment());
            btnCancel.addActionListener(e -> dispose());
        }

        private void loadComboData() {
            // load tutors
            String sqlTutors = """
                    SELECT t.SystemID, CONCAT(p.FirstName, ' ', p.LastName) AS TutorName
                    FROM Tutor t
                    JOIN Person p ON t.SystemID = p.SystemID
                    ORDER BY TutorName
                    """;

            String sqlSubjects = """
                    SELECT SubjectID, SubjectName
                    FROM SubjectsOffered
                    ORDER BY SubjectName
                    """;

            String sqlLanguages = """
                    SELECT LanguageID, Language
                    FROM AvailableLanguage
                    ORDER BY Language
                    """;

            try (Connection conn = DatabaseConnection.getConnection()) {

                // Tutors
                try (PreparedStatement ps = conn.prepareStatement(sqlTutors);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("SystemID");
                        String name = rs.getString("TutorName");
                        cboTutor.addItem(new ComboItem(id, name));
                    }
                }

                // Subjects
                try (PreparedStatement ps = conn.prepareStatement(sqlSubjects);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("SubjectID");
                        String name = rs.getString("SubjectName");
                        cboSubject.addItem(new ComboItem(id, name));
                    }
                }

                // Languages
                try (PreparedStatement ps = conn.prepareStatement(sqlLanguages);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("LanguageID");
                        String name = rs.getString("Language");
                        cboLanguage.addItem(new ComboItem(id, name));
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Error loading combo data:\n" + ex.getMessage(),
                        "DB Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

        private void saveAppointment() {
            ComboItem tutorItem = (ComboItem) cboTutor.getSelectedItem();
            ComboItem subjectItem = (ComboItem) cboSubject.getSelectedItem();
            ComboItem languageItem = (ComboItem) cboLanguage.getSelectedItem();
            String dateStr = txtDate.getText().trim();
            String timeStr = txtTime.getText().trim();
            String location = txtLocation.getText().trim();

            if (tutorItem == null || subjectItem == null || languageItem == null ||
                    dateStr.isEmpty() || timeStr.isEmpty() || location.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "All fields are required.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                // Get next SessionID
                int newSessionId = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COALESCE(MAX(SessionID), 0) + 1 AS NewID FROM Session");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        newSessionId = rs.getInt("NewID");
                    }
                }

                // Insert into Session (StudLim set to 1 for a single appointment)
                String insertSession = """
                        INSERT INTO Session
                            (SessionID, SessionDate, SessionTime, Location,
                             StudLim, SubjectID, SystemID, LanguageID)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """;

                try (PreparedStatement ps = conn.prepareStatement(insertSession)) {
                    ps.setInt(1, newSessionId);
                    ps.setDate(2, Date.valueOf(dateStr));       // may throw if format is wrong
                    ps.setTime(3, Time.valueOf(timeStr));       // may throw if format is wrong
                    ps.setString(4, location);
                    ps.setInt(5, 1);                            // StudLim = 1
                    ps.setInt(6, subjectItem.getId());
                    ps.setInt(7, tutorItem.getId());            // tutor SystemID
                    ps.setInt(8, languageItem.getId());
                    ps.executeUpdate();
                }

                // Insert into Attend (student registers, Status set as 'Registered')
                String insertAttend = """
                        INSERT INTO Attend (SystemID, SessionID, DateReg, Status)
                        VALUES (?, ?, ?, ?)
                        """;

                try (PreparedStatement ps = conn.prepareStatement(insertAttend)) {
                    ps.setInt(1, student.getSystemId());
                    ps.setInt(2, newSessionId);
                    ps.setDate(3, Date.valueOf(LocalDate.now()));
                    ps.setString(4, "Registered");
                    ps.executeUpdate();
                }

                conn.commit();
                saved = true;
                JOptionPane.showMessageDialog(
                        this,
                        "Appointment created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Error saving appointment:\n" + ex.getMessage(),
                        "DB Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
