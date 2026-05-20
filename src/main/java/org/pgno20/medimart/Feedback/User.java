package org.pgno20.medimart.Feedback;

public class User {
    protected String userName;
    protected String userRole; // CUSTOMER, SUPPLIER

    public User() {}

    public User(String userName, String userRole) {
        this.userName = userName;
        this.userRole = userRole;
    }

    // Getters and Setters
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
}