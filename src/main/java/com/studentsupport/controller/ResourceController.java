package com.studentsupport.controller;

import com.studentsupport.dto.ResourceResponse;
import com.studentsupport.service.ResourceService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public Page<ResourceResponse> search(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String category,
                                          Pageable pageable) {
        return resourceService.search(keyword, category, pageable);
    }

    @GetMapping("/{resourceId}")
    public ResourceResponse getById(@PathVariable Long resourceId) {
        return resourceService.getById(resourceId);
    }

    @GetMapping("/{resourceId}/open")
    public void open(@PathVariable Long resourceId, HttpServletResponse response) throws IOException {
        String url = resourceService.getById(resourceId).getUrl();
        response.sendRedirect(url);
    }
}
