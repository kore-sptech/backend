package kore.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Hidden;

import org.springframework.web.bind.annotation.GetMapping;

@RequestMapping("/test")
@RestController
@Hidden
public class TestController {

    @GetMapping
    public String test() {
        return "Test OK";
    }

}
