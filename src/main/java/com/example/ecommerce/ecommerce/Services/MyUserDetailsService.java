package com.example.ecommerce.ecommerce.Services;


import com.example.ecommerce.ecommerce.Entity.UserPrincipal;
import com.example.ecommerce.ecommerce.Entity.Users;
import com.example.ecommerce.ecommerce.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
//        System.out.println(userName); debugging
        Optional<Users> user = userRepository.findByUsername(userName);
        if (user.isEmpty()) {
            System.out.println("User Not Found");
            throw new UsernameNotFoundException("user not found");
        }


        return new UserPrincipal(user);
    }
}