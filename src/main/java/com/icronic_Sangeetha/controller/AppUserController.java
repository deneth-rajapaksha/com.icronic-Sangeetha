package com.icronic_Sangeetha.controller;

import com.icronic_Sangeetha.dto.request.AppUserRequests;
import com.icronic_Sangeetha.dto.response.AppUserResponse;
import com.icronic_Sangeetha.dto.response.PaginatedResponse;
import com.icronic_Sangeetha.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/app-user") // Base path for user-related operations
public class AppUserController {

    @Autowired
    private AppUserService appUserService;

    @GetMapping("/get-user-profile")
    public ResponseEntity<AppUserResponse> getUserProfile(Authentication authentication) {
        String email = authentication.getName(); // Extract email from the JWT token
        AppUserResponse response = appUserService.getUserProfile(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-user-profile")
    public ResponseEntity<AppUserResponse> updateUserProfile(
            @Valid @RequestBody AppUserRequests request,
            Authentication authentication) {
        String email = authentication.getName();
        AppUserResponse response = appUserService.updateUserProfile(request, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-all-users")
    @PreAuthorize("hasRole('ADMIN')") // Restricts access to Admin only
    public ResponseEntity<PaginatedResponse<AppUserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(appUserService.getAllUsers(page, size));
    }

    @PatchMapping("/update-user-role/{userId}")
    public ResponseEntity<AppUserResponse> updateUserRole(
            @PathVariable Long userId,
            @RequestParam String role,
            Authentication authentication) {
        String adminEmail = authentication.getName();
        AppUserResponse response = appUserService.updateUserRole(userId, role, adminEmail);
        return ResponseEntity.ok(response);
    }
}
