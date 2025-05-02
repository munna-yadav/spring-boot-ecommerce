package com.example.ecommerce.ecommerce.Controller;

import com.cloudinary.Cloudinary;
import com.example.ecommerce.ecommerce.Dto.product.CreateProductDto;
import com.example.ecommerce.ecommerce.Entity.Product;
import com.example.ecommerce.ecommerce.Services.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    Cloudinary cloudinary;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@ModelAttribute CreateProductDto dto){
        return productService.add(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin-only")
    public ResponseEntity<String> adminOnlyEndpoint() {
        return ResponseEntity.ok("Welcome, Admin!");
    }

    @GetMapping("/get")
    public ResponseEntity<?> fetchAllProduct(){
        return productService.fetchProduct();
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam("q") String query){
        return productService.searchProduct(query);
    }
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id){
        return productService.getProduct(id);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id){
        return productService.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateQuantity(@RequestBody CreateProductDto todo, @PathVariable Integer id) {
        return productService.updateProduct(todo,id);
    }
}

