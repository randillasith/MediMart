package org.pgno20.medimart.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class ViewController {

    @GetMapping("/users-portal")
    public String users() {
        // Must match exactly: src/main/resources/templates/userdetails.html
        return "staffdetails"; 
    }

    @GetMapping("/medicines") // Changed from /medicine to match your sidebar link
    public String medicines() {

        return "medicines"; 
    }

     @GetMapping("/addmindetails") // Changed from /medicine to match your sidebar link
    public String addmindetails() {

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
}