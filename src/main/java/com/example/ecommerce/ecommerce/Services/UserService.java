package com.example.ecommerce.ecommerce.Services;

import com.cloudinary.Cloudinary;
import com.example.ecommerce.ecommerce.Dto.customer.CustomerDto;
import com.example.ecommerce.ecommerce.Dto.customer.UpdateCustomerRequest;
import com.example.ecommerce.ecommerce.Entity.Users;
import com.example.ecommerce.ecommerce.Repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.persistence.Table;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired Cloudinary cloudinary;
    @Autowired UserRepository userRepository;

    @Autowired AuthService authService;

    @Autowired JWTService jwtService;

    @Autowired HttpServletRequest request;

    @Autowired PasswordEncoder passwordEncoder;

    @Autowired ModelMapper modelMapper;

    public String extractUsernameFromToken() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtService.extractUserName(token);
        } else {
            return "Token missing";
        }
    }


    public boolean isAdmin() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Claims claim = jwtService.extractAllClaims(token);
            String role = claim.get("role", String.class);
            return "ADMIN".equalsIgnoreCase(role);
        } else {
            return false; // Token missing or invalid format
        }
    }


    public ResponseEntity<?> updatePassword(String oldPassword, String newPassword){
        String username = extractUsernameFromToken();
        Optional<Users> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        Users user = optionalUser.get();

        // verify password
        if (!passwordEncoder.matches(oldPassword,user.getPassword())){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("incorrect old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok("password changed");
    }

    @Transactional
    public ResponseEntity<?> updateProfile(UpdateCustomerRequest dto){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Users> optionalUsers = userRepository.findByUsername(username);
        if (optionalUsers.isEmpty()){
            ResponseEntity.status(HttpStatus.UNAUTHORIZED);
        }
        Users user  =  optionalUsers.get();

        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())){
            if (userRepository.existsByUsername(dto.getUsername())){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("username taken");
            }
            user.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())){
            if (userRepository.existsByEmail(dto.getEmail())){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("user with email already exists");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getAddress() != null){
            user.setAddress(dto.getAddress());
        }

        if (dto.getName() != null){
            user.setName(dto.getName());
        }

        if (dto.getPhone() != null){
            user.setPassword(dto.getEmail());
        }

        Users updatedUser= userRepository.save(user);
        CustomerDto response = modelMapper.map(updatedUser,CustomerDto.class);
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<?> changeProfilePicture(MultipartFile image){

        String username = extractUsernameFromToken();
        Optional<Users> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized request");
        }
        Users user = optionalUser.get();

        if (user.getImage() != null){
            String oldImage = user.getImage();
        }


        if (image != null && !image.isEmpty()){
            try {
                Map<?, ?> result = cloudinary.uploader().upload(image.getBytes(), Map.of());
                user.setImage(result.get("secure_url").toString());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Image upload failed");
            }
            userRepository.save(user);

        }

        return ResponseEntity.status(HttpStatus.OK).body(user.getImage());

    }

    @Transactional
    public ResponseEntity<?>getUser(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Users> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","User not authorized"));
        }
        Users user = optionalUser.get();
        return ResponseEntity.ok(modelMapper.map(user, CustomerDto.class));
    }
}
