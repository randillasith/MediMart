package org.pgno20.medimart.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class ViewController {

    @GetMapping("/users-portal")
    public String users(HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login.html";
        if (!"ROLE_ADMIN".equals(session.getAttribute("userRole"))) return "redirect:/medicines";
        return "staffdetails"; 
    }

    @GetMapping("/medicines") 
    public String medicines(HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login.html";
        return "medicines"; 
    }

    @GetMapping("/addmindetails") 
    public String addmindetails(HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login.html";
        if (!"ROLE_ADMIN".equals(session.getAttribute("userRole"))) return "redirect:/medicines";
        return "addmindetails"; 
    }

   @GetMapping("/logout")
public String logout(HttpSession session) {
    session.invalidate(); // Clears admin data
    
    // Use "redirect:" to the actual filename since it's in the static folder
    return "redirect:/index.html"; 
}



    @GetMapping("/home")
    public String homePage() {
        // This looks for src/main/resources/templates/index.html
        return "index"; 
    }

    @GetMapping("/supplier-details")
    public String supplierDetails(HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login.html";
        return "suppliers";
    }
}