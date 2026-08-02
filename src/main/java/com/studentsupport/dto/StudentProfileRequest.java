package com.studentsupport.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentProfileRequest {

    private String course;
    private String previousBackground;
    private String careerGoal;
    private String skillInterests;
}
