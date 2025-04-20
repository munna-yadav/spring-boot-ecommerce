package com.example.ecommerce.ecommerce.Controller;

import com.example.ecommerce.ecommerce.Dto.product.CreateProductDto;
import com.example.ecommerce.ecommerce.Entity.Product;
import com.example.ecommerce.ecommerce.Services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {
    @Autowired
    ProductService productService;

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(
            @ModelAttribute Product product,
            @RequestParam (required = false, value = "image")MultipartFile image){


        return productService.add(product,image);
    }

    @PostMapping("/cart/add")
    public ResponseEntity<?> addToCart(@RequestParam Integer productId, @RequestParam Integer quantity){
        System.out.println("welcome to cart page");
        return productService.addToCart(productId,quantity);
    }
}
