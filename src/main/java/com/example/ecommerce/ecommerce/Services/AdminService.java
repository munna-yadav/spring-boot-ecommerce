package com.example.ecommerce.ecommerce.Services;

import com.example.ecommerce.ecommerce.Entity.Admin;
import com.example.ecommerce.ecommerce.Entity.Customer;
import com.example.ecommerce.ecommerce.Entity.Users;
import com.example.ecommerce.ecommerce.Enum.Role;
import com.example.ecommerce.ecommerce.Repository.AdminRepository;
import com.example.ecommerce.ecommerce.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    AdminRepository adminRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    CustomerService customerService;

    @Transactional
    public ResponseEntity<?> deleteUser(Long id){

       if (!customerService.isAdmin()){
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only admins can delete users");
       }

        Optional<Users> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user with id not found");
        }
        Users targetUser = optionalUser.get();
        userRepository.delete(targetUser);

        return ResponseEntity.ok("user deleted");
    }


}
