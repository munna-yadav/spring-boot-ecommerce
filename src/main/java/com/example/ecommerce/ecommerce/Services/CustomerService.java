package com.example.ecommerce.ecommerce.Services;

import com.cloudinary.Cloudinary;
import com.example.ecommerce.ecommerce.Dto.customer.CustomerDto;
import com.example.ecommerce.ecommerce.Dto.customer.UpdateCustomerRequest;
import com.example.ecommerce.ecommerce.Entity.Customer;
import com.example.ecommerce.ecommerce.Repository.CustomerRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomerService {
    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    HttpServletRequest request;

    @Autowired JWTService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    Cloudinary cloudinary;

    public String extractUsernameFromToken(){
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        String username = jwtService.extractUserName(token);
        return username;
    }
    public boolean isAdmin(){
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        Claims claims = jwtService.extractAllClaims(token);
        String role = claims.get("role", String.class);
        return "ADMIN".equalsIgnoreCase(role);
    }

    public ResponseEntity<?> updatePassword(String oldPassword, String newPassword){
        String username = extractUsernameFromToken();
        Optional<Customer> optionalCustomer = customerRepository.findByUsername(username);
        if (optionalCustomer.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found");
        }
        Customer customer = optionalCustomer.get();

        // verify password
        if (!passwordEncoder.matches(oldPassword,customer.getPassword())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("old password is incorrect");
        }
        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);
        return ResponseEntity.ok("Password changed");


    }

    public ResponseEntity<?> updateProfile(UpdateCustomerRequest dto){
        String username = extractUsernameFromToken();
        Optional<Customer> optionalCustomer = customerRepository.findByUsername(username);
        if (optionalCustomer.isEmpty()){
            ResponseEntity.status(HttpStatus.UNAUTHORIZED);
        }
        Customer customer  =  optionalCustomer.get();

        if (dto.getUsername() != null && !dto.getUsername().equals(customer.getUsername())){
            if (customerRepository.existsByUsername(dto.getUsername())){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("username taken");
            }
            customer.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(customer.getEmail())){
            if (customerRepository.existsByEmail(dto.getEmail())){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("user with email already exists");
            }
            customer.setEmail(dto.getEmail());
        }

        if (dto.getAddress() != null){
            customer.setAddress(dto.getAddress());
        }

        if (dto.getName() != null){
            customer.setName(dto.getName());
        }

        if (dto.getPhone() != null){
            customer.setPassword(dto.getEmail());
        }

        Customer updatedCustomer= customerRepository.save(customer);
        CustomerDto response = modelMapper.map(updatedCustomer,CustomerDto.class);
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<?> changeProfilePicture(MultipartFile image){

        String username = extractUsernameFromToken();
        Optional<Customer> optionalCustomer = customerRepository.findByUsername(username);
        if (optionalCustomer.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized request");
        }
        Customer customer = optionalCustomer.get();

        if (customer.getProfileImageUrl() != null){
            String oldImage = customer.getProfileImageUrl();
        }


        if (image != null && !image.isEmpty()){
            try {
                Map<?, ?> result = cloudinary.uploader().upload(image.getBytes(), Map.of());
                customer.setProfileImageUrl(result.get("secure_url").toString());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Image upload failed");
            }
            customerRepository.save(customer);

        }

        return ResponseEntity.status(HttpStatus.OK).body(customer.getProfileImageUrl());

    }
}
