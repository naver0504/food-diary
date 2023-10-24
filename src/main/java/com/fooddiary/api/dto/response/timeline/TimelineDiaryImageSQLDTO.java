package com.fooddiary.api.dto.response.timeline;

import java.time.LocalDateTime;

public interface TimelineDiaryImageSQLDTO {
    Integer getId();
    LocalDateTime getCreateTime();
    String getStoredFileName();
    Long getImageId();
}
