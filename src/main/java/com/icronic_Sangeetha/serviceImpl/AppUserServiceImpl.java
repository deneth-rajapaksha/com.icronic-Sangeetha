package com.icronic_Sangeetha.serviceImpl;

import com.icronic_Sangeetha.dto.request.AppUserRequests;
import com.icronic_Sangeetha.dto.response.AppUserResponse;
import com.icronic_Sangeetha.dto.response.PaginatedResponse;
import com.icronic_Sangeetha.entity.AppUser;
import com.icronic_Sangeetha.repository.AppUserRepository;
import com.icronic_Sangeetha.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service // Marks this for IOC management
public class AppUserServiceImpl implements AppUserService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AppUserResponse getUserProfile(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return AppUserResponse.fromEntity(user, null, null); // Conversion from Entity to DTO
    }

    @Override
    public AppUserResponse updateUserProfile(AppUserRequests request, String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        // Logic for updating password
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
                throw new RuntimeException("Old password is required to update password");
            }
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new RuntimeException("Old password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        AppUser updatedUser = appUserRepository.save(user);
        return AppUserResponse.fromEntity(updatedUser, null, null);
    }

    @Override
    public PaginatedResponse<AppUserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppUser> userPage = appUserRepository.findAll(pageable);

        List<AppUserResponse> responses = userPage.getContent().stream()
                .map(user -> AppUserResponse.fromEntity(user, null, null))
                .collect(Collectors.toList());

        return new PaginatedResponse<>(responses, userPage.getNumber(), userPage.getSize(),
                userPage.getTotalElements(), userPage.getTotalPages(), userPage.isLast(), userPage.isFirst());
    }

    @Autowired
    private com.icronic_Sangeetha.util.JwtUtil jwtUtil;

    @Override
    public AppUserResponse updateUserRole(Long userId, String role, String adminEmail) {
        AppUser caller = appUserRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Caller user not found"));

        AppUser userToUpdate = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User to update not found"));

        // Allow if caller is ADMIN OR if caller is updating their own role
        if (!"ADMIN".equals(caller.getRole()) && !caller.getId().equals(userToUpdate.getId())) {
            throw new RuntimeException("You don't have permission to update this user's role");
        }

        userToUpdate.setRole(role.trim().toUpperCase());
        AppUser updatedUser = appUserRepository.save(userToUpdate);

        // Generate new token if the user updated themselves
        String newToken = null;
        if (caller.getId().equals(userToUpdate.getId())) {
            newToken = jwtUtil.generateAccessToken(
                    updatedUser.getId(),
                    updatedUser.getName(),
                    updatedUser.getEmail(),
                    updatedUser.getRole()
            );
        }

        return AppUserResponse.fromEntity(updatedUser, newToken, null);
    }
}
