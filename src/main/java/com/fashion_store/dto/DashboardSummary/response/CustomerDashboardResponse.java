package com.fashion_store.dto.DashboardSummary.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerDashboardResponse {
    String fullName;
    Date createdAt;
    String avatar;
}
