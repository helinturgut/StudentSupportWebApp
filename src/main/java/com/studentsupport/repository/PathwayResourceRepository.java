package com.studentsupport.repository;

import com.studentsupport.entity.CareerPathway;
import com.studentsupport.entity.PathwayResource;
import com.studentsupport.entity.PathwayResourceId;
import com.studentsupport.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PathwayResourceRepository extends JpaRepository<PathwayResource, PathwayResourceId> {

    List<PathwayResource> findByPathway(CareerPathway pathway);

    List<PathwayResource> findByResource(Resource resource);

    void deleteByPathwayAndResource(CareerPathway pathway, Resource resource);
}
