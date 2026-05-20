package org.pgno20.medimart.Feedback;

public class Feedback extends Person{

    private int id;
    private String message;
    private int rating;

    public Feedback(String customerName, String message, int rating) {

        super(customerName);

        this.message = message;
        this.rating = rating;
    }

    public Feedback() {
        super("");
    }



    public int getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getMessage() {
        return message;
    }

    public int getRating() {
        return rating;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
