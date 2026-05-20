package org.pgno20.medimart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves Thymeleaf admin page templates.
 *
 * Authorization is now enforced entirely by Spring Security (SecurityConfig).
 * Unauthenticated or unauthorized requests are intercepted at the filter level
 * before this controller is ever reached — no manual session checks needed.
 */
@Controller
public class ViewController {

    /** Admin: main dashboard overview */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    /** Admin: staff / user management page */
    @GetMapping("/users-portal")
    public String users() {
        return "staffdetails";
    }

    /** Admin + Staff: main inventory / medicines dashboard */
    @GetMapping("/medicines")
    public String medicines() {
        return "medicines";
    }

    /** Admin: admin profile / details page */
    @GetMapping("/addmindetails")
    public String addmindetails() {
        return "addmindetails";
    }

    /** Admin: supplier management page */
    @GetMapping("/supplier-details")
    public String supplierDetails() {
        return "suppliers";
    }

    /** Supplier: personal dashboard */
    @GetMapping("/supplier-dashboard")
    public String supplierDashboard() {
        return "supplier-dashboard";
    }

    /** Admin: orders management page */
    @GetMapping("/orders-management")
    public String ordersManagement() {
        return "orders";
    }

    /** Admin: prescription verification queue */
    @GetMapping("/prescriptions")
    public String prescriptions() {
        return "prescriptions";
    }

    /** Admin: system configuration & global settings */
    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }

    /** Admin: user feedback management dashboard */
    @GetMapping("/feedback-management")
    public String feedbackManagement() {
        return "feedback";
    }

    /** Public: logout redirect (session cleared via POST /api/auth/logout) */
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/index.html";
    }

    /** Public: home page alias */
    @GetMapping("/home")
    public String homePage() {
        return "forward:/index.html";
    }
}
