package org.pgno20.medimart.Feedback;

import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class FeedbackService {

    // 1. CREATE
    public void addFeedback(Feedback feedback) {
        String sql = "INSERT INTO feedback(user_name, user_role, message, rating) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, feedback.getUserName());
            pst.setString(2, feedback.getUserRole());
            pst.setString(3, feedback.getMessage());
            pst.setInt(4, feedback.getRating());

            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 2. READ ALL
    public List<Feedback> viewFeedbacks() {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT * FROM feedback";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Feedback f = new Feedback();
                f.setId(rs.getInt("id"));
                f.setUserName(rs.getString("user_name"));
                f.setUserRole(rs.getString("user_role"));
                f.setMessage(rs.getString("message"));
                f.setRating(rs.getInt("rating"));
                list.add(f);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 3. READ BY ROLE (CUSTOMER හෝ SUPPLIER අනුව වෙන් කර බැලීම)
    public List<Feedback> getFeedbacksByRole(String role) {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT * FROM feedback WHERE user_role = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, role);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Feedback f = new Feedback();
                    f.setId(rs.getInt("id"));
                    f.setUserName(rs.getString("user_name"));
                    f.setUserRole(rs.getString("user_role"));
                    f.setMessage(rs.getString("message"));
                    f.setRating(rs.getInt("rating"));
                    list.add(f);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 4. UPDATE
    public void updateFeedback(int id, Feedback feedback) {
        String sql = "UPDATE feedback SET message=?, rating=? WHERE id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, feedback.getMessage());
            pst.setInt(2, feedback.getRating());
            pst.setInt(3, id);

            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 5. DELETE
    public void deleteFeedback(int id) {
        String sql = "DELETE FROM feedback WHERE id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}