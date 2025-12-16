package com.asctutorial.util;

import com.asctutorial.app.PersonInfo;

import java.sql.*;

public class DatabaseConnection {

    // TODO: change these to YOUR actual MySQL settings
    private static final String URL = "jdbc:mysql://localhost:3306/ASCTutorialCenter?allowMultiQueries=true";
    private static final String USER = "root";          // your MySQL user
    private static final String PASSWORD = "root";  // your MySQL password

    static {
        try {
            // MySQL 8+ driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Checks if there is a record in Person table that matches
     * the given username and password.
     */
    public static boolean authenticate(String username, String password) throws SQLException {
        return authenticateAndGetPerson(username, password) != null;
//        String sql = "SELECT SystemID FROM Person WHERE UserName = ? AND Password = ?";
//
//        try (Connection conn = getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//
//            ps.setString(1, username);
//            ps.setString(2, password);
//
//            try (ResultSet rs = ps.executeQuery()) {
//                // If there is at least one row -> credentials are valid
//                return rs.next();
//            }
//        }
    }
    public static PersonInfo authenticateAndGetPerson(String username, String password) throws SQLException {
        String sql = """
                SELECT 
                    p.SystemID,
                    p.FirstName,
                    p.LastName,
                    p.UserName,
                    CASE
                        WHEN s.SystemID IS NOT NULL THEN 'STUDENT'
                        WHEN t.SystemID IS NOT NULL THEN 'TUTOR'
                        WHEN m.SystemID IS NOT NULL THEN 'MANAGER'
                        ELSE 'UNKNOWN'
                    END AS Role
                FROM Person p
                LEFT JOIN Student s ON p.SystemID = s.SystemID
                LEFT JOIN Tutor   t ON p.SystemID = t.SystemID
                LEFT JOIN Manager m ON p.SystemID = m.SystemID
                WHERE p.UserName = ? AND p.Password = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int systemId = rs.getInt("SystemID");
                    String firstName = rs.getString("FirstName");
                    String lastName = rs.getString("LastName");
                    String userName = rs.getString("UserName");
                    String role = rs.getString("Role");
                    return new PersonInfo(systemId, firstName, lastName, userName, role);
                }
            }
        }
        return null; // invalid login
    }
}
