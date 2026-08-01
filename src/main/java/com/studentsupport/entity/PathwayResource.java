package com.studentsupport.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"pathway", "resource"})
@Entity
@Table(name = "pathway_resources")
public class PathwayResource {

    @EmbeddedId
    private PathwayResourceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pathwayId")
    @JoinColumn(name = "pathway_id")
    private CareerPathway pathway;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("resourceId")
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @Column(columnDefinition = "TEXT")
    private String linkReason;
}
