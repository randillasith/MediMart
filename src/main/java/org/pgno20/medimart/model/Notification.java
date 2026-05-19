package org.pgno20.medimart.model;

public class Notification {
    private String message;
    private String type;
    private String role;
    private Long referenceId;

    public Notification() {}

    public Notification(String message, String type, String role, Long referenceId) {
        this.message = message;
        this.type = type;
        this.role = role;
        this.referenceId = referenceId;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
}
