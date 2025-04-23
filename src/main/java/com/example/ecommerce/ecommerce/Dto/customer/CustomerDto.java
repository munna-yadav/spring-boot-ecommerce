package com.example.ecommerce.ecommerce.Dto.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String role;
    private String profileImageUrl;
    private LocalDateTime createdAt;



}
