package com.studentsupport.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentProfileResponse {

    private Long profileId;
    private Long userId;
    private String course;
    private String previousBackground;
    private String careerGoal;
    private String skillInterests;
}
