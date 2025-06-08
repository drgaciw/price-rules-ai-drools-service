package com.example.pricerulesaidrools.security.controller;

import com.example.pricerulesaidrools.security.dto.JwtResponse;
import com.example.pricerulesaidrools.security.dto.LoginRequest;
import com.example.pricerulesaidrools.security.dto.MessageResponse;
import com.example.pricerulesaidrools.security.dto.SignupRequest;
import com.example.pricerulesaidrools.security.jwt.JwtUtils;
import com.example.pricerulesaidrools.security.model.Role;
import com.example.pricerulesaidrools.security.model.User;
import com.example.pricerulesaidrools.security.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User userDetails = (User) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .build();

        Set<Role> roles = new HashSet<>();
        
        // By default, give ROLE_MONITOR (readonly) access to new users
        roles.add(Role.ROLE_MONITOR);
        
        // If admin flag is specified and valid, add admin role
        if (signUpRequest.getRoles() != null && signUpRequest.getRoles().contains("admin")) {
            roles.add(Role.ROLE_ADMIN);
        }
        
        if (signUpRequest.getRoles() != null && signUpRequest.getRoles().contains("rule_manager")) {
            roles.add(Role.ROLE_RULE_MANAGER);
        }
        
        if (signUpRequest.getRoles() != null && signUpRequest.getRoles().contains("rule_executor")) {
            roles.add(Role.ROLE_RULE_EXECUTOR);
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}