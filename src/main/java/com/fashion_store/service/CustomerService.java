package com.fashion_store.service;

import com.fashion_store.Utils.SecurityUtils;
import com.fashion_store.dto.customer.request.CustomerCreateRequest;
import com.fashion_store.dto.customer.request.CustomerUpdateRequest;
import com.fashion_store.dto.customer.response.CustomerResponse;
import com.fashion_store.entity.Customer;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.mapper.CustomerMapper;
import com.fashion_store.repository.CustomerRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerService extends GenerateService<Customer, String> {
    CustomerRepository customerRepository;
    CloudinaryService cloudinaryService;
    CustomerMapper customerMapper;
    PasswordEncoder passwordEncoder;

    @Override
    JpaRepository<Customer, String> getRepository() {
        return customerRepository;
    }

    public CustomerResponse create(CustomerCreateRequest request) {
        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EXISTED);
        if (request.getPhone() != null && customerRepository.existsByPhone(request.getPhone()))
            throw new AppException(ErrorCode.EXISTED);
        request.setAuthProvider(request.getAuthProvider().toUpperCase().trim());
        Customer customer = customerMapper.toCustomer(request);
        customer.setPassword(passwordEncoder.encode(request.getPassword().trim()));

        // handle avatar
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            try {
                String avatarUrl = cloudinaryService.uploadFile(request.getAvatar());
                customer.setAvatar(avatarUrl);

            } catch (IOException e) {
                throw new AppException(ErrorCode.FILE_SAVE_FAILED);
            }
        } else {
            customer.setAvatar("");
        }
        customer = customerRepository.save(customer);
        return customerMapper.toCustomerResponse(customer);
    }

    public List<CustomerResponse> getAll(boolean deleted, String name) {
        return customerRepository.findAll()
                .stream()
                .filter(item -> {
                    if (name != null) {
                        return item.getFullName().trim().toLowerCase().contains(name.trim().toLowerCase()) && item.getIsDeleted() == deleted;
                    }
                    return item.getIsDeleted() == deleted;
                })
                .map(customerMapper::toCustomerResponse)
                .collect(Collectors.toList());
    }

    public CustomerResponse getMyInfo() {
        String customerId = SecurityUtils.getCurrentUserId();
        if (customerId == null)
            throw new AppException(ErrorCode.UNAUTHORIZED);
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        return customerMapper.toCustomerResponse(customer);
    }

    public CustomerResponse getInfo(String id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        return customerMapper.toCustomerResponse(customer);
    }

    public void status(String id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        try {
            customer.setStatus(customer.getStatus() == null || !customer.getStatus());
            customerRepository.save(customer);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }

    public CustomerResponse update(CustomerUpdateRequest request, String id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (request.getEmail() != null && customerRepository.existsByEmailAndIdNot(request.getEmail(), id))
            throw new AppException(ErrorCode.EXISTED);
        if (request.getPhone() != null && customerRepository.existsByPhoneAndIdNot(request.getPhone(), id))
            throw new AppException(ErrorCode.EXISTED);

        if (request.getAuthProvider() != null) {
            request.setAuthProvider(request.getAuthProvider().toUpperCase().trim());
        }
        customerMapper.updateCustomer(customer, request);

        if (request.getNewPassword() != null) {
            customer.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        }
        // handle avatar
        boolean avatarDelete = request.getAvatarDelete() != null && request.getAvatarDelete();
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            try {
                String avatarUrl = cloudinaryService.uploadFile(request.getAvatar());
                // Lưu URL vào DB
                customer.setAvatar(avatarUrl);
            } catch (IOException e) {
                throw new AppException(ErrorCode.FILE_SAVE_FAILED);
            }
        } else if (avatarDelete) {
            customer.setAvatar("");
        }

        customer = customerRepository.save(customer);
        return customerMapper.toCustomerResponse(customer);
    }

    public void destroy(String id) {
        Customer item = customerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (item.getOrders() == null || item.getOrders().isEmpty()) {
            try {
                customerRepository.delete(item);
            } catch (Exception e) {
                throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
            }
        } else {
            throw new AppException(ErrorCode.CUSTOMER_HAS_ORDERS);
        }
    }
}
