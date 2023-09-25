package com.fooddiary.api.controller;
import com.fooddiary.api.dto.request.SaveImageRequestDTO;
import com.fooddiary.api.dto.request.UpdateImageDetailDTO;
import com.fooddiary.api.dto.response.*;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.DayImageService;
import com.fooddiary.api.service.ImageService;
import com.fooddiary.api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/image")
public class ImageController {

    private static final String MAIL_NAME = "email";
    private static final String TOKEN_NAME = "token";

    private final DayImageService dayImageService;
    private final ImageService imageService;
    private final UserService userService;


    @PostMapping(value = "/saveImage", consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE
    })
    public ResponseEntity<StatusResponseDTO> saveImage(final @RequestPart("files") List<MultipartFile> multipartFiles,
                                                       final @RequestPart("imageDetails") SaveImageRequestDTO saveImageRequestDTO,
                                                       HttpServletRequest request){
        final User user = getUser(request);

        return ResponseEntity.ok(dayImageService.saveImage(multipartFiles, saveImageRequestDTO, user));
    }



    /***
     * /image?year=2023&month=6&day=30
     * 하루 사진 받기
     */
    @GetMapping("/image")
    public ResponseEntity<ShowImageOfDayDTO> showImageOfDay(final @RequestParam int year, final @RequestParam int month,
                                                                  final @RequestParam int day) {

        final User user = getUser();
        return ResponseEntity.ok(imageService.getImages(year, month, day, user));
    }

    /***
     *
     * /image?year=2023&month=7
     * 한 달의 사진 받기
     */
    @GetMapping("/images")
    public ResponseEntity<List<ThumbNailImagesDTO>> showThumbNailImages(final @RequestParam int year, final @RequestParam int month) {

        final User user = getUser();

        return ResponseEntity.ok(dayImageService.getThumbNailImages(year, month, user));
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<TimeLineResponseDTO>> showTimeLine(final @RequestParam int year, final @RequestParam int month) {
        final User user = getUser();
        return ResponseEntity.ok(dayImageService.getTimeLine(year, month, user));
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ImageDetailResponseDTO> showImage(final @PathVariable Integer imageId) {
        User user = getUser();
        return ResponseEntity.ok(imageService.getImageDetail(imageId, user));
    }

    @PatchMapping("/{imageId}")
    public ResponseEntity<StatusResponseDTO> updateImage(final @PathVariable Integer imageId,
                                                         final @RequestBody UpdateImageDetailDTO updateImageDetailDTO) {
        final User user = getUser();
        return ResponseEntity.ok(imageService.updateImageDetail(imageId, user, updateImageDetailDTO));
    }


    private User getUser() {

        final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user;
    }

    private User getUser(HttpServletRequest request) {
        final User user = userService.getValidUser(request.getHeader(MAIL_NAME), request.getHeader(TOKEN_NAME));

        return user;
    }

}
