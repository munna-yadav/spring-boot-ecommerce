package com.example.ecommerce.ecommerce.Controller;

import com.example.ecommerce.ecommerce.Services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/v1/cart")
public class CartController {

    @Autowired
    CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestParam Integer productId, @RequestParam(required = false) Integer quantity){
        return cartService.addItem(productId,quantity);
    }

    @DeleteMapping("/delete-item")
    public ResponseEntity<?> deleteCartItem(@RequestParam Long productId){
        return cartService.removeItem(productId);
    }

    @GetMapping("/get")
    public ResponseEntity<?> getCart(){
        return cartService.getCart();
    }

    @GetMapping("/checkout")
    public ResponseEntity<?>checkout(){
        return cartService.checkout();
    }


}
