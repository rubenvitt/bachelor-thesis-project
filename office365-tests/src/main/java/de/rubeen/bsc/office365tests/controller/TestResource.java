package de.rubeen.bsc.office365tests.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestResource {

    @GetMapping(path = "/test")
    public String abc() {
        return "test success";
    }
}
