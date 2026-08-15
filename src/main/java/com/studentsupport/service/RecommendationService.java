package com.studentsupport.service;

import com.studentsupport.dto.RecommendationResponse;
import com.studentsupport.entity.CareerPathway;
import com.studentsupport.entity.Recommendation;
import com.studentsupport.entity.RecommendationType;
import com.studentsupport.entity.Resource;
import com.studentsupport.entity.StudentProfile;
import com.studentsupport.entity.User;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.repository.CareerPathwayRepository;
import com.studentsupport.repository.RecommendationRepository;
import com.studentsupport.repository.ResourceRepository;
import com.studentsupport.repository.StudentProfileRepository;
import com.studentsupport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationService {

    private static final int MAX_RESOURCES = 5;
    private static final int MAX_PATHWAYS = 3;

    private final RecommendationRepository recommendationRepository;
    private final ResourceRepository resourceRepository;
    private final CareerPathwayRepository careerPathwayRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    public List<RecommendationResponse> generateForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        StudentProfile profile = studentProfileRepository.findByUser(user).orElse(null);

        String interestTerm = profile != null && StringUtils.hasText(profile.getSkillInterests())
                ? profile.getSkillInterests()
                : (profile != null ? profile.getCourse() : null);
        String goalTerm = profile != null && StringUtils.hasText(profile.getCareerGoal())
                ? profile.getCareerGoal()
                : interestTerm;

        List<Recommendation> recommendations = new ArrayList<>();

        List<Resource> resources = StringUtils.hasText(interestTerm)
                ? resourceRepository.findByKeywordAndCategory(interestTerm, null, PageRequest.of(0, MAX_RESOURCES)).getContent()
                : resourceRepository.findAll(PageRequest.of(0, MAX_RESOURCES)).getContent();
        for (Resource resource : resources) {
            recommendations.add(Recommendation.builder()
                    .user(user)
                    .resource(resource)
                    .recommendationType(RecommendationType.RESOURCE)
                    .reason(StringUtils.hasText(interestTerm)
                            ? "Matches your interest in \"" + interestTerm + "\""
                            : "Recently added learning resource")
                    .build());
        }

        List<CareerPathway> pathways = StringUtils.hasText(goalTerm)
                ? careerPathwayRepository.findByTitleOrSkillsContaining(goalTerm)
                : careerPathwayRepository.findAll();
        for (CareerPathway pathway : pathways.stream().limit(MAX_PATHWAYS).toList()) {
            recommendations.add(Recommendation.builder()
                    .user(user)
                    .pathway(pathway)
                    .recommendationType(RecommendationType.PATHWAY)
                    .reason(StringUtils.hasText(goalTerm)
                            ? "Matches your career goal \"" + goalTerm + "\""
                            : "Suggested career pathway to explore")
                    .build());
        }

        recommendationRepository.deleteByUser(user);
        List<Recommendation> saved = recommendationRepository.saveAll(recommendations);
        return saved.stream().map(this::toResponse).toList();
    }

    public List<RecommendationResponse> getForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<Recommendation> existing = recommendationRepository.findByUserOrderByCreatedAtDesc(user);
        if (existing.isEmpty()) {
            return generateForUser(userId);
        }
        return existing.stream().map(this::toResponse).toList();
    }

    private RecommendationResponse toResponse(Recommendation recommendation) {
        return RecommendationResponse.builder()
                .recommendationId(recommendation.getRecommendationId())
                .recommendationType(recommendation.getRecommendationType())
                .resourceId(recommendation.getResource() != null ? recommendation.getResource().getResourceId() : null)
                .resourceTitle(recommendation.getResource() != null ? recommendation.getResource().getTitle() : null)
                .pathwayId(recommendation.getPathway() != null ? recommendation.getPathway().getPathwayId() : null)
                .pathwayTitle(recommendation.getPathway() != null ? recommendation.getPathway().getTitle() : null)
                .reason(recommendation.getReason())
                .createdAt(recommendation.getCreatedAt())
                .build();
    }
}
