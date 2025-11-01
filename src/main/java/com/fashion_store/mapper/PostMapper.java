package com.fashion_store.mapper;

import com.fashion_store.dto.post.request.PostRequest;
import com.fashion_store.dto.post.response.PostClientResponse;
import com.fashion_store.dto.post.response.PostResponse;
import com.fashion_store.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PostMapper {
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "image", ignore = true)
    Post toPost(PostRequest postRequest);

    @Mapping(target = "topicName", expression = "java(post.getTopic() != null ? post.getTopic().getName() : null)")
    @Mapping(target = "topicId", expression = "java(post.getTopic() != null ? post.getTopic().getId() : null)")
    PostResponse toPostResponse(Post post);

    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "slug", ignore = true)
    void updatePost(@MappingTarget Post post, PostRequest postRequest);

    @Mapping(target = "topicName", expression = "java(post.getTopic() != null ? post.getTopic().getName() : null)")
    PostClientResponse toPostClientResponse(Post post);
}
