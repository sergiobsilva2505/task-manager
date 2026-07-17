package br.com.forjacode.taskmanager.adapters.input.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/hello")
public class HelloController {

    @GetMapping
    public ResponseEntity<Map<String, String>> sayHello() {
        return ResponseEntity.ok(Map.of("message", "Hello, World!"));
    }
}
