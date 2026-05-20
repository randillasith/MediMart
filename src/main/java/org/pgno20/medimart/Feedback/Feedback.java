package org.pgno20.medimart.Feedback;

public class Feedback {

    private int id;
    private String customerName;
    private String message;
    private int rating;

    // EMPTY CONSTRUCTOR
    public Feedback() {
    }

    // PARAMETER CONSTRUCTOR
    public Feedback(int id, String customerName, String message, int rating) {
        this.id = id;
        this.customerName = customerName;
        this.message = message;
        this.rating = rating;
    }

    // GETTERS & SETTERS
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}