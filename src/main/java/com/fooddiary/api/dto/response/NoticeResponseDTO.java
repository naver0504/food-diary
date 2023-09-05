package com.fooddiary.api.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeResponseDTO {
    private Integer id;
    private String title;
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    //@JsonDeserialize(using = LocalDateTimeDeserializer.class)
    //@JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDate noticeAt;
}
