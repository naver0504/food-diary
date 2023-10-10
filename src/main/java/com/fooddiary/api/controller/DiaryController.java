package com.fooddiary.api.controller;

import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.dto.request.diary.NewDiaryRequestDTO;
import com.fooddiary.api.dto.response.ThumbNailImagesDTO;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /**
     * 한 장의 사진을 받아서 일기를 등록합니다. 우선 사진만 등록됩니다.
     * @param newDiaryRequestDTO
     * @param user
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws InterruptedException
     */
    @PostMapping(value = "/new", consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> createDiary(final @RequestPart("images") List<MultipartFile> images,
                                               final @RequestPart("imageInfo") NewDiaryRequestDTO newDiaryRequestDTO,
                                                         final @AuthenticationPrincipal User user) {
        if (images.size() > 5) {
            throw new BizException("we allow max 5 images");
        }
        diaryService.createDiary(images, newDiaryRequestDTO, user);
        return ResponseEntity.ok().build();
    }

    /**
     * 일기에 사진을 추가합니다.
     * @param images
     * @param newDiaryRequestDTO
     * @param user
     * @return
     */
    @PostMapping(value = "/{diaryId}/images", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> addImages(@PathVariable("diaryId") Integer diaryId, final @RequestPart("images") List<MultipartFile> images,
                                            final @RequestPart("imageInfo") NewDiaryRequestDTO newDiaryRequestDTO,
                                            final @AuthenticationPrincipal User user) {
        if (images.size() > 5) {
            throw new BizException("we allow max 5 images");
        }
        diaryService.addImages(diaryId, images, newDiaryRequestDTO, user);
        return ResponseEntity.ok().build();
    }

    /**
     * 홈화면 1달치 사진 보여주기
     * @param year
     * @param month
     * @param user
     * @return
     * @throws IOException
     */
    @GetMapping("/home")
    public ResponseEntity<List<ThumbNailImagesDTO>> home(final @RequestParam int year, final @RequestParam int month, final @AuthenticationPrincipal User user) throws IOException {
        return ResponseEntity.ok(diaryService.getMonthlyImages(year, month, user));
    }

}
