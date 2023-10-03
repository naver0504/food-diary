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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
                                                                  final @RequestParam int day, final @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(imageService.getImages(year, month, day, user));
    }

    /***
     *
     * /image?year=2023&month=7
     * 한 달의 사진 받기
     */
    @GetMapping("/images")
    public ResponseEntity<List<ThumbNailImagesDTO>> showThumbNailImages(final @RequestParam int year, final @RequestParam int month, final @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(dayImageService.getThumbNailImages(year, month, user));
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<TimeLineResponseDTO>> showTimeLine(final @RequestParam int year, final @RequestParam int month,
                                                                  final @RequestParam(defaultValue = "31") int startDay, final @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dayImageService.getTimeLine(year, month, startDay, user));
    }

    @GetMapping("/timeline/{startImageId}")
    public ResponseEntity<List<TimeLineResponseDTO.ImageResponseDTO>> showTimeLineWithStartImageId(final @RequestParam int year, final @RequestParam int month, final @RequestParam int day,
                                                                  final @PathVariable int startImageId, final @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(imageService.getTimeLineImagesWithStartImageId(year, month, day, startImageId, user));
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ImageDetailResponseDTO> showImageDetail(final @PathVariable int imageId, final @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(imageService.getImageDetail(imageId, user));
    }

    @PatchMapping("/{imageId}")
    public ResponseEntity<StatusResponseDTO> updateImageFile(final @RequestPart MultipartFile file, final @PathVariable int imageId,
                                                             final @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(imageService.updateImage(imageId, file, user));
    }

    @PostMapping("/{imageId}")
    public ResponseEntity<StatusResponseDTO> uploadDetailImages(final @RequestPart List<MultipartFile> files, final @PathVariable int imageId,
                                                                final @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(imageService.updateImageFile(files, imageId, user));
    }

    @PostMapping("/{parentImageId}/detail")
    public ResponseEntity<StatusResponseDTO> updateImageDetail(final @PathVariable int parentImageId,
                                                               final @RequestBody UpdateImageDetailDTO updateImageDetailDTO,
                                                               final @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(imageService.updateImageDetail(parentImageId, user, updateImageDetailDTO));
    }

    private User getUser(HttpServletRequest request) {
        final User user = userService.getValidUser(request.getHeader(MAIL_NAME), request.getHeader(TOKEN_NAME));

        return user;
    }

}
