package com.example.ecommerce.ecommerce.Controller;

import com.example.ecommerce.ecommerce.Dto.auth.LoginRequestDto;
import com.example.ecommerce.ecommerce.Entity.Users;
import com.example.ecommerce.ecommerce.Services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(
            @ModelAttribute Users user,
            @RequestParam(value = "image",required = false )MultipartFile imageFile){

        return authService.registerUser(user, imageFile);
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@Validated @RequestBody LoginRequestDto loginRequestDto){
        return authService.login(loginRequestDto);
    }


}
