package com.fooddiary.api.dto.request;

import com.fooddiary.api.entity.image.DiaryTime;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SaveImageRequestDTO {
    private Integer diaryId;
    @Builder.Default
    private Double longitude = -200D;
    @Builder.Default
    private Double latitude = -200D;
}
