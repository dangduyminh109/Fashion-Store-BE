package com.fashion_store.entity;

import com.fashion_store.enums.DiscountType;
import com.fashion_store.enums.OrderStatus;
import com.fashion_store.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "orders")
public class Order extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Builder.Default
    Boolean isPaid = false;
    LocalDateTime paidAt;
    @Column(nullable = false)
    BigDecimal totalAmount;
    @Builder.Default
    BigDecimal totalDiscount = BigDecimal.ZERO;
    @Column(nullable = false)
    BigDecimal finalAmount;
    @Column(nullable = false)
    String customerName;
    @Column(nullable = false)
    PaymentMethod paymentMethod;
    @Column(nullable = false)
    OrderStatus orderStatus;
    @Column(nullable = false)
    String address;
    @Column(nullable = false)
    String phone;
    @Column(nullable = false)
    String city;
    @Column(nullable = false)
    String district;
    @Column(nullable = false)
    String ward;

    @Column(nullable = false)
    Integer cityId;
    @Column(nullable = false)
    Integer districtId;
    @Column(nullable = false)
    Integer wardId;

    String note;
    String transactionRef;

    String voucherName;
    DiscountType discountType;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    Customer customer;

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    Voucher voucher;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderItem> orderItems = new ArrayList<>();
}
