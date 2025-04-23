package com.example.ecommerce.ecommerce.Controller;

import com.example.ecommerce.ecommerce.Dto.customer.UpdateCustomerRequest;
import com.example.ecommerce.ecommerce.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired UserService userService;

    @PutMapping("/update-profile")
    public ResponseEntity<?>updateCustomer(@RequestBody UpdateCustomerRequest dto){
        return userService.updateProfile(dto);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String > request){
        String oldPassword = request.getOrDefault("oldPassword","").trim();
        String newPassword = request.getOrDefault("newPassword","").trim();
        return userService.updatePassword(oldPassword,newPassword);
    }

    @PutMapping("/change-image")
    public ResponseEntity<?> updatePicture(@RequestParam(value = "image", required = true)MultipartFile image){
        return userService.changeProfilePicture(image);
    }
}
