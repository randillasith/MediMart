package org.pgno20.medimart.Feedback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FeedbackService {

    Connection con = DBConnection.getConnection();

    // CREATE
    public void addFeedback(Feedback feedback) {

        try {

            String sql = "INSERT INTO feedback(customer_name,message,rating) VALUES(?,?,?)";

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, feedback.getCustomerName());
            pst.setString(2, feedback.getMessage());
            pst.setInt(3, feedback.getRating());

            pst.executeUpdate();

            System.out.println("Feedback Added!");

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // READ
    public void viewFeedbacks() {

        try {

            String sql = "SELECT * FROM feedback";

            PreparedStatement pst = con.prepareStatement(sql);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                System.out.println(
                        rs.getInt("id") + " | " +
                                rs.getString("customer_name") + " | " +
                                rs.getString("message") + " | " +
                                rs.getInt("rating")
                );
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // UPDATE
    public void updateFeedback(int id, String newMessage) {

        try {

            String sql = "UPDATE feedback SET message=? WHERE id=?";

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, newMessage);
            pst.setInt(2, id);

            pst.executeUpdate();

            System.out.println("Feedback Updated!");

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // DELETE
    public void deleteFeedback(int id) {

        try {

            String sql = "DELETE FROM feedback WHERE id=?";

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setInt(1, id);

            pst.executeUpdate();

            System.out.println("Feedback Deleted!");

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
