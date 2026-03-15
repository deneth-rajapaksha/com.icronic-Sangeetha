package com.icronic_Sangeetha.controller;

import com.icronic_Sangeetha.dto.request.ForgotPasswordRequest;
import com.icronic_Sangeetha.dto.request.LoginUserRequest;
import com.icronic_Sangeetha.dto.request.RefreshTokenRequest;
import com.icronic_Sangeetha.dto.request.RegisterUserRequest;
import com.icronic_Sangeetha.dto.response.AppUserResponse;
import com.icronic_Sangeetha.dto.response.MessegeResponse;
import com.icronic_Sangeetha.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/registerUser")
    public ResponseEntity<MessegeResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        MessegeResponse response = authService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/loginUser")
    public ResponseEntity<AppUserResponse> loginUser(@Valid @RequestBody LoginUserRequest request) {
        AppUserResponse response = authService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refreshAccessToken") // no usages
    public ResponseEntity<AppUserResponse> refreshAccessToken(@Valid @RequestBody RefreshTokenRequest request) {
        AppUserResponse response = authService.refreshAccessToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgotPassword") // no usages
    public ResponseEntity<MessegeResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        MessegeResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }
}

