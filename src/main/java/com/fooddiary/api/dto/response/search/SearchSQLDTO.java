package com.fooddiary.api.dto.response.search;

import com.fooddiary.api.dto.request.search.CategoryType;

public interface SearchSQLDTO {
    String getCategoryName();
    CategoryType getCategoryType();
}
