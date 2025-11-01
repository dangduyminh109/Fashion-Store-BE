package com.fashion_store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "addresses")
public class Address extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false)
    String name;
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

    Boolean isDefault;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    Customer customer;
}
