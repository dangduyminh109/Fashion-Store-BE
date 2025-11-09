package com.fashion_store.dto.post.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostFeaturedResponse {
    Long id;
    String title;
    String content;
    String slug;
    String topicName;
    String image;
    LocalDateTime createdAt;
}