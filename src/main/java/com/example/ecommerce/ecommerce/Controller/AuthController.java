package com.example.ecommerce.ecommerce.Controller;

import com.example.ecommerce.ecommerce.Dto.auth.LoginRequestDto;
import com.example.ecommerce.ecommerce.Entity.Users;
import com.example.ecommerce.ecommerce.Services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.GetExchange;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(
            @RequestBody Users user){

        return authService.registerUser(user);
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@Validated @RequestBody LoginRequestDto loginRequestDto){
        return authService.login(loginRequestDto);
    }


}
