package com.fashion_store.entity;

import com.fashion_store.enums.ChatRole;
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
@Table(name = "conversation")
public class Conversation extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ChatRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    String message;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    Customer customer;
}
