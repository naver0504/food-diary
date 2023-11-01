package com.fooddiary.api.dto.response.search;

import com.fooddiary.api.entity.diary.DiaryTime;

public interface DiarySearchSQLDTO {

    Integer getId();
    String getThumbnailFileName();
    String getPlace();

    interface DiarySearchNoTagSQLDTO extends DiarySearchSQLDTO {
        DiaryTime getDiaryTime();
    }

    interface DiarySearchWithTagSQLDTO extends DiarySearchSQLDTO {
        String getTagName();
    }
}
