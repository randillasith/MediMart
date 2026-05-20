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

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // READ
    public List<Feedback> viewFeedbacks() {

        List<Feedback> list = new ArrayList<>();

        try {

            String sql = "SELECT * FROM feedback";

            PreparedStatement pst = con.prepareStatement(sql);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                Feedback f = new Feedback();

                f.setId(rs.getInt("id"));
                f.setCustomerName(rs.getString("customer_name"));
                f.setMessage(rs.getString("message"));
                f.setRating(rs.getInt("rating"));

                list.add(f);
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return list;
    }

    // UPDATE
    public void updateFeedback(int id, String newMessage) {

        try {

            String sql = "UPDATE feedback SET message=? WHERE id=?";

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, newMessage);
            pst.setInt(2, id);

            pst.executeUpdate();

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

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}