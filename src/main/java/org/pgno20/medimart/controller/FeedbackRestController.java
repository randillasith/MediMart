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

/**
 * REST controller for customer feedback/reviews.
 *
 * Access rules (enforced here AND in SecurityConfig):
 *   GET  /api/feedbacks        — public (anyone can read reviews)
 *   POST /api/feedbacks        — authenticated users only (session required)
 *   PUT  /api/feedbacks/{id}   — owner only (user can only edit their own review)
 *   DELETE /api/feedbacks/{id} — owner OR admin can delete
 */
@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackRestController {

    private final FeedbackService feedbackService;

    /** Constructor injection — Spring manages FeedbackService lifecycle. */
    public FeedbackRestController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // ── GET all feedbacks (public) ────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    // ── POST — submit new feedback (logged-in users only) ────────────────────

    @PostMapping
    public ResponseEntity<?> addFeedback(@RequestBody Feedback feedback, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "You must be logged in to submit a review."));
        }

        // Always use the session name so users can't spoof another person's name
        String userFullName = (String) session.getAttribute("userFullName");
        if (userFullName == null || userFullName.trim().isEmpty()) {
            userFullName = "Verified Customer";
        }
        String userRole = (String) session.getAttribute("userRole");
        feedback.setUserName(userFullName);
        feedback.setUserRole(userRole != null ? userRole.replace("ROLE_", "") : "CUSTOMER");

        // Validate rating
        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            feedback.setRating(5);
        }

        // Validate message
        if (feedback.getMessage() == null || feedback.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Review message cannot be empty."));
        }

        Feedback saved = feedbackService.addFeedback(feedback);
        return ResponseEntity.ok(Map.of("success", true, "message", "Review submitted!", "id", saved.getId()));
    }

    // ── PUT — edit feedback (owner only) ─────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateFeedback(
            @PathVariable int id,
            @RequestBody Map<String, String> payload,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "You must be logged in to edit a review."));
        }

        // Admins cannot edit customer reviews — only delete
        String userRole = (String) session.getAttribute("userRole");
        if ("ROLE_ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Administrators can only delete reviews, not edit them."));
        }

        Feedback existing = feedbackService.getFeedbackById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Review not found."));
        }

        // Only the owner can edit their own review
        String userFullName = (String) session.getAttribute("userFullName");
        if (userFullName == null || !userFullName.equalsIgnoreCase(existing.getUserName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "You can only edit your own reviews."));
        }

        String newMessage = payload.get("message");
        if (newMessage == null || newMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Message cannot be empty."));
        }

        feedbackService.updateFeedback(id, newMessage.trim());
        return ResponseEntity.ok(Map.of("success", true, "message", "Review updated successfully."));
    }

    // ── DELETE — owner can delete own review; admin can delete any ────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFeedback(@PathVariable int id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "You must be logged in to delete a review."));
        }

        Feedback existing = feedbackService.getFeedbackById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Review not found."));
        }

        String userRole = (String) session.getAttribute("userRole");
        String userFullName = (String) session.getAttribute("userFullName");

        boolean isAdmin = "ROLE_ADMIN".equals(userRole);
        boolean isOwner = userFullName != null && userFullName.equalsIgnoreCase(existing.getUserName());

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "You can only delete your own reviews."));
        }

        feedbackService.deleteFeedback(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Review deleted successfully."));
    }
}
