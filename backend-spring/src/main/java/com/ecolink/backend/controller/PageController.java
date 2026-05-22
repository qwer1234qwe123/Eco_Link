package com.ecolink.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/prediction")
    public String prediction() {
        return "forward:/prediction.html";
    }

    @GetMapping("/route")
    public String route() {
        return "forward:/route.html";
    }

    @GetMapping("/trend")
    public String trend() {
        return "forward:/trend.html";
    }

    @GetMapping("/worker")
    public String worker() {
        return "forward:/worker.html";
    }

    @GetMapping("/sensor-log")
    public String sensorLog() {
        return "forward:/sensor-log.html";
    }

    @GetMapping("/collection-history")
    public String collectionHistory() {
        return "forward:/collection-history.html";
    }

    @GetMapping("/trashcan")
    public String trashcan() {
        return "forward:/trashcan.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }
}