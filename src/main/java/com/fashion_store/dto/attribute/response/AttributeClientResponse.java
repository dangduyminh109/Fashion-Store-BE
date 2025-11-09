package com.fashion_store.dto.attribute.response;

import com.fashion_store.enums.AttributeDisplayType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttributeClientResponse {
    Long id;
    String name;
    AttributeDisplayType attributeDisplayType;
    List<AttributeValueResponse> listAttributeValue;
}
