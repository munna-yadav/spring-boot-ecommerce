package com.example.ecommerce.ecommerce.Services;


import com.cloudinary.Cloudinary;
import com.example.ecommerce.ecommerce.Dto.customer.CustomerDto;
import com.example.ecommerce.ecommerce.Dto.auth.LoginRequestDto;
import com.example.ecommerce.ecommerce.Dto.auth.LoginResponseDto;
import com.example.ecommerce.ecommerce.Entity.Admin;
import com.example.ecommerce.ecommerce.Entity.Customer;
import com.example.ecommerce.ecommerce.Enum.Role;
import com.example.ecommerce.ecommerce.Repository.AdminRepository;
import com.example.ecommerce.ecommerce.Repository.CustomerRepository;
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

    @Autowired CustomerRepository customerRepository;
    @Autowired
    AdminRepository adminRepository;

    @Autowired PasswordEncoder passwordEncoder;

    @Autowired Cloudinary cloudinary;

    @Autowired JWTService jwtService;

    @Autowired LoginResponseDto responseDto;


    public ResponseEntity<?> registerCustomer(Customer customer, MultipartFile imageFile){

        if (customerRepository.existsByUsername(customer.getUsername())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("user with the username exist");
        }

        if (customerRepository.existsByEmail(customer.getEmail())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User with email already Registered");
        }
        customer.setUsername(customer.getUsername().toLowerCase(Locale.ROOT));
        customer.setRole(Role.CUSTOMER);
        // upload image to cloudinary if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Map<?, ?> result = cloudinary.uploader().upload(imageFile.getBytes(), Map.of());
                customer.setProfileImageUrl(result.get("secure_url").toString());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Image upload failed");
            }
        }
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        Customer saved = customerRepository.save(customer);
        CustomerDto dto = modelMapper.map(saved,CustomerDto.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);

    }

    public ResponseEntity<?> loginCustomer(LoginRequestDto dto) {

        if (dto.getEmailOrUsername() == null || dto.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username/email and password must not be null");
        }
        String identifier = dto.getEmailOrUsername().trim();
        String password = dto.getPassword();


        Optional<Customer> optionalCustomer = customerRepository.findByEmailOrUsername(identifier, identifier);
        if (optionalCustomer.isEmpty()) {
            return ResponseEntity.status(404).body("Invalid username or email");
        }

        Customer customer = optionalCustomer.get();

        // ✅ FIXED password check
        if (!passwordEncoder.matches(password, customer.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }

        // ✅ Generate JWT token
        String token = jwtService.generateToken(customer.getUsername(),customer.getRole());

        // ✅ Prepare response DTO
        LoginResponseDto responseDto = new LoginResponseDto();
        responseDto.setMessage("User logged in");
        responseDto.setToken(token);
        return ResponseEntity.status(200).body(responseDto);
    }


    public ResponseEntity<?> registerAdmin(Admin admin){
        if (adminRepository.existsByUsername(admin.getUsername())){
            return  ResponseEntity.status(HttpStatus.CONFLICT).body("user with username exists");
        }
        if (adminRepository.existsByEmail(admin.getEmail())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User with email already exists");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        Admin createdAdmin = adminRepository.save(admin);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message","user registered successfully","username", createdAdmin.getUsername()));
    }

    public ResponseEntity<?> loginAdmin(LoginRequestDto dto){

        if (dto.getEmailOrUsername() == null || dto.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username/email and password must not be null");
        }
//
//        if (!adminRepository.existsByEmail(dto.getEmailOrUsername())){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email  not found");
//        }
//        if (!adminRepository.existsByUsername(dto.getEmailOrUsername())){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with username not found");
//        }

        String password = dto.getPassword();
        String identifier = dto.getEmailOrUsername().trim();

        Optional<Admin> optionalAdmin = adminRepository.findByEmailOrUsername(identifier,identifier);
        if (optionalAdmin.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email of username not found");
        }
        Admin admin = optionalAdmin.get();

        if (!passwordEncoder.matches(password,admin.getPassword())){
            return ResponseEntity.status(401).body("invalid password");

        }

        String token = jwtService.generateToken(admin.getUsername(),admin.getRole());

        return ResponseEntity.status(200).body(Map.of("message","logged in","token",token));

    }
}



