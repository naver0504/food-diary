package com.fooddiary.api.controller;
import com.fooddiary.api.dto.response.DayImageDto;
import com.fooddiary.api.dto.response.DayImagesDto;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.DayImageService;
import com.fooddiary.api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/image")
public class ImageController {

    private static final String MAIL_NAME = "email";
    private static final String TOKEN_NAME = "token";

    private final DayImageService dayImageService;
    private final UserService userService;

    @PostMapping(value = "/saveImage")
    public void saveImage(@RequestPart("files") List<MultipartFile> multipartFiles,
                          @RequestParam("localDateTime") LocalDateTime localDateTime,
                          HttpServletRequest request) throws IOException {
        User user = getUser(request);
        dayImageService.saveImage(multipartFiles, localDateTime, user);
    }

    /***
     * /image?year=2023&month=6&day=30
     * 하루 사진 받기
     */
    @GetMapping("/image")
    public ResponseEntity<List<DayImageDto>> showImage(@RequestParam int year, @RequestParam int month,
                                                       @RequestParam int day,  HttpServletRequest request) throws IOException {
        User user = getUser(request);
        List<DayImageDto> dayImageDto = dayImageService.getDayImage(year, month, day, user);
        return ResponseEntity.ok(dayImageDto);
    }

    /***
     *
     * /image?year=2023&month=7
     * 한 달의 사진 받기
     */
    @GetMapping("/images")
    public ResponseEntity<List<DayImagesDto>> showImages(@RequestParam int year, @RequestParam int month,
                                                         HttpServletRequest request) throws IOException {
        User user = getUser(request);
        List<DayImagesDto> dayImages = dayImageService.getDayImages(year, month, user);
        return ResponseEntity.ok(dayImages);
    }


    private User getUser(HttpServletRequest request) {
        User user = userService.getValidUser(request.getHeader(MAIL_NAME), request.getHeader(TOKEN_NAME));
        return user;
    }
}
