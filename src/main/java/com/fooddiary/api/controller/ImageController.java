package com.fooddiary.api.controller;
import com.fooddiary.api.dto.request.SaveImageRequestDTO;
import com.fooddiary.api.dto.request.UpdateImageDetailDTO;
import com.fooddiary.api.dto.response.*;
import com.fooddiary.api.dto.response.diary.HomeResponseDTO;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.DayImageService;
import com.fooddiary.api.service.ImageService;
import com.fooddiary.api.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import static com.fooddiary.api.common.constants.UserConstants.LOGIN_FROM_KEY;
import static com.fooddiary.api.common.constants.UserConstants.TOKEN_KEY;


@RestController
@RequiredArgsConstructor
@Slf4j
//@RequestMapping("/image")
public class ImageController {

    private static final String MAIL_NAME = "email";
    private static final String TOKEN_NAME = "token";

    private final DayImageService dayImageService;
    private final ImageService imageService;
    private final UserService userService;

    /**
     * 사진 추가를 하는 메소드
     * @param multipartFiles
     * @param saveImageRequestDTO
     * @param request
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws InterruptedException
     */
    //@PostMapping(value = "/saveImage", consumes = {
    //        MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE
   // })
    public ResponseEntity<StatusResponseDTO> saveImage(final @RequestPart("file") MultipartFile multipartFiles,
                                                       final @RequestPart("imageDetails") SaveImageRequestDTO saveImageRequestDTO,
                                                       HttpServletRequest request) throws GeneralSecurityException, IOException, InterruptedException {
        final User user = getUser(request);
        imageService.storeImage(saveImageRequestDTO.getDiaryId(), Arrays.asList(multipartFiles), user, saveImageRequestDTO);
        return ResponseEntity.ok().build();
    }



    /***
     * /image?year=2023&month=6&day=30
     * 하루 사진 받기
     */
    //@GetMapping("/image")
    public ResponseEntity<ShowImageOfDayDTO> showImageOfDay(final @RequestParam int year, final @RequestParam int month,
                                                                  final @RequestParam int day, final @AuthenticationPrincipal User user) {

        //return ResponseEntity.ok(imageService.getImages(year, month, day, user)); todo
        return ResponseEntity.ok(null);
    }

    /***
     *
     * /image?year=2023&month=7
     * 한 달의 사진 받기
     */
    //@GetMapping("/images")
    public ResponseEntity<List<HomeResponseDTO>> showThumbNailImages(final @RequestParam int year, final @RequestParam int month, final @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(dayImageService.getThumbNailImages(year, month, user));
    }

    /**
     * 사진 수정
     * @param file
     * @param imageId
     * @param user
     * @return
     */
    //@PatchMapping("/{imageId}")
    public ResponseEntity<StatusResponseDTO> updateImageFile(final @RequestPart MultipartFile file, final @PathVariable int imageId,
                                                             final @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(imageService.updateImage(imageId, file, user));
    }

    //@PostMapping("/{imageId}")
    public ResponseEntity<StatusResponseDTO> uploadDetailImages(final @RequestPart List<MultipartFile> files, final @PathVariable int imageId,
                                                                final @AuthenticationPrincipal User user) {

       //  return ResponseEntity.ok(imageService.uploadImageFile(files, imageId, user));
        return ResponseEntity.ok(null);
    }

    //@PostMapping("/{parentImageId}/detail")
    public ResponseEntity<StatusResponseDTO> updateImageDetail(final @PathVariable int parentImageId,
                                                               final @RequestBody UpdateImageDetailDTO updateImageDetailDTO,
                                                               final @AuthenticationPrincipal User user) {
        // return ResponseEntity.ok(imageService.updateImageDetail(parentImageId, user, updateImageDetailDTO));
        return ResponseEntity.ok(null);
    }

    private User getUser(HttpServletRequest request) throws GeneralSecurityException, IOException, InterruptedException {
        return userService.getValidUser(request.getHeader(LOGIN_FROM_KEY), request.getHeader(TOKEN_KEY));
    }

}
