// src/main/java/com/surfmaster/controller/HealthPingController.java
package com.surfmaster.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthPingController {
    @GetMapping("/ping")
    public String ping() { return "pong"; }
}
