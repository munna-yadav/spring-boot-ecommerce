package com.example.ecommerce.ecommerce.Services;

import com.example.ecommerce.ecommerce.Dto.Order.OrderResponseDTO;
import com.example.ecommerce.ecommerce.Entity.Order;
import com.example.ecommerce.ecommerce.Entity.Users;
import com.example.ecommerce.ecommerce.Repository.OrderItemRepository;
import com.example.ecommerce.ecommerce.Repository.OrderRepository;
import com.example.ecommerce.ecommerce.Repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    OrderRepository orderRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    ModelMapper modelMapper;

    public ResponseEntity<?> getOrders(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Users> optionalUsers = userRepository.findByUsername(username);
        Users user = optionalUsers.get();
        List<Order> orders = orderRepository.findByCustomer(user);
        return ResponseEntity.status(HttpStatus.OK).body(orders.stream().map(order -> modelMapper.map(order, OrderResponseDTO.class)).collect(Collectors.toList()));

    }

    @Transactional
    public ResponseEntity<?> deleteOrder(Integer orderId) {
        // Get current authenticated user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Users> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Users user = optionalUser.get();

        // Check if order exists and belongs to the user
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Order not found"));
        }

        Order order = optionalOrder.get();

        // Verify ownership (assuming order has a reference to user)
        if (!order.getCustomer().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You don't have permission to delete this order"));
        }

        try {
            // Delete associated order items first
            orderItemRepository.deleteByOrderId(orderId);

            // Then delete the order
            orderRepository.deleteById(orderId);

            return ResponseEntity.ok(Map.of("message", "Order deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete order", "error", e.getMessage()));
        }
    }
}
