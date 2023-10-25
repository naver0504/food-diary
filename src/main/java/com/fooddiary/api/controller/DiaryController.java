package com.fooddiary.api.controller;

import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.dto.request.diary.DiaryMemoRequestDTO;
import com.fooddiary.api.dto.request.diary.PlaceInfoDTO;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /**
     * 한 장의 사진을 받아서 일기를 등록합니다. 우선 사진만 등록됩니다.
     * @param images 사진들
     * @param placeInfoDTO 위치 정보를 선택적으로 받게 합니다.
     * @param user 스프링 thread-local SecurityContext에 저장된 사용자 정보
     * @return void ok응답으로 처리
     */
    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createDiary(@RequestPart("files") final List<MultipartFile> images,
                                            @RequestParam("createTime") final LocalDate createDate,
                                            @RequestPart("placeInfo") final PlaceInfoDTO placeInfoDTO,
                                            @AuthenticationPrincipal final User user) {
        if (images.size() > 5) {
            throw new BizException("we allow max 5 images");
        }
        diaryService.createDiary(images, createDate, placeInfoDTO, user);
        return ResponseEntity.ok().build();
    }

    /**
     * 일기에 사진을 추가합니다.
     * @param diaryId 일기 아이디
     * @param files 사진 파일들
     * @param user spring context에서 관리되는 사용자 정보
     * @return status 200
     */
    @PostMapping(value = "/{diaryId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> addImages(@PathVariable("diaryId") long diaryId, @RequestPart("files")
    final List<MultipartFile> files, @AuthenticationPrincipal final User user) {
        if (files.size() > 5) {
            throw new BizException("we allow max 5 images");
        }
        diaryService.addImages(diaryId, files, user);
        return ResponseEntity.ok().build();
    }

    /**
     * 사진 수정
     * @param file 신규 사진
     * @param imageId 수정할 사진 id
     * @param user spring context에서 관리되는 사용자 정보
     * @return status 200
     */
    @PatchMapping("/image/{imageId}")
    public ResponseEntity<Void> updateImage(@RequestPart final MultipartFile file, @PathVariable("imageId")
    final int imageId, @AuthenticationPrincipal final User user) {
        diaryService.updateImage(imageId, file, user);
        return ResponseEntity.ok().build();
    }

    /**
     * 일기 자세히 보기
     * @param diaryId 일기 id
     * @param user spring context에서 관리되는 사용자 정보
     * @return status 200
     */
    @GetMapping("/{diaryId}")
    public ResponseEntity<DiaryDetailResponseDTO> getDiaryDetail(@PathVariable("diaryId") final long diaryId,
                                                                 @AuthenticationPrincipal final User user) {
        return ResponseEntity.ok(diaryService.getDiaryDetail(diaryId, user));
    }

    /**
     * 일기에 메모, 태그를 수정합니다.
     */
    @PutMapping("/{diaryId}/memo")
    public ResponseEntity<Void> updateMemo(@PathVariable("diaryId") final long diaryId, @RequestBody DiaryMemoRequestDTO diaryMemoRequestDTO) {
        diaryService.updateMemo(diaryId, diaryMemoRequestDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * 일기 삭제
     * @param diaryId 해당 일기의 id
     * @param user 스프링 thread-local SecurityContext에 저장된 사용자 정보
     * @return 성공하면 ok
     */
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> deleteDiary(@PathVariable("diaryId") final long diaryId, final @AuthenticationPrincipal User user) {
        diaryService.deleteDiary(diaryId, user);
        return ResponseEntity.ok().build();
    }

    /**
     * 홈화면 1달치 사진 보여주기
     * @param yearMonth yyyy-mm
     * @param user 스프링 thread-local SecurityContext에 저장된 사용자 정보
     * @return HomeResponseDTO 썸네일 이미지를 보여줍니다. id는 일기 id입니다.
     * @throws IOException aws쪽에 장애가 생길 경우
     */
    @GetMapping("/home")
    public ResponseEntity<List<HomeResponseDTO>> home(@RequestParam final YearMonth yearMonth,
                                                      @AuthenticationPrincipal final User user) throws IOException {
        return ResponseEntity.ok(diaryService.getHome(yearMonth, user));
    }

    /**
     * 하루치 일기들을 보여주기
     * @param date yyyy-mm-dd
     * @param user 스프링 thread-local SecurityContext에 저장된 사용자 정보
     * @return HomeDayResponseDTO 이전 일기 날짜,다음 일기 날짜가 포함된 오늘 날짜의 일기 데이터
     */
    @GetMapping("/home-day")
    public ResponseEntity<HomeDayResponseDTO> homeDay(@RequestParam final LocalDate date,
                                                      @AuthenticationPrincipal final User user) {
        return ResponseEntity.ok(diaryService.getHomeDay(date, user));
    }
}
