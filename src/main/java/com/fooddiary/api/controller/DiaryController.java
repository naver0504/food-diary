package com.fooddiary.api.controller;

import com.fooddiary.api.dto.request.SaveImageRequestDTO;
import com.fooddiary.api.dto.request.diary.NewDiaryRequestDTO;
import com.fooddiary.api.dto.response.StatusResponseDTO;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.DiaryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
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
    public ResponseEntity<Void> createDiary(final @RequestPart("files") List<MultipartFile> images,
                                               final @RequestPart("imageDetail") NewDiaryRequestDTO newDiaryRequestDTO,
                                                         final @AuthenticationPrincipal User user) {
        diaryService.createDiary(images, newDiaryRequestDTO, user);
        return ResponseEntity.ok().build();
    }


}
