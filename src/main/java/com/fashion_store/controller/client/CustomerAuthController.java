package com.fashion_store.controller.client;

import com.fashion_store.dto.auth.request.CustomerAuthRequest;
import com.fashion_store.dto.auth.request.LogoutRequest;
import com.fashion_store.dto.auth.request.RefreshRequest;
import com.fashion_store.dto.customer.request.CustomerForgotPasswordRequest;
import com.fashion_store.dto.customer.request.CustomerRegisterRequest;
import com.fashion_store.dto.customer.request.SendOtpRequest;
import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.dto.auth.response.AuthenticationResponse;
import com.fashion_store.dto.customer.response.CustomerResponse;
import com.fashion_store.service.CustomerAuthService;
import com.nimbusds.jose.JOSEException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerAuthController {
    CustomerAuthService customerAuthService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody @Valid CustomerAuthRequest authenticationRequest) throws JOSEException {
        AuthenticationResponse authenticationResponse = customerAuthService.login(authenticationRequest);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationResponse)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        customerAuthService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) throws ParseException, JOSEException {
        AuthenticationResponse result = customerAuthService.refresh(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/register")
    ApiResponse<CustomerResponse> register(@RequestBody CustomerRegisterRequest request) throws JOSEException {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerAuthService.register(request))
                .build();
    }

    @PostMapping("/forgot-password")
    ApiResponse<String> forgotPassword(@RequestBody CustomerForgotPasswordRequest request) throws JOSEException {
        return ApiResponse.<String>builder()
                .result(customerAuthService.forgotPassword(request))
                .build();
    }

    @PostMapping("/send-otp")
    ApiResponse<Void> sendOtp(@RequestBody SendOtpRequest request) throws MessagingException {
        return ApiResponse.<Void>builder()
                .message(customerAuthService.sendOtp(request))
                .build();
    }
}
