package com.fooddiary.api.controller;

import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.dto.request.diary.DiaryMemoRequestDTO;
import com.fooddiary.api.dto.request.diary.NewDiaryRequestDTO;
import com.fooddiary.api.dto.response.diary.HomeResponseDTO;
import com.fooddiary.api.dto.response.diary.DiaryDetailResponseDTO;
import com.fooddiary.api.dto.response.diary.HomeDayResponseDTO;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.diary.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /**
     * 한 장의 사진을 받아서 일기를 등록합니다. 우선 사진만 등록됩니다.
     * @param images 사진들
     * @param newDiaryRequestDTO 위치 정보를 선택적으로 받게 합니다. 사진에 다 넣어줘야 합니다. json array로 받습니다.
     * @param user
     * @return void ok응답으로 처리
     */
    @PostMapping(value = "/new", consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> createDiary(final @RequestPart("images") List<MultipartFile> images,
                                               final @RequestParam("createTime") LocalDateTime createTime,
                                               final @RequestPart(value = "imageInfo", required = false) List<NewDiaryRequestDTO> newDiaryRequestDTO,
                                                         final @AuthenticationPrincipal User user) {
        if (images.size() > 5) {
            throw new BizException("we allow max 5 images");
        }
        diaryService.createDiary(images, createTime, newDiaryRequestDTO, user);
        return ResponseEntity.ok().build();
    }

    /**
     * 일기에 사진을 추가합니다.
     * @param diaryId 일기 아이디
     * @param files 사진 파일들
     * @param newDiaryRequestDTOList 위치를 저장하기 위한 추가 정보. 사진에 다 넣어줘야 합니다. json array로 받습니다.
     * @param user spring context에서 관리되는 사용자 정보
     * @return
     */
    @PostMapping(value = "/{diaryId}/images", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> addImages(@PathVariable("diaryId") Integer diaryId, final @RequestPart("files") List<MultipartFile> files,
                                            final @RequestPart(value = "imageInfo", required = false) List<NewDiaryRequestDTO> newDiaryRequestDTOList,
                                            final @AuthenticationPrincipal User user) {
        if (files.size() > 5) {
            throw new BizException("we allow max 5 images");
        }
        diaryService.addImages(diaryId, files, newDiaryRequestDTOList, user);
        return ResponseEntity.ok().build();
    }

    /**
     * 사진 수정
     * @param file 신규 사진
     * @param imageId 수정할 사진 id
     * @param user
     * @return
     */
    @PatchMapping("/image/{imageId}")
    public ResponseEntity<Void> updateImageFile(final @RequestPart MultipartFile file, final @PathVariable("imageId") int imageId,
                                                final @RequestPart(value = "imageInfo", required = false) NewDiaryRequestDTO newDiaryRequestDTO,
                                                final @AuthenticationPrincipal User user) {
        diaryService.updateImage(imageId, file, user, newDiaryRequestDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * 일기 자세히 보기
     * @param diaryId
     * @param user
     * @return
     */
    @GetMapping("/{diaryId}")
    public ResponseEntity<DiaryDetailResponseDTO> getDiaryDetail(@PathVariable("diaryId") final int diaryId, final @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(diaryService.getDiaryDetail(diaryId, user));
    }

    /**
     * 일기에 메모, 태그를 수정합니다.
     */
    @PutMapping("/{diaryId}/memo")
    public ResponseEntity<Void> updateMemo(@PathVariable("diaryId") Integer diaryId, @RequestBody DiaryMemoRequestDTO diaryMemoRequestDTO, final @AuthenticationPrincipal User user) {
        diaryService.updateMemo(diaryId, diaryMemoRequestDTO, user);
        return ResponseEntity.ok().build();
    }

    /**
     * 홈화면 1달치 사진 보여주기
     * @param year
     * @param month
     * @param user
     * @return HomeResponseDTO 썸네일 이미지를 보여줍니다. id는 일기 id입니다.
     * @throws IOException
     */
    @GetMapping("/home")
    public ResponseEntity<List<HomeResponseDTO>> home(final @RequestParam int year, final @RequestParam int month, final @AuthenticationPrincipal User user) throws IOException {
        return ResponseEntity.ok(diaryService.getHome(year, month, user));
    }

    /**
     * 하루치 일기들을 보여주기
     * @param year
     * @param month
     * @param day
     * @param user
     * @return HomeDayResponseDTO 이전 일기 날짜,다음 일기 날짜가 포함된 오늘 날짜의 일기 데이터
     */
    @GetMapping("/home-day")
    public ResponseEntity<HomeDayResponseDTO> homeDay(final @RequestParam int year, final @RequestParam int month, final @RequestParam int day, final @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(diaryService.getHomeDay(year, month, day, user));
    }
}
