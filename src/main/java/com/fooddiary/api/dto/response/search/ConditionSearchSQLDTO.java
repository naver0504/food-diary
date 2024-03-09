package com.fooddiary.api.dto.response.search;

import com.fooddiary.api.dto.request.search.CategoryType;

public interface ConditionSearchSQLDTO {
    byte[] getCategoryName();
    CategoryType getCategoryType();
}
