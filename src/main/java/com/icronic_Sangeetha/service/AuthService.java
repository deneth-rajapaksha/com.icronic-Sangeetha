package com.icronic_Sangeetha.service;

import com.icronic_Sangeetha.dto.request.ForgotPasswordRequest;
import com.icronic_Sangeetha.dto.request.LoginUserRequest;
import com.icronic_Sangeetha.dto.request.RefreshTokenRequest;
import com.icronic_Sangeetha.dto.request.RegisterUserRequest;
import com.icronic_Sangeetha.dto.response.AppUserResponse;
import com.icronic_Sangeetha.dto.response.MessegeResponse;
import jakarta.validation.Valid;

public interface AuthService {

    MessegeResponse registerUser(RegisterUserRequest request);

    AppUserResponse loginUser( LoginUserRequest request);

    AppUserResponse refreshAccessToken(RefreshTokenRequest request);

    MessegeResponse forgotPassword( ForgotPasswordRequest request);
}
