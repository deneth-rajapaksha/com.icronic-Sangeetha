package com.icronic_Sangeetha.service;

import com.icronic_Sangeetha.dto.request.AppUserRequests;
import com.icronic_Sangeetha.dto.response.AppUserResponse;
import com.icronic_Sangeetha.dto.response.PaginatedResponse;

public interface AppUserService {
    AppUserResponse getUserProfile(String email);

    AppUserResponse updateUserProfile(AppUserRequests request, String email);

    PaginatedResponse<AppUserResponse> getAllUsers(int page, int size);

    AppUserResponse updateUserRole(Long userId, String role, String adminEmail);
}
