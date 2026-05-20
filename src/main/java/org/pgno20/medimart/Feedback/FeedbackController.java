package org.pgno20.medimart.Feedback;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin
public class FeedbackController {

    FeedbackService service = new FeedbackService();

    // CREATE
    @PostMapping
    public String add(@RequestBody Feedback feedback) {
        service.addFeedback(feedback);
        return "Feedback Added";
    }

    // READ
    @GetMapping
    public List<Feedback> getAll() {
        return service.getAllFeedbacks();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id) {
        service.deleteFeedback(id);
        return "Deleted";
    }

    // UPDATE
    @PutMapping("/{id}")
    public String update(@PathVariable int id, @RequestBody Feedback f) {
        service.updateFeedback(id, f.getMessage());
        return "Updated";
    }
}