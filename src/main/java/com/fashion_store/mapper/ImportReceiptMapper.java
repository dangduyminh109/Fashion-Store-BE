package com.fashion_store.mapper;

import com.fashion_store.dto.importReceipt.request.ImportReceiptRequest;
import com.fashion_store.dto.importReceipt.request.ImportReceiptUpdateRequest;
import com.fashion_store.dto.importReceipt.response.ImportReceiptResponse;
import com.fashion_store.entity.ImportReceipt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {ImportItemMapper.class})
public interface ImportReceiptMapper {
    ImportReceipt toImportReceipt(ImportReceiptRequest importReceiptRequest);

    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    ImportReceiptResponse toImportReceiptResponse(ImportReceipt importReceipt);

    void updateImportReceipt(@MappingTarget ImportReceipt importReceipt, ImportReceiptUpdateRequest importReceiptUpdateRequest);
}
