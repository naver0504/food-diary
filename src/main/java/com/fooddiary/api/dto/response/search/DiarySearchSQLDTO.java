package com.fooddiary.api.dto.response.search;

import com.fooddiary.api.entity.diary.DiaryTime;

public interface DiarySearchSQLDTO {

    Integer getId();
    String getThumbnailFileName();

    interface DiarySearchWithDiaryTimeSQLDTO extends DiarySearchSQLDTO {
        DiaryTime getDiaryTime();
    }

    interface DiarySearchWithTagSQLDTO extends DiarySearchSQLDTO {
        String getTagName();
    }

    interface DiarySearchWithPlaceSQLDTO extends DiarySearchSQLDTO {
        String getPlace();
    }
}
