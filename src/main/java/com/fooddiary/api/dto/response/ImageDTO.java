package com.fooddiary.api.dto.response;


import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fooddiary.api.entity.image.TimeStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageDTO {

    int id;
    byte[] bytes;

    @JsonIgnore
    TimeStatus timeStatus;

    String time;
}
