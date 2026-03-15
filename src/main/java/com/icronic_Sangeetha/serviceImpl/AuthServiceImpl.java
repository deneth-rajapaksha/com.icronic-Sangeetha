package com.icronic_Sangeetha.serviceImpl;

import com.icronic_Sangeetha.dto.request.ForgotPasswordRequest;
import com.icronic_Sangeetha.dto.request.LoginUserRequest;
import com.icronic_Sangeetha.dto.request.RefreshTokenRequest;
import com.icronic_Sangeetha.dto.request.RegisterUserRequest;
import com.icronic_Sangeetha.dto.response.AppUserResponse;
import com.icronic_Sangeetha.dto.response.MessegeResponse;
import com.icronic_Sangeetha.entity.AppUser;
import com.icronic_Sangeetha.exception.*;
import com.icronic_Sangeetha.repository.AppUserRepository;
import com.icronic_Sangeetha.service.AuthService;
import com.icronic_Sangeetha.service.EmailService;
import com.icronic_Sangeetha.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public MessegeResponse registerUser(RegisterUserRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException("Email Already Exists");

        }

        String tempPassword = generateTemporaryPassword();
        AppUser appUser = new AppUser();
        appUser.setName(request.getName());
        appUser.setEmail(request.getEmail());
        appUser.setPassword(passwordEncoder.encode(tempPassword));
        appUser.setRole(request.getRole() != null ? request.getRole() : "USER");

        appUserRepository.save(appUser);
        emailService.sendWelcomeEmail(appUser.getEmail(), appUser.getName(), tempPassword);

        return new MessegeResponse("Account created successfully. A temporary password has been sent to your email");

    }

    @Override
    public AppUserResponse loginUser(LoginUserRequest request) {
        AppUser appUser = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), appUser.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(
                appUser.getId(),
                appUser.getName(),
                appUser.getEmail(),
                appUser.getRole()
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                appUser.getId(),
                appUser.getEmail()
        );

        appUser.setRefreshToken(refreshToken);
        appUserRepository.save(appUser);

        return AppUserResponse.fromEntity(appUser, accessToken, refreshToken);
    }

    @Override
    public AppUserResponse refreshAccessToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        String email = jwtUtil.extractEmail(refreshToken);

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid token type");
        }

        AppUser appUser = appUserRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!jwtUtil.validateToken(refreshToken, email)) {
            throw new TokenExpiredException("Refresh token expired or invalid");
        }

        String newAccessToken = jwtUtil.generateAccessToken(
                appUser.getId(),
                appUser.getName(),
                appUser.getEmail(),
                appUser.getRole()
        );
        return  AppUserResponse.fromEntity(appUser,newAccessToken, refreshToken);

    }

    @Override
    public MessegeResponse forgotPassword(ForgotPasswordRequest request) {
        AppUser appUser = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException ("User not found with email: " + request.getEmail()));
        String tempPassword = generateTemporaryPassword();

        appUser.setPassword(passwordEncoder.encode(tempPassword));
        appUserRepository.save(appUser);

        emailService.sendCredentialsEmail(appUser.getEmail(), appUser.getName(), tempPassword);

        return new MessegeResponse("Temporary password has been sent to your email");
    }

    private String generateTemporaryPassword() {
        String chars = "AQZXSWEDCVFRTGBNHYUJMKIOLP!@#$%^&*()1234567890qazwsxedcrfvtgbyhnujmikolp][{}";
        SecureRandom  random = new SecureRandom();
        StringBuilder password = new StringBuilder(10);

        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();

    }
}
