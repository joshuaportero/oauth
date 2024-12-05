package dev.portero.oauth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("api/v1/products")
public class ProductController {

    @GetMapping
    public ResponseEntity<?> getProducts() {
        return ResponseEntity.ok("products");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@RequestAttribute("firebaseUid") String uid, @RequestAttribute("email") String email, @PathVariable("id") String id) {
        return ResponseEntity.ok("product with id " + id + " for user " + uid + " with email " + email);
    }
}
