package com.example.ecommerce.ecommerce.Controller;

import com.example.ecommerce.ecommerce.Dto.auth.LoginRequestDto;
import com.example.ecommerce.ecommerce.Entity.Admin;
import com.example.ecommerce.ecommerce.Entity.Customer;
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
            @ModelAttribute Customer customer,
            @RequestParam(value = "image",required = false )MultipartFile imageFile){

        return authService.registerCustomer(customer, imageFile);
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody Admin admin){
        System.out.println("welcome to admin page");
        return authService.registerAdmin(admin);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@Validated @RequestBody LoginRequestDto loginRequestDto){
        return authService.loginCustomer(loginRequestDto);
    }

    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@Validated @RequestBody  LoginRequestDto dto){
        System.out.println("Admin login"+ dto);
        return authService.loginAdmin(dto);
    }
}
