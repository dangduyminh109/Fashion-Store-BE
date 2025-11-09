package com.fashion_store.service;

import com.fashion_store.Utils.GenerateSlugUtils;
import com.fashion_store.dto.topic.request.TopicRequest;
import com.fashion_store.dto.topic.response.TopicResponse;
import com.fashion_store.entity.Topic;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.mapper.TopicMapper;
import com.fashion_store.repository.TopicRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TopicService extends GenerateService<Topic, Long> {
    TopicRepository topicRepository;
    TopicMapper topicMapper;

    @Override
    JpaRepository<Topic, Long> getRepository() {
        return topicRepository;
    }

    public TopicResponse create(TopicRequest request) {
        if (topicRepository.existsByName(request.getName()))
            throw new AppException(ErrorCode.EXISTED);
        Topic topic = topicMapper.toTopic(request);
        // slug
        String baseSlug = GenerateSlugUtils.generateSlug(topic.getName());
        List<Topic> existing = topicRepository.findBySlugStartingWith(baseSlug);
        String finalSlug = baseSlug;
        if (!existing.isEmpty()) {
            finalSlug = baseSlug + "-" + existing.size();
        }
        topic.setSlug(finalSlug);

        topic = topicRepository.save(topic);
        return topicMapper.toTopicResponse(topic);
    }

    public List<TopicResponse> getAll(boolean deleted) {
        return topicRepository.findAll()
                .stream()
                .filter(item -> item.getIsDeleted() == deleted)
                .map(topicMapper::toTopicResponse)
                .collect(Collectors.toList());
    }

    public List<TopicResponse> getTopic() {
        return topicRepository.findAll()
                .stream()
                .filter(item -> item.getIsDeleted() == false && item.getStatus())
                .map(topicMapper::toTopicResponse)
                .collect(Collectors.toList());
    }

    public TopicResponse getInfo(Long id) {
        Topic topic = topicRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        return topicMapper.toTopicResponse(topic);
    }

    public TopicResponse update(TopicRequest request, Long id) {
        Topic topic = topicRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (topicRepository.existsByNameAndIdNot(request.getName(), id))
            throw new AppException(ErrorCode.EXISTED);

        topicMapper.updateTopic(topic, request);

        // slug
        String baseSlug = GenerateSlugUtils.generateSlug(topic.getName());
        List<Topic> existing = topicRepository.findBySlugStartingWith(baseSlug)
                .stream().filter(item -> !item.getId().equals(id)).toList();
        String finalSlug = baseSlug;
        if (!existing.isEmpty()) {
            finalSlug = baseSlug + "-" + existing.size();
        }
        topic.setSlug(finalSlug);

        topic = topicRepository.save(topic);
        return topicMapper.toTopicResponse(topic);
    }

    public void status(Long id) {
        Topic topic = topicRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        try {
            topic.setStatus(topic.getStatus() == null || !topic.getStatus());
            topicRepository.save(topic);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }
}
