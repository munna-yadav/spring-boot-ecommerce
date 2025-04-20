package com.example.ecommerce.ecommerce.Controller;

import com.example.ecommerce.ecommerce.Entity.Admin;
import com.example.ecommerce.ecommerce.Services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")

public class AdminController {

    @Autowired
    AdminService adminService;


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("delete-user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        System.out.println("hello admin");
        return adminService.deleteUser(id);
    }

}
