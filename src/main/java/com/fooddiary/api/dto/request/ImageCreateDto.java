package com.fooddiary.api.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ImageCreateDto {

    private List<MultipartFile> multipartFile;
    private LocalDateTime localDateTime;
}
