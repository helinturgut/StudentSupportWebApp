package com.studentsupport.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PathwayResourceId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long pathwayId;
    private Long resourceId;
}
