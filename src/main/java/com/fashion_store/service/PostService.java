package com.fashion_store.service;

import com.fashion_store.Utils.GenerateSlugUtils;
import com.fashion_store.dto.post.request.PostRequest;
import com.fashion_store.dto.post.response.PostResponse;
import com.fashion_store.entity.Post;
import com.fashion_store.entity.Topic;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.mapper.PostMapper;
import com.fashion_store.repository.PostRepository;
import com.fashion_store.repository.TopicRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService extends GenerateService<Post, Long> {
    PostRepository postRepository;
    CloudinaryService cloudinaryService;
    PostMapper postMapper;
    private final TopicRepository topicRepository;

    @Override
    JpaRepository<Post, Long> getRepository() {
        return postRepository;
    }

    public PostResponse create(PostRequest request) {
        if (postRepository.existsByTitle(request.getTitle()))
            throw new AppException(ErrorCode.EXISTED);
        Post post = postMapper.toPost(request);

        if (request.getTopicId() != null) {
            Topic topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
            post.setTopic(topic);
        }

        // handle image
        if (request.getImage() != null) {
            if (!request.getImage().isEmpty()) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(request.getImage());
                    post.setImage(imageUrl);

                } catch (IOException e) {
                    throw new AppException(ErrorCode.FILE_SAVE_FAILED);
                }
            } else {
                post.setImage("");
            }
        }

        // slug
        String baseSlug = GenerateSlugUtils.generateSlug(post.getTitle());
        List<Post> existing = postRepository.findBySlugStartingWith(baseSlug);
        String finalSlug = baseSlug;
        if (!existing.isEmpty()) {
            finalSlug = baseSlug + "-" + existing.size();
        }
        post.setSlug(finalSlug);

        post = postRepository.save(post);
        return postMapper.toPostResponse(post);
    }

    public List<PostResponse> getAll(boolean deleted) {
        return postRepository.findAll()
                .stream()
                .filter(item -> item.getIsDeleted() == deleted)
                .map(postMapper::toPostResponse)
                .collect(Collectors.toList());
    }

    public PostResponse getInfo(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        return postMapper.toPostResponse(post);
    }

    public void status(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        try {
            post.setStatus(post.getStatus() == null || !post.getStatus());
            postRepository.save(post);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }

    public PostResponse update(PostRequest request, Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (postRepository.existsByTitleAndIdNot(request.getTitle(), id))
            throw new AppException(ErrorCode.EXISTED);

        postMapper.updatePost(post, request);

        if (request.getTopicId() != null) {
            Topic topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
            post.setTopic(topic);
        }

        // handle image
        boolean imageDelete = request.getImageDelete() != null && request.getImageDelete();
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadFile(request.getImage());
                // Lưu URL vào DB
                post.setImage(imageUrl);
            } catch (IOException e) {
                throw new AppException(ErrorCode.FILE_SAVE_FAILED);
            }
        } else if (imageDelete) {
            post.setImage("");
        }

        // slug
        String baseSlug = GenerateSlugUtils.generateSlug(post.getTitle());
        List<Post> existing = postRepository.findBySlugStartingWith(baseSlug)
                .stream().filter(item -> !item.getId().equals(id)).toList();
        String finalSlug = baseSlug;
        if (!existing.isEmpty()) {
            finalSlug = baseSlug + "-" + existing.size();
        }
        post.setSlug(finalSlug);

        post = postRepository.save(post);
        return postMapper.toPostResponse(post);
    }
}
