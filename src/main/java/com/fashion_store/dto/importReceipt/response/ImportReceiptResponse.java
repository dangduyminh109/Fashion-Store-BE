package com.fashion_store.dto.importReceipt.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImportReceiptResponse {
    Long id;
    String note;
    LocalDateTime importDate;
    Long supplierId;
    String supplierName;
    Boolean isDeleted;
    List<ImportItemResponse> importItemList;
}
