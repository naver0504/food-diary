package com.fooddiary.api.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fooddiary.api.dto.request.NoticeGetListRequestDTO;
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
    public ResponseEntity<List<NoticeResponseDTO>> getMoreNoticeList(
            NoticeGetListRequestDTO noticeGetListRequestDTO) {
        return ResponseEntity.ok(noticeService.getMoreNoticeList(noticeGetListRequestDTO));
    }

    @GetMapping("/paging")
    public ResponseEntity<List<NoticeResponseDTO>> getPagingNoticeList(@RequestParam("page") int page,
                                                                       @RequestParam("size") int size) {
        return ResponseEntity.ok(noticeService.getPagingNoticeList(PageRequest.of(page, size)));
    }

    @GetMapping("/detail")
    public ResponseEntity<NoticeResponseDTO> getMoreNotice(@RequestParam("id") int id) {
        return ResponseEntity.ok(noticeService.getDetailNotice(id));
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


