package org.pgno20.medimart.Feedback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin // Frontend එක ලේසියෙන්ම කනෙක්ට් කරගන්න
public class FeedbackController {

    @Autowired // Dependency Injection (IOC)
    private FeedbackService service;

    // POST: http://localhost:8080/api/feedback
    @PostMapping
    public String add(@RequestBody Feedback feedback) {
        service.addFeedback(feedback);
        return "Feedback Submitted Successfully!";
    }

    // GET ALL: http://localhost:8080/api/feedback
    @GetMapping
    public List<Feedback> getAll() {
        return service.viewFeedbacks();
    }

    // GET BY ROLE: http://localhost:8080/api/feedback/role/CUSTOMER
    @GetMapping("/role/{role}")
    public List<Feedback> getByRole(@PathVariable String role) {
        return service.getFeedbacksByRole(role.toUpperCase());
    }

    // PUT: http://localhost:8080/api/feedback/1
    @PutMapping("/{id}")
    public String update(@PathVariable int id, @RequestBody Feedback feedback) {
        service.updateFeedback(id, feedback);
        return "Feedback Updated Successfully!";
    }

    // DELETE: http://localhost:8080/api/feedback/1
    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id) {
        service.deleteFeedback(id);
        return "Feedback Deleted Successfully!";
    }
}