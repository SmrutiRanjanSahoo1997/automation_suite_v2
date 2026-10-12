package com.automation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminViewController {

    @GetMapping({"/admin", "/admin/"})
    public String adminDashboard() {
        return "forward:/admin/dashboard.html";
    }
}
