package com.fashion_store.service;

import com.fashion_store.dto.DashboardSummary.response.CustomerDashboardResponse;
import com.fashion_store.dto.DashboardSummary.response.DashboardSummaryResponse;
import com.fashion_store.dto.DashboardSummary.response.OrderDashboardResponse;
import com.fashion_store.mapper.CustomerMapper;
import com.fashion_store.mapper.OrderMapper;
import com.fashion_store.repository.CustomerRepository;
import com.fashion_store.repository.OrderRepository;
import com.fashion_store.repository.PostRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardService {
    OrderRepository orderRepository;
    PostRepository postRepository;
    CustomerRepository customerRepository;
    CustomerMapper customerMapper;
    OrderMapper orderMapper;

    public DashboardSummaryResponse getData() {
        LocalDate now = LocalDate.now();

        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        LocalDate previousMonthDate = now.minusMonths(1);
        int previousMonth = previousMonthDate.getMonthValue();
        int previousYear = previousMonthDate.getYear();

        Integer totalOrder = orderRepository.countOrdersByYearAndMonth(currentYear, currentMonth);
        Integer totalOrderPrev = orderRepository.countOrdersByYearAndMonth(previousYear, previousMonth);

        Integer totalCustomer = customerRepository.countCustomersByYearAndMonth(currentYear, currentMonth);
        Integer totalCustomerPrev = customerRepository.countCustomersByYearAndMonth(previousYear, previousMonth);

        Integer totalPost = postRepository.countPostsByYearAndMonth(currentYear, currentMonth);
        Integer totalPostPrev = postRepository.countPostsByYearAndMonth(previousYear, previousMonth);

        BigDecimal totalRevenue = orderRepository.getTotalRevenue(currentYear, currentMonth);
        BigDecimal totalRevenuePrev = orderRepository.getTotalRevenue(previousYear, previousMonth);


        List<Object[]> rawMonthlyRevenue = orderRepository.getMonthlyRevenue(currentYear);
        List<Object[]> rawMonthlyRevenuePrev = orderRepository.getMonthlyRevenue(previousYear);

        List<BigDecimal> monthlyRevenue = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));
        List<BigDecimal> monthlyRevenuePrev = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));

        for (Object[] row : rawMonthlyRevenue) {
            int month = ((Number) row[0]).intValue();
            BigDecimal revenue = (BigDecimal) row[1];
            monthlyRevenue.set(month, revenue);
        }

        for (Object[] row : rawMonthlyRevenuePrev) {
            int month = ((Number) row[0]).intValue();
            BigDecimal revenue = (BigDecimal) row[1];
            monthlyRevenuePrev.set(month - 1, revenue);
        }

        List<CustomerDashboardResponse> customerList = customerRepository
                .findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(customerMapper::toDashboardCustomerResponse)
                .toList();

        List<OrderDashboardResponse> orderList = orderRepository
                .findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(orderMapper::toDashboardOrderResponse)
                .toList();

        List<Object[]> results = orderRepository.findTopCategoriesWithOrderCount();

        Map<String, Long> topCategories = results.stream()
                .limit(10)
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (Long) r[1]
                ));
        return DashboardSummaryResponse.builder()
                .totalOrder(totalOrder)
                .totalOrderPrev(totalOrderPrev)
                .totalPost(totalPost)
                .totalPostPrev(totalPostPrev)
                .totalCustomer(totalCustomer)
                .totalCustomerPrev(totalCustomerPrev)
                .totalRevenue(totalRevenue)
                .totalRevenuePrev(totalRevenuePrev)
                .monthlyRevenue(monthlyRevenue)
                .monthlyRevenuePrev(monthlyRevenuePrev)
                .customerList(customerList)
                .orderList(orderList)
                .topCategory(topCategories)
                .build();
    }
}
