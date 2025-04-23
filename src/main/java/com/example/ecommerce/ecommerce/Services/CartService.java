package com.example.ecommerce.ecommerce.Services;

import com.example.ecommerce.ecommerce.Dto.Cart.CartResponse;
import com.example.ecommerce.ecommerce.Dto.Cart.OrderDTO;
import com.example.ecommerce.ecommerce.Entity.*;
import com.example.ecommerce.ecommerce.Enum.OrderStatus;
import com.example.ecommerce.ecommerce.Repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class CartService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    public ResponseEntity<?> addItem(Integer productId, Integer quantity) {

        if (quantity == null){
            quantity = 1;
        }

        // first get the user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Users> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authorized");
        }
        Users user = optionalUser.get();

        // Get the product
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product with id not found");
        }
        Product product = optionalProduct.get();

        // Get or create cart for the user

        Cart cart = user.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
            user.setCart(cart);
        }

        // check if product already exists in the cart
        boolean productExists = false;

        for (CartItem item : cart.getCartItems()) {

            if (item.getProduct().getId() == productId) {
                item.setQuantity(item.getQuantity() + quantity);
                cartItemRepository.save(item);
                productExists = true;
                break;
            }

        }

        // if product doesn't exists in the cart
        if (!productExists) {
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

    public ResponseEntity<?> removeItem(Long productId){


        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Users> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user no authorized");
        }
        Users  user = optionalUser.get();

        // get user cat;
        Cart userCart = user.getCart();
        if (userCart == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User cart is empty") ;
        }

        CartItem itemToRemove = null;
       for (CartItem item: userCart.getCartItems()){
           if (item.getProduct().getId() == productId){
               itemToRemove = item;
               break;
           }
       }
       if (itemToRemove == null){
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found in cart");
       }

        // remove item from cart

        userCart.removeCartItem(itemToRemove);
       cartItemRepository.delete(itemToRemove);
       cartRepository.save(userCart);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message","item removed from cart","Cart Total:",userCart.getTotalAmount()));
    }

    public ResponseEntity<?> getCart(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Users>optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated or authorized");
        }
        Users user = optionalUser.get();

        // get the user cart
        Cart userCart = user.getCart();

        if (userCart == null){
            return ResponseEntity.ok(Map.of(
                    "message", "Cart is empty"
            ));
        }

        // create a list out of the cart items
        List<CartResponse> responseList = new ArrayList<>();

        for (CartItem item: userCart.getCartItems()){

            // Create a new CartResponse object for each item
            CartResponse response = new CartResponse();
            response.setQuantity(item.getQuantity());
            response.setPrice(item.getProduct().getPrice());
            response.setProduct(item.getProduct().getName());
            response.setProductId(item.getProduct().getId());
            response.setTotalPrice(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))); // Calculate total price
            responseList.add(response);
        }

        return ResponseEntity.ok(Map.of(
                "message", "Cart retrieved successfully",
                "cart", responseList,
                "totalAmount", userCart.getTotalAmount()
        ));
    }

    public ResponseEntity<?> checkout(){

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Users> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not authorized");
        }

        Users user = optionalUser.get();

        // get the user cart;

        Cart userCart = user.getCart();

        // TODO ensure all the products in cart are available during checkout

        Order order = userCart.checkout(OrderStatus.PENDING,user.getAddress(),"cash");
        order.setCustomer(user);
        orderRepository.save(order);
        OrderDTO dto = modelMapper.map(order,OrderDTO.class);

        return ResponseEntity.ok(Map.of("order",dto));
    }
}