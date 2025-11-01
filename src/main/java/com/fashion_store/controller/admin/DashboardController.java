package com.fashion_store.controller.admin;

import com.fashion_store.dto.DashboardSummary.response.DashboardSummaryResponse;
import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.service.DashboardService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/dashboard")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardController {
    DashboardService dashboardService;

    @GetMapping
    public ApiResponse<DashboardSummaryResponse> getData() {
        return ApiResponse.<DashboardSummaryResponse>builder()
                .result(dashboardService.getData())
                .build();
    }
}
