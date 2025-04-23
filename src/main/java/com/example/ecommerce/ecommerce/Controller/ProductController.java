package com.example.ecommerce.ecommerce.Controller;

import com.cloudinary.Cloudinary;
import com.example.ecommerce.ecommerce.Entity.Product;
import com.example.ecommerce.ecommerce.Services.AuthService;
import com.example.ecommerce.ecommerce.Services.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    Cloudinary cloudinary;

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@ModelAttribute Product product, @RequestParam(name = "image",required = false)MultipartFile image){
        return productService.add(product,image);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin-only")
    public ResponseEntity<String> adminOnlyEndpoint() {
        return ResponseEntity.ok("Welcome, Admin!");
    }

}

