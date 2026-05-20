package org.pgno20.medimart.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.pgno20.medimart.Feedback.Feedback;
import org.pgno20.medimart.Feedback.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackRestController {

    private final FeedbackService feedbackService = new FeedbackService();

    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    @PostMapping
    public ResponseEntity<?> addFeedback(@RequestBody Feedback feedback, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "You must log in to submit a review"));
        }

        String userFullName = (String) session.getAttribute("userFullName");
        if (userFullName == null || userFullName.trim().isEmpty()) {
            userFullName = "Verified Customer";
        }

        feedback.setCustomerName(userFullName);
        feedbackService.addFeedback(feedback);
        return ResponseEntity.ok(feedback);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateFeedback(
            @PathVariable int id,
            @RequestBody Map<String, String> payload,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "You must log in to edit reviews"));
        }

        String userRole = (String) session.getAttribute("userRole");
        if ("ROLE_ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Administrators cannot modify customer reviews"));
        }

        Feedback existing = feedbackService.getFeedbackById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Review not found"));
        }

        String userFullName = (String) session.getAttribute("userFullName");
        if (!userFullName.equalsIgnoreCase(existing.getCustomerName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "You are not authorized to update this review"));
        }

        String newMessage = payload.get("message");
        if (newMessage == null || newMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Message content cannot be empty"));
        }

        feedbackService.updateFeedback(id, newMessage);
        return ResponseEntity.ok(Map.of("success", true, "message", "Feedback updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFeedback(@PathVariable int id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "You must log in to delete reviews"));
        }

        String userRole = (String) session.getAttribute("userRole");
        String userFullName = (String) session.getAttribute("userFullName");

        Feedback existing = feedbackService.getFeedbackById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Review not found"));
        }

        boolean isAdmin = "ROLE_ADMIN".equals(userRole);
        boolean isOwner = userFullName != null && userFullName.equalsIgnoreCase(existing.getCustomerName());

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "You are not authorized to delete this review"));
        }

        feedbackService.deleteFeedback(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Feedback deleted successfully"));
    }
}
