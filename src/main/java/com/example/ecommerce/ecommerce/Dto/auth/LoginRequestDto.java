package com.example.ecommerce.ecommerce.Dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {
    private String emailOrUsername;
    private String password;
}
