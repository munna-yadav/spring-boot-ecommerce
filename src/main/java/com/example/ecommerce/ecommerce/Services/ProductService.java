package com.example.ecommerce.ecommerce.Services;

import com.cloudinary.Cloudinary;
import com.example.ecommerce.ecommerce.Entity.Product;
import com.example.ecommerce.ecommerce.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ProductService {

    @Autowired Cloudinary cloudinary;

    @Autowired UserService userService;

    @Autowired ProductRepository productRepository;

    public ResponseEntity<?> add(Product product, MultipartFile image){
        if (!userService.isAdmin()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only admins can upload a product");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(username);

        if (product.getName() == null || product.getDescription() == null || product.getStockQuantity() == null){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("some fields are missing");
        }
        if (image != null && !image.isEmpty()) {
            try {
                Map<?, ?> result = cloudinary.uploader().upload(image.getBytes(), Map.of());
                product.setImage(result.get("secure_url").toString());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image");
            }
        }

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }
}
