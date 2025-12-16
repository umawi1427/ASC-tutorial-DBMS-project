package com.asctutorial.app;

public class PersonInfo {

    private final int systemId;
    private final String firstName;
    private final String lastName;
    private final String userName;
    private final String role; // "MANAGER", "TUTOR", "STUDENT", "UNKNOWN"

    public PersonInfo(int systemId, String firstName, String lastName, String userName, String role) {
        this.systemId = systemId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.role = role;
    }

    public int getSystemId() {
        return systemId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUserName() {
        return userName;
    }

    public String getRole() {
        return role;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}