package com.example.ecommerce.ecommerce.Dto.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {

    private String username;

    private String name;

    private String email;

    private String password;

    private String phone;

    private String address;

    // No image field here — handled separately using MultipartFile
}
