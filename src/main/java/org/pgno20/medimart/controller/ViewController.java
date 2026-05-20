package org.pgno20.medimart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.pgno20.medimart.repository.UserRepository;

/**
 * Serves Thymeleaf admin page templates.
 *
 * Authorization is now enforced entirely by Spring Security (SecurityConfig).
 * Unauthenticated or unauthorized requests are intercepted at the filter level
 * before this controller is ever reached — no manual session checks needed.
 */
@Controller
public class ViewController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/api/debug-users")
    @org.springframework.web.bind.annotation.ResponseBody
    public Object debugUsers() {
        return userRepository.findAll();
    }

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
