package org.pgno20.medimart.Feedback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FeedbackService {

    private Connection con;

    private void ensureConnection() throws java.sql.SQLException {
        if (con == null || con.isClosed()) {
            con = DBConnection.getConnection();
        }
    }

    // CREATE
    public void addFeedback(Feedback feedback) {
        try {
            ensureConnection();
            String sql = "INSERT INTO feedback(customer_name,message,rating) VALUES(?,?,?)";

            PreparedStatement pst = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);

            pst.setString(1, feedback.getCustomerName());
            pst.setString(2, feedback.getMessage());
            pst.setInt(3, feedback.getRating());

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    feedback.setId(rs.getInt(1));
                }
            }

            System.out.println("Feedback Added!");

        } catch (Exception e) {
            System.out.println("Error adding feedback: " + e);
        }
    }

    // READ
    public void viewFeedbacks() {
        try {
            ensureConnection();
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
            System.out.println("Error viewing feedbacks: " + e);
        }
    }

    // GET ALL FOR API
    public java.util.List<Feedback> getAllFeedbacks() {
        java.util.List<Feedback> list = new java.util.ArrayList<>();
        try {
            ensureConnection();
            String sql = "SELECT * FROM feedback ORDER BY id DESC";

            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Feedback feedback = new Feedback(
                        rs.getString("customer_name"),
                        rs.getString("message"),
                        rs.getInt("rating")
                );
                feedback.setId(rs.getInt("id"));
                list.add(feedback);
            }
        } catch (Exception e) {
            System.out.println("Error fetching feedbacks list: " + e);
        }
        return list;
    }

    // GET ONE BY ID
    public Feedback getFeedbackById(int id) {
        try {
            ensureConnection();
            String sql = "SELECT * FROM feedback WHERE id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Feedback feedback = new Feedback(
                        rs.getString("customer_name"),
                        rs.getString("message"),
                        rs.getInt("rating")
                );
                feedback.setId(rs.getInt("id"));
                return feedback;
            }
        } catch (Exception e) {
            System.out.println("Error fetching feedback by id: " + e);
        }
        return null;
    }

    // UPDATE
    public void updateFeedback(int id, String newMessage) {
        try {
            ensureConnection();
            String sql = "UPDATE feedback SET message=? WHERE id=?";

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, newMessage);
            pst.setInt(2, id);

            pst.executeUpdate();

            System.out.println("Feedback Updated!");

        } catch (Exception e) {
            System.out.println("Error updating feedback: " + e);
        }
    }

    // DELETE
    public void deleteFeedback(int id) {
        try {
            ensureConnection();
            String sql = "DELETE FROM feedback WHERE id=?";

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setInt(1, id);

            pst.executeUpdate();

            System.out.println("Feedback Deleted!");

        } catch (Exception e) {
            System.out.println("Error deleting feedback: " + e);
        }
    }
}
