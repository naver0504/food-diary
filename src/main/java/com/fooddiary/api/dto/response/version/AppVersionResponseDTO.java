package com.fooddiary.api.dto.response.version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AppVersionResponseDTO {

    private String version;
}
