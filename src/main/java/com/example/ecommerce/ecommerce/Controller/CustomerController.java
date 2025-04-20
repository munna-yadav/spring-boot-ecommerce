package com.example.ecommerce.ecommerce.Controller;

import com.example.ecommerce.ecommerce.Dto.customer.UpdateCustomerRequest;
import com.example.ecommerce.ecommerce.Services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer")
public class CustomerController {

    @Autowired
    CustomerService customerService;


    @PutMapping("/update-profile")
    public ResponseEntity<?> updateCustomer(@RequestBody UpdateCustomerRequest updatedCustomer){

        return customerService.updateProfile(updatedCustomer);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map< String, String> request){

        String oldPassword = request.getOrDefault("oldPassword","").trim();
        String newPassword = request.getOrDefault("newPassword","").trim();

        return customerService.updatePassword(oldPassword,newPassword);
    }

    @PutMapping("/change-image")
    public ResponseEntity<?> changeImage(
            @RequestParam (value = "image", required = true)MultipartFile imageFile
    ){
        return customerService.changeProfilePicture(imageFile);
    }
}
