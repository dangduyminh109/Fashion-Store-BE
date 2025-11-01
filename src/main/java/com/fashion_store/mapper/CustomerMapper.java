package com.fashion_store.mapper;

import com.fashion_store.dto.DashboardSummary.response.CustomerDashboardResponse;
import com.fashion_store.dto.customer.request.CustomerCreateRequest;
import com.fashion_store.dto.customer.request.CustomerRegisterRequest;
import com.fashion_store.dto.customer.request.CustomerUpdateInfoRequest;
import com.fashion_store.dto.customer.request.CustomerUpdateRequest;
import com.fashion_store.dto.customer.response.CustomerResponse;
import com.fashion_store.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {AddressMapper.class})
public interface CustomerMapper {

    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "password", ignore = true)
    Customer toCustomer(CustomerCreateRequest customerCreateRequest);

    @Mapping(target = "password", ignore = true)
    Customer toCustomer(CustomerRegisterRequest customerRegisterRequest);

    CustomerResponse toCustomerResponse(Customer customer);

    CustomerDashboardResponse toDashboardCustomerResponse(Customer customer);

    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateCustomer(@MappingTarget Customer customer, CustomerUpdateRequest customerUpdateRequest);

    @Mapping(target = "avatar", ignore = true)
    void customerUpdateInfo(@MappingTarget Customer customer, CustomerUpdateInfoRequest customerUpdateInfoRequest);
}
