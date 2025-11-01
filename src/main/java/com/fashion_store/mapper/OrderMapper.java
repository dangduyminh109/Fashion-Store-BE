package com.fashion_store.mapper;

import com.fashion_store.dto.DashboardSummary.response.OrderDashboardResponse;
import com.fashion_store.dto.order.request.OrderCreateRequest;
import com.fashion_store.dto.order.request.OrderUpdateRequest;
import com.fashion_store.dto.order.response.OrderResponse;
import com.fashion_store.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {OrderMapper.class})
public interface OrderMapper {
    Order toOrder(OrderCreateRequest orderRequest);

    @Mapping(target = "customerId", source = "customer.id")
    OrderResponse toOrderResponse(Order order);

    void updateOrder(@MappingTarget Order order, OrderUpdateRequest orderUpdateRequest);

    @Mapping(target = "numOfProduct", expression = "java(order.getOrderItems().size())")
    OrderDashboardResponse toDashboardOrderResponse(Order order);
}
