package com.asctutorial.app;

import com.asctutorial.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TutorDashboardWindow extends JFrame {

    private final PersonInfo tutor;
    private JTable tblTutorSessions;

    public TutorDashboardWindow(PersonInfo tutor) {
        this.tutor = tutor;

        setTitle("Tutor Dashboard - ASC Tutorial Center");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadTutorSessions();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel(
                "Tutor Interface - Welcome " + tutor.getFullName(),
                SwingConstants.CENTER
        );
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Your Sessions"));

        tblTutorSessions = new JTable();
        tablePanel.add(new JScrollPane(tblTutorSessions), BorderLayout.CENTER);

        root.add(tablePanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void loadTutorSessions() {
        String[] cols = {"SessionID", "Date", "Time", "Subject", "Language", "Location", "# Students"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        String sql = """
                SELECT 
                    s.SessionID,
                    s.SessionDate,
                    s.SessionTime,
                    subj.SubjectName,
                    lang.Language,
                    s.Location,
                    COUNT(a.SystemID) AS StudentCount
                FROM Session s
                JOIN SubjectsOffered subj ON s.SubjectID = subj.SubjectID
                JOIN AvailableLanguage lang ON s.LanguageID = lang.LanguageID
                LEFT JOIN Attend a ON s.SessionID = a.SessionID
                WHERE s.SystemID = ?
                GROUP BY s.SessionID, s.SessionDate, s.SessionTime,
                         subj.SubjectName, lang.Language, s.Location
                ORDER BY s.SessionDate, s.SessionTime
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tutor.getSystemId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                            rs.getInt("SessionID"),
                            rs.getDate("SessionDate"),
                            rs.getTime("SessionTime"),
                            rs.getString("SubjectName"),
                            rs.getString("Language"),
                            rs.getString("Location"),
                            rs.getInt("StudentCount")
                    };
                    model.addRow(row);
                }
            }

            tblTutorSessions.setModel(model);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading tutor sessions:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
