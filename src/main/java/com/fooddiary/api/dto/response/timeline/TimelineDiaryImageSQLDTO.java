package com.fooddiary.api.dto.response.timeline;

import java.time.LocalDateTime;

public interface TimelineDiaryImageSQLDTO {
    Integer getId();
    LocalDateTime getCreate_time();
    String getStored_file_name();
    Integer getImage_id();
}
