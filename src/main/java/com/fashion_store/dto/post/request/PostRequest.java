package com.fashion_store.dto.post.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostRequest {
    @NotBlank(message = "INVALID_TITLE")
    String title;
    @NotBlank(message = "INVALID_CONTENT")
    String content;
    @Builder.Default
    Boolean status = true;
    MultipartFile image;
    @Builder.Default
    Boolean imageDelete = false;
    Long topicId;
}
