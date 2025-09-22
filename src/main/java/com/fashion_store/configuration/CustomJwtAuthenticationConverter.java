package com.fashion_store.configuration;

import com.fashion_store.enums.TypeUser;
import com.fashion_store.entity.Customer;
import com.fashion_store.entity.User;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.repository.CustomerRepository;
import com.fashion_store.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String username = jwt.getSubject();
        if (username == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        String type = jwt.getClaim("type");
        if (type.equals(TypeUser.ADMIN.name())) {
            Optional<User> userOpt = userRepository.findByUsernameFetchPermissions(username);
            if (userOpt.isPresent()) {
                if (userOpt.get().getRole().getIsDeleted() || !userOpt.get().getRole().getStatus()) {
                    throw new AppException(ErrorCode.UNAUTHORIZED);
                }
                User user = userOpt.get();
                if (!user.getStatus() || Boolean.TRUE.equals(user.getIsDeleted())) {
                    throw new AppException(ErrorCode.UNAUTHORIZED);
                }
                Set<GrantedAuthority> authorities = user.getRole().getPermissions()
                        .stream()
                        .map(p -> new SimpleGrantedAuthority(p.getCode()))
                        .collect(Collectors.toSet());
                return new UsernamePasswordAuthenticationToken(user.getId(), jwt, authorities);
            }
            throw new AppException(ErrorCode.UNAUTHORIZED);
        } else if (type.equals(TypeUser.CUSTOMER.name())) {
            Optional<Customer> customerOpt = customerRepository.findByEmail(username);
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                return new UsernamePasswordAuthenticationToken(customer.getId(), jwt, Collections.emptyList());
            }
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        throw new AppException(ErrorCode.INVALID_TOKEN);
    }
}
