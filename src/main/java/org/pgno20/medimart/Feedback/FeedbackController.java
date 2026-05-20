package org.pgno20.medimart.Feedback;

import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin
public class FeedbackController {

    private FeedbackService feedbackService = new FeedbackService();

    // ADD FEEDBACK
    @PostMapping
    public String addFeedback(@RequestBody Feedback feedback) {
        try {
            feedbackService.addFeedback(feedback);
            return "Feedback Added Successfully";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error Adding Feedback";
        }
    }

    // VIEW FEEDBACKS
    @GetMapping
    public List<Feedback> getFeedbacks() {
        try {
            return feedbackService.viewFeedbacks();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // DELETE FEEDBACK
    @DeleteMapping("/{id}")
    public String deleteFeedback(@PathVariable int id) {
        try {
            feedbackService.deleteFeedback(id);
            return "Feedback Deleted";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Delete Failed";
        }
    }
}