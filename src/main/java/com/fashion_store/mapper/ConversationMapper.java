package com.fashion_store.mapper;

import com.fashion_store.dto.chat.request.ConversationHistory;
import com.fashion_store.entity.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConversationMapper {
    List<ConversationHistory> toConversationHistory(List<Conversation> conversations);
}
