package com.fooddiary.api.controller;
import com.fooddiary.api.dto.request.ImageCreateDto;
import com.fooddiary.api.dto.response.DayImageDto;
import com.fooddiary.api.dto.response.DayImageDtos;
import com.fooddiary.api.service.DayImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final DayImageService dayImageService;

    @PostMapping("/saveImage")
    public void saveImage(@RequestBody ImageCreateDto imageCreateDto) throws IOException {
        dayImageService.saveImage(imageCreateDto.getMultipartFile(), imageCreateDto.getLocalDateTime());
    }

    @GetMapping("/image")
    public ResponseEntity<DayImageDtos> showImage(@RequestParam int year, @RequestParam int month, @RequestParam int day) {
        List<DayImageDto> dayImageDto = dayImageService.getDayImage(year, month, day);
        DayImageDtos dayImageDtos = new DayImageDtos(dayImageDto);
        return ResponseEntity.ok(dayImageDtos);
    }



}
