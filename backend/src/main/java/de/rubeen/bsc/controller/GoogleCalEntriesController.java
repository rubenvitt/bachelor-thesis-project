package de.rubeen.bsc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoogleCalEntriesController {
    @GetMapping(path = "/test")
    public String banane() {
        return "";
    }
}
