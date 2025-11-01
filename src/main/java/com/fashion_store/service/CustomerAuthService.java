package com.fashion_store.service;

import com.fashion_store.Utils.JwtUtils;
import com.fashion_store.dto.auth.request.CustomerAuthRequest;
import com.fashion_store.dto.auth.request.LogoutRequest;
import com.fashion_store.dto.auth.request.RefreshRequest;
import com.fashion_store.dto.customer.request.CustomerForgotPasswordRequest;
import com.fashion_store.dto.customer.request.CustomerRegisterRequest;
import com.fashion_store.dto.customer.request.SendOtpRequest;
import com.fashion_store.enums.TypeUser;
import com.fashion_store.dto.auth.response.AuthenticationResponse;
import com.fashion_store.dto.customer.response.CustomerResponse;
import com.fashion_store.entity.InvalidatedToken;
import com.fashion_store.entity.Customer;
import com.fashion_store.entity.Otp;
import com.fashion_store.enums.AuthProvider;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.mapper.CustomerMapper;
import com.fashion_store.repository.CustomerRepository;
import com.fashion_store.repository.InvalidatedTokenRepository;
import com.fashion_store.repository.OtpRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerAuthService {
    OtpRepository otpRepository;
    CustomerRepository customerRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    CustomerMapper customerMapper;
    PasswordEncoder passwordEncoder;
    EmailService emailService;
    JwtUtils jwtUtils;

    @NonFinal
    @Value("${otp.valid-duration}")
    protected long VALID_DURATION_OTP;

    public AuthenticationResponse login(CustomerAuthRequest request) throws JOSEException {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), customer.getPassword());
        if (customer.getStatus() == false || customer.getIsDeleted() == true) {
            authenticated = false;
        }
        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        var token = jwtUtils.generateToken(customer.getEmail(), TypeUser.CUSTOMER.name());
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        String token = request.getToken();
        SignedJWT signedJWT = jwtUtils.verifyToken(token, true);
        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var newInvalidatedToken = InvalidatedToken.builder()
                .id(jti)
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenRepository.save(newInvalidatedToken);
    }

    public AuthenticationResponse refresh(RefreshRequest request) throws ParseException, JOSEException {
        String token = request.getToken();
        SignedJWT signedJWT = jwtUtils.verifyToken(token, true);
        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var newInvalidatedToken = InvalidatedToken.builder()
                .id(jti)
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenRepository.save(newInvalidatedToken);

        String email = signedJWT.getJWTClaimsSet().getSubject();

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));

        if (customer.getStatus() == false || customer.getIsDeleted() == true) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String newToken = jwtUtils.generateToken(customer.getEmail(), TypeUser.CUSTOMER.name());
        return AuthenticationResponse.builder()
                .token(newToken)
                .build();
    }

    public CustomerResponse register(CustomerRegisterRequest request) throws JOSEException {
        boolean existingCustomer = customerRepository.existsByEmail(request.getEmail());
        if (existingCustomer) {
            throw new AppException(ErrorCode.EXISTED);
        }
        Otp otp = otpRepository.findByEmailAndCode(request.getEmail(), request.getOtp())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_OTP));
        if (otp.getExpiryTime().isBefore(LocalDateTime.now()))
            throw new AppException(ErrorCode.INVALID_OTP);

        Customer customer = customerMapper.toCustomer(request);
        customer.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        customer.setStatus(true);
        customer.setAuthProvider(AuthProvider.GMAIL);
        customerRepository.save(customer);
        var token = jwtUtils.generateToken(customer.getEmail(), TypeUser.CUSTOMER.name());
        CustomerResponse customerResponse = customerMapper.toCustomerResponse(customer);
        customerResponse.setToken(token);
        otpRepository.delete(otp);
        return customerResponse;
    }

    public String forgotPassword(CustomerForgotPasswordRequest request) throws JOSEException {
        Otp otp = otpRepository.findByEmailAndCode(request.getEmail(), request.getOtp())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_OTP));
        if (otp.getExpiryTime().isBefore(LocalDateTime.now()))
            throw new AppException(ErrorCode.INVALID_OTP);
        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new AppException(ErrorCode.INVALID_PASSWORD);

        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));

        customer.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));

        otpRepository.delete(otp);
        return "Đổi mật khẩu thành công";
    }

    public String sendOtp(SendOtpRequest request) throws MessagingException {
        Optional<Otp> existingOtp = otpRepository.findFirstByEmailAndExpiryTimeAfter(
                request.getEmail(), LocalDateTime.now());

        if (existingOtp.isPresent()) {
            return "Email đã được gửi";
        }

        Otp otp = Otp.builder()
                .email(request.getEmail())
                .code(generateOtpCode())
                .expiryTime(LocalDateTime.now().plusMinutes(VALID_DURATION_OTP))
                .build();

        Otp result = otpRepository.save(otp);
        emailService.sendEmail(request.getEmail(), result.getCode());
        return "Email đã được gửi";
    }

    public String generateOtpCode() {
        Random random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
