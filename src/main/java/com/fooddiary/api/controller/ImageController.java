package com.fooddiary.api.controller;
import com.fooddiary.api.dto.response.DayImageDto;
import com.fooddiary.api.dto.response.DayImagesDto;
import com.fooddiary.api.dto.response.SaveImageResponseDto;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.DayImageService;
import com.fooddiary.api.service.UserService;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.nio.charset.StandardCharsets;
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

    @PostMapping("/saveImage")
    public ResponseEntity<SaveImageResponseDto> saveImage(@RequestPart("files") List<MultipartFile> multipartFiles,
                          @RequestParam("localDateTime") LocalDateTime localDateTime,
                          HttpServletRequest request){
        User user = getUser(request);

        SaveImageResponseDto saveImageResponseDto = dayImageService.saveImage(multipartFiles, localDateTime, user);
        return ResponseEntity.ok(saveImageResponseDto);
    }



    /***
     * /image?year=2023&month=6&day=30
     * 하루 사진 받기
     */
    @GetMapping("/image")
    public ResponseEntity<List<DayImageDto>> showImage(@RequestParam int year, @RequestParam int month,
                                                       @RequestParam int day) {
        User user = getUser();
        List<DayImageDto> dayImageDto = dayImageService.getDayImage(year, month, day, user);
        return ResponseEntity.ok(dayImageDto);
    }

    /***
     *
     * /image?year=2023&month=7
     * 한 달의 사진 받기
     */
    @GetMapping("/images")
    public ResponseEntity<List<DayImagesDto>> showImages(@RequestParam int year, @RequestParam int month) {
        User user = getUser();
        List<DayImagesDto> dayImages = dayImageService.getDayImages(year, month, user);
        return ResponseEntity.ok(dayImages);
    }


    private static User getUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user;
    }

    private User getUser(HttpServletRequest request) {
        User user = userService.getValidUser(request.getHeader(MAIL_NAME), request.getHeader(TOKEN_NAME));
        return user;
    }
}
