package com.fashion_store.service;

import com.fashion_store.Utils.JwtUtils;
import com.fashion_store.enums.TypeUser;
import com.fashion_store.dto.auth.request.AuthenticationRequest;
import com.fashion_store.dto.auth.request.LogoutRequest;
import com.fashion_store.dto.auth.request.RefreshRequest;
import com.fashion_store.dto.auth.response.AuthenticationResponse;
import com.fashion_store.dto.user.response.UserResponse;
import com.fashion_store.entity.InvalidatedToken;
import com.fashion_store.entity.User;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.mapper.UserMapper;
import com.fashion_store.repository.InvalidatedTokenRepository;
import com.fashion_store.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    UserMapper userMapper;
    JwtUtils jwtUtils;

    public UserResponse login(AuthenticationRequest request) throws JOSEException {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (user.getStatus() == false || user.getIsDeleted() == true) {
            authenticated = false;
        }
        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        var token = jwtUtils.generateToken(user.getUsername(), TypeUser.ADMIN.name());
        UserResponse userResponse = userMapper.toUserResponse(user);
        userResponse.setToken(token);
        return userResponse;
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

        String username = signedJWT.getJWTClaimsSet().getSubject();

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
        if (user.getStatus() == false || user.getIsDeleted() == true) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String newToken = jwtUtils.generateToken(user.getUsername(), TypeUser.ADMIN.name());
        return AuthenticationResponse.builder()
                .token(newToken)
                .build();
    }
}
