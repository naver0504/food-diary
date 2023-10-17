package com.fooddiary.api.dto.response.notice;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeResponseDTO {

    private List<NoticeDTO> list;
    private long count;

    @Getter
    @Setter
    public static class NoticeDTO {
        private Integer id;
        private String title;
        private String content;
        private boolean available;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        //@JsonDeserialize(using = LocalDateTimeDeserializer.class)
        //@JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDate noticeAt;
    }

}
