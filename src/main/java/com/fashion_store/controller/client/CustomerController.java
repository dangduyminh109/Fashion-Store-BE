package com.fashion_store.controller.client;

import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.dto.customer.request.CustomerUpdateInfoRequest;
import com.fashion_store.dto.customer.response.CustomerResponse;
import com.fashion_store.service.CustomerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RequestMapping("/api/customer")
@RestController("ClientCustomerController")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerController {
    CustomerService customerService;

    @GetMapping("/me")
    public ApiResponse<CustomerResponse> getMyInfo() {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.getMyInfo())
                .build();
    }

    @PutMapping("/update-info")
    public ApiResponse<CustomerResponse> CustomerUpdateInfo(@ModelAttribute @Valid CustomerUpdateInfoRequest request) {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.CustomerUpdateInfo(request))
                .build();
    }
}
