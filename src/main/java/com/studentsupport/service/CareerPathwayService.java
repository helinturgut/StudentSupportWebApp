package com.studentsupport.service;

import com.studentsupport.dto.CareerPathwayRequest;
import com.studentsupport.dto.CareerPathwayResponse;
import com.studentsupport.dto.PathwayResourceLinkRequest;
import com.studentsupport.dto.PathwayResourceResponse;
import com.studentsupport.entity.CareerPathway;
import com.studentsupport.entity.PathwayResource;
import com.studentsupport.entity.PathwayResourceId;
import com.studentsupport.entity.Resource;
import com.studentsupport.entity.User;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.repository.CareerPathwayRepository;
import com.studentsupport.repository.PathwayResourceRepository;
import com.studentsupport.repository.ResourceRepository;
import com.studentsupport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CareerPathwayService {

    private final CareerPathwayRepository careerPathwayRepository;
    private final PathwayResourceRepository pathwayResourceRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public List<CareerPathwayResponse> search(String term) {
        List<CareerPathway> pathways = StringUtils.hasText(term)
                ? careerPathwayRepository.findByTitleOrSkillsContaining(term)
                : careerPathwayRepository.findAll();
        return pathways.stream().map(this::toResponse).toList();
    }

    public List<CareerPathwayResponse> searchByProfession(String profession) {
        return careerPathwayRepository.findByProfessionIgnoreCaseOrderByTitleAsc(profession).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<String> getProfessions() {
        return careerPathwayRepository.findDistinctProfessions();
    }

    public CareerPathwayResponse getById(Long pathwayId) {
        return toResponse(findById(pathwayId));
    }

    public CareerPathwayResponse create(Long createdByUserId, CareerPathwayRequest request) {
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CareerPathway pathway = CareerPathway.builder()
                .createdBy(createdBy)
                .profession(request.getProfession())
                .title(request.getTitle())
                .description(request.getDescription())
                .requiredSkills(request.getRequiredSkills())
                .build();

        return toResponse(careerPathwayRepository.save(pathway));
    }

    public CareerPathwayResponse update(Long pathwayId, CareerPathwayRequest request) {
        CareerPathway pathway = findById(pathwayId);
        pathway.setProfession(request.getProfession());
        pathway.setTitle(request.getTitle());
        pathway.setDescription(request.getDescription());
        pathway.setRequiredSkills(request.getRequiredSkills());
        return toResponse(careerPathwayRepository.save(pathway));
    }

    public void delete(Long pathwayId) {
        if (!careerPathwayRepository.existsById(pathwayId)) {
            throw new ResourceNotFoundException("Career pathway not found");
        }
        careerPathwayRepository.deleteById(pathwayId);
    }

    public CareerPathwayResponse linkResource(Long pathwayId, PathwayResourceLinkRequest request) {
        CareerPathway pathway = findById(pathwayId);
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        PathwayResource pathwayResource = PathwayResource.builder()
                .id(new PathwayResourceId(pathway.getPathwayId(), resource.getResourceId()))
                .pathway(pathway)
                .resource(resource)
                .linkReason(request.getLinkReason())
                .build();
        pathwayResourceRepository.save(pathwayResource);

        return toResponse(pathway);
    }

    public void unlinkResource(Long pathwayId, Long resourceId) {
        CareerPathway pathway = findById(pathwayId);
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        pathwayResourceRepository.deleteByPathwayAndResource(pathway, resource);
    }

    private CareerPathway findById(Long pathwayId) {
        return careerPathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Career pathway not found"));
    }

    private CareerPathwayResponse toResponse(CareerPathway pathway) {
        List<PathwayResourceResponse> resources = pathwayResourceRepository.findByPathway(pathway).stream()
                .map(pr -> PathwayResourceResponse.builder()
                        .resourceId(pr.getResource().getResourceId())
                        .resourceTitle(pr.getResource().getTitle())
                        .resourceUrl(pr.getResource().getUrl())
                        .linkReason(pr.getLinkReason())
                        .build())
                .toList();

        return CareerPathwayResponse.builder()
                .pathwayId(pathway.getPathwayId())
                .profession(pathway.getProfession())
                .title(pathway.getTitle())
                .description(pathway.getDescription())
                .requiredSkills(pathway.getRequiredSkills())
                .createdByUserId(pathway.getCreatedBy().getUserId())
                .createdByName(pathway.getCreatedBy().getFullName())
                .createdAt(pathway.getCreatedAt())
                .resources(resources)
                .build();
    }
}
