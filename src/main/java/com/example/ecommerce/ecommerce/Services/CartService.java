package com.example.ecommerce.ecommerce.Services;

import com.example.ecommerce.ecommerce.Dto.Cart.CartResponse;
import com.example.ecommerce.ecommerce.Dto.Cart.OrderDTO;
import com.example.ecommerce.ecommerce.Dto.Cart.OrderItemDTO;
import com.example.ecommerce.ecommerce.Entity.*;
import com.example.ecommerce.ecommerce.Enum.OrderStatus;
import com.example.ecommerce.ecommerce.Repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            response.setImage(item.getProduct().getImage());
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

    @Transactional
    public ResponseEntity<?> checkout(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Users> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not authorized");
        }

        Users user = optionalUser.get();
        Cart userCart = user.getCart();
        
        if (userCart == null || userCart.getCartItems().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cart is empty");
        }
        
        // Check stock availability
        for (CartItem item : userCart.getCartItems()) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                return ResponseEntity.badRequest().body(
                    "Not enough stock for " + product.getName() + 
                    ". Available: " + product.getStockQuantity() + 
                    ", Requested: " + item.getQuantity());
            }
        }

        // Create the order
        Order order = userCart.checkout(OrderStatus.PENDING, user.getAddress(), "cash");
        
        // Update product stock quantities
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            int newStock = product.getStockQuantity() - item.getQuantity();
            product.setStockQuantity(newStock);
        }
        
        // Save the order first
        orderRepository.save(order);
        
        // Now clear the cart in a separate transaction
        clearCart(userCart);

        // Create DTO without circular references
        OrderDTO dto = createOrderDTO(order, user);
        
        return ResponseEntity.ok(Map.of(
                "message", "Order placed successfully",
                "order", dto
        ));
    }

    @Transactional
    private void clearCart(Cart userCart) {
        List<CartItem> cartItems = new ArrayList<>(userCart.getCartItems());
        for (CartItem item : cartItems) {
            userCart.removeCartItem(item);
            cartItemRepository.delete(item);
        }
        cartRepository.save(userCart);
    }

    private OrderDTO createOrderDTO(Order order, Users user) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCustomerName(user.getUsername());
        dto.setCustomerEmail(user.getEmail());
        
        // Map order items
        for (OrderItem item : order.getOrderItems()) {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProductName(item.getProduct().getName());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setPrice(item.getPrice());
            itemDTO.setTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            dto.getItems().add(itemDTO);
        }
        
        return dto;
    }
}