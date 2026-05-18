package Feedback;

public class Feedback {

    private int id;
    private String customerName;
    private String message;
    private int rating;

    public Feedback(String customerName, String message, int rating) {
        this.customerName = customerName;
        this.message = message;
        this.rating = rating;
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
}
