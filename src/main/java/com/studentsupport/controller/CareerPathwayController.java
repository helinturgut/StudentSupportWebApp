package com.studentsupport.controller;

import com.studentsupport.dto.CareerPathwayResponse;
import com.studentsupport.service.CareerPathwayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/career-pathways")
@RequiredArgsConstructor
public class CareerPathwayController {

    private final CareerPathwayService careerPathwayService;

    @GetMapping
    public List<CareerPathwayResponse> search(@RequestParam(required = false) String term,
                                               @RequestParam(required = false) String profession) {
        if (StringUtils.hasText(profession)) {
            return careerPathwayService.searchByProfession(profession);
        }
        return careerPathwayService.search(term);
    }

    @GetMapping("/professions")
    public List<String> getProfessions() {
        return careerPathwayService.getProfessions();
    }

    @GetMapping("/{pathwayId}")
    public CareerPathwayResponse getById(@PathVariable Long pathwayId) {
        return careerPathwayService.getById(pathwayId);
    }
}
