package com.example.ecommerce.ecommerce.Services;

import com.cloudinary.Cloudinary;
import com.example.ecommerce.ecommerce.Entity.*;
import com.example.ecommerce.ecommerce.Repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired CustomerService customerService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    Cloudinary cloudinary;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    CartRepository cartRepository;

    @Autowired
    CustomerRepository customerRepository;

    public ResponseEntity<?> add(Product product, MultipartFile image) {

        if (!customerService.isAdmin()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only admins can upload a product");
        }

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

    public ResponseEntity<?>addToCart(Integer productId, Integer quantity){

        // get the current user
        String username = customerService.extractUsernameFromToken();
        Optional<Users> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not authorized");
        }
        Users customer = optionalUser.get();

        // Get the product
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product with productId not found");
        }

        Product product = optionalProduct.get();

        System.out.println(product);
        // Get or create Customer cart

       Cart cart = customer.getCart();
       if (cart == null){
           cart = new Cart();
           cart.setCustomer(customer);
           cart = cartRepository.save(cart);
           customer.setCart(cart);
       }
        System.out.println(cart);
       // check if product already exists in cart

        boolean productExists = false;
       for (CartItem item : cart.getCartItems()){
           if (item.getProduct().getId() == productId){
               item.setQuantity(item.getQuantity()+ quantity);
               cartItemRepository.save(item);
               productExists = true;
               break;
           }
       }

       // if product doesnt exist in the cart create a new cartItem

        if (!productExists){
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setCart(cart);
            cart.getCartItems().add(cartItem);
            cartItemRepository.save(cartItem);
        }
        return ResponseEntity.ok(Map.of(
                "message", "Product added to cart",
                "cartTotal", cart.getTotalAmount()
        ));
    }
}
