package com.fooddiary.api.controller;
import com.fooddiary.api.dto.request.ImageCreateDto;
import com.fooddiary.api.dto.response.DayImageDto;
import com.fooddiary.api.dto.response.DayImagesDto;
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


    /***
     * /image?year=2023,month=6,day=30
     * 하루 사진 받기
     */
    @GetMapping("/image")
    public ResponseEntity<List<DayImageDto>> showImage(@RequestParam int year, @RequestParam int month, @RequestParam int day) {
        List<DayImageDto> dayImageDto = dayImageService.getDayImage(year, month, day);
        return ResponseEntity.ok(dayImageDto);
    }

    /***
     *
     * /image?year=2023,month=7
     * 한 달의 사진 받기
     */
    @GetMapping("/images")
    public ResponseEntity<List<DayImagesDto>> showImages(@RequestParam int year, @RequestParam int month) {
        List<DayImagesDto> dayImages = dayImageService.getDayImages(year, month);

        return ResponseEntity.ok(dayImages);
    }



}
