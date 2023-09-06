package com.fooddiary.api.controller;

import java.util.List;

import com.fooddiary.api.dto.request.NoticeGetListRequestDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fooddiary.api.dto.request.NoticeModifyRequestDTO;
import com.fooddiary.api.dto.request.NoticeNewRequestDTO;
import com.fooddiary.api.dto.response.NoticeResponseDTO;
import com.fooddiary.api.service.NoticeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    @GetMapping("/more")
    public ResponseEntity<List<NoticeResponseDTO>> getMoreList(NoticeGetListRequestDTO noticeGetListRequestDTO) {
        return ResponseEntity.ok(noticeService.getMoreList(noticeGetListRequestDTO));
    }

    @GetMapping("/paging")
    public ResponseEntity<List<NoticeResponseDTO>> getPagingNoticeList(Pageable pageable) {
        return ResponseEntity.ok(noticeService.getPagingNoticeList(pageable));
    }

    @PostMapping("/new")
    public ResponseEntity<Void> newNotice(@RequestBody NoticeNewRequestDTO noticeNewRequestDTO) {
        noticeService.newNotice(noticeNewRequestDTO);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/modify")
    public ResponseEntity<Void> modifyNotice(@RequestBody NoticeModifyRequestDTO noticeModifyRequestDTO) {
        noticeService.modifyNotice(noticeModifyRequestDTO);
        return ResponseEntity.ok().build();
    }
}


