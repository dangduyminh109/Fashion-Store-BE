package com.fashion_store.mapper;

import com.fashion_store.dto.attribute.request.AttributeRequest;
import com.fashion_store.dto.attribute.response.AttributeClientResponse;
import com.fashion_store.dto.attribute.response.AttributeResponse;
import com.fashion_store.entity.Attribute;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {AttributeValueMapper.class})
public interface AttributeMapper {
    Attribute toAttribute(AttributeRequest attributeRequest);

    AttributeResponse toAttributeResponse(Attribute attribute);

    AttributeClientResponse toAttributeClientResponse(Attribute attribute);

    void updateAttribute(@MappingTarget Attribute attribute, AttributeRequest attributeRequest);
}
