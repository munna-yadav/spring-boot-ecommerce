package com.example.ecommerce.ecommerce.Services;


import com.cloudinary.Cloudinary;
import com.example.ecommerce.ecommerce.Dto.customer.CustomerDto;
import com.example.ecommerce.ecommerce.Dto.auth.LoginRequestDto;
import com.example.ecommerce.ecommerce.Dto.auth.LoginResponseDto;
import com.example.ecommerce.ecommerce.Entity.Users;
import com.example.ecommerce.ecommerce.Enum.Role;
import com.example.ecommerce.ecommerce.Repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

   @Autowired ModelMapper modelMapper;

    @Autowired
    UserRepository userRepository;


    @Autowired PasswordEncoder passwordEncoder;

    @Autowired Cloudinary cloudinary;

    @Autowired JWTService jwtService;

    @Autowired LoginResponseDto responseDto;


    public ResponseEntity<?> registerUser(Users user){

        if (user.getUsername() == null || user.getEmail() == null  || user.getName() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","All fields required"));
        }

        if (userRepository.existsByUsername(user.getUsername())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","user with the username exist"));
        }

        if (userRepository.existsByEmail(user.getEmail())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","User with email already Registered"));
        }
        user.setUsername(user.getUsername().toLowerCase());
        user.setRole(user.getRole());
        // upload image to cloudinary if provided
//        if (imageFile != null && !imageFile.isEmpty()) {
//            try {
//                Map<?, ?> result = cloudinary.uploader().upload(imageFile.getBytes(), Map.of());
//                user.setImage(result.get("secure_url").toString());
//            } catch (IOException e) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body("Image upload failed");
//            }
//        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Users saved = userRepository.save(user);
        CustomerDto dto = modelMapper.map(saved,CustomerDto.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);

    }

    public ResponseEntity<?> login(LoginRequestDto dto) {
        System.out.println(dto);

        if (dto.getEmailOrUsername() == null || dto.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("message","All fields required"));
        }
        String identifier = dto.getEmailOrUsername().trim();
        String password = dto.getPassword();


        Optional<Users> optionalUser = userRepository.findByEmailOrUsername(identifier, identifier);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message","Invalid username or email"));
        }

        Users user = optionalUser.get();

        // ✅ FIXED password check
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Invalid password"));
        }

        // ✅ Generate JWT token
        String token = jwtService.generateToken(user.getUsername(),user.getRole());

        // ✅ Prepare response DTO
        responseDto.setMessage("User logged in");
        responseDto.setToken(token);
        return ResponseEntity.status(200).body(responseDto);
    }

}



