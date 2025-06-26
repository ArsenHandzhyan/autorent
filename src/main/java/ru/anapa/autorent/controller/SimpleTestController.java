package ru.anapa.autorent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/simple-test")
public class SimpleTestController {

    @GetMapping("/hello")
    @ResponseBody
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from SimpleTestController! Time: " + java.time.LocalDateTime.now());
    }

    @GetMapping("/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> json() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "SimpleTestController работает!");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
} 