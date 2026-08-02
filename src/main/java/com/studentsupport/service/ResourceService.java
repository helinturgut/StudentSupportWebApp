package com.studentsupport.service;

import com.studentsupport.dto.ResourceRequest;
import com.studentsupport.dto.ResourceResponse;
import com.studentsupport.entity.Resource;
import com.studentsupport.entity.User;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.repository.ResourceRepository;
import com.studentsupport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public Page<ResourceResponse> search(String keyword, String category, Pageable pageable) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword : null;
        String normalizedCategory = StringUtils.hasText(category) ? category : null;
        return resourceRepository.findByKeywordAndCategory(normalizedKeyword, normalizedCategory, pageable)
                .map(this::toResponse);
    }

    public ResourceResponse getById(Long resourceId) {
        return toResponse(findById(resourceId));
    }

    public ResourceResponse create(Long createdByUserId, ResourceRequest request) {
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Resource resource = Resource.builder()
                .createdBy(createdBy)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .url(request.getUrl())
                .build();

        return toResponse(resourceRepository.save(resource));
    }

    public ResourceResponse update(Long resourceId, ResourceRequest request) {
        Resource resource = findById(resourceId);
        resource.setTitle(request.getTitle());
        resource.setDescription(request.getDescription());
        resource.setCategory(request.getCategory());
        resource.setUrl(request.getUrl());
        return toResponse(resourceRepository.save(resource));
    }

    public void delete(Long resourceId) {
        if (!resourceRepository.existsById(resourceId)) {
            throw new ResourceNotFoundException("Resource not found");
        }
        resourceRepository.deleteById(resourceId);
    }

    private Resource findById(Long resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
    }

    private ResourceResponse toResponse(Resource resource) {
        return ResourceResponse.builder()
                .resourceId(resource.getResourceId())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .category(resource.getCategory())
                .url(resource.getUrl())
                .createdByUserId(resource.getCreatedBy().getUserId())
                .createdByName(resource.getCreatedBy().getFullName())
                .createdAt(resource.getCreatedAt())
                .build();
    }
}
