package org.pgno20.medimart.Feedback;

// User ක්ලාස් එකෙන් Inherit කර ඇත (Inheritance)
public class Feedback extends User {

    private int id;
    private String message;
    private int rating;

    // Encapsulation: Empty Constructor
    public Feedback() {
        super();
    }

    // Constructor Overloading (Polymorphism)
    public Feedback(int id, String userName, String userRole, String message, int rating) {
        super(userName, userRole); // මවු ක්ලාස් එකේ Constructor එකට දත්ත යැවීම
        this.id = id;
        this.message = message;
        this.rating = rating;
    }

    // Getters and Setters (Encapsulation)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}