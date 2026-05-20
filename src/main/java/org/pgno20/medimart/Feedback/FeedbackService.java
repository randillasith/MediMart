package org.pgno20.medimart.Feedback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackService {

    Connection con = DBConnection.getConnection();

    // CREATE
    public void addFeedback(Feedback feedback) {

        try {

            String sql = "INSERT INTO feedback(customer_name, message, rating) VALUES (?, ?, ?)";

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, feedback.getCustomerName());
            pst.setString(2, feedback.getMessage());
            pst.setInt(3, feedback.getRating());

            pst.executeUpdate();

            System.out.println("Feedback Added Successfully!");

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // READ
    public List<Feedback> viewFeedbacks() throws SQLException {

        List<Feedback> feedbackList = new ArrayList<>();

        String sql = "SELECT * FROM feedback";

        PreparedStatement pst = con.prepareStatement(sql);

        ResultSet rs = pst.executeQuery();

        while (rs.next()) {

            Feedback feedback = new Feedback();

            feedback.setId(rs.getInt("id"));
            feedback.setCustomerName(rs.getString("customer_name"));
            feedback.setMessage(rs.getString("message"));
            feedback.setRating(rs.getInt("rating"));

            feedbackList.add(feedback);
        }

        return feedbackList;
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