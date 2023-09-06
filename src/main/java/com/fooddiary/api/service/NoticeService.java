package com.fooddiary.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fooddiary.api.dto.request.NoticeGetListRequestDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.dto.request.NoticeModifyRequestDTO;
import com.fooddiary.api.dto.request.NoticeNewRequestDTO;
import com.fooddiary.api.dto.response.NoticeResponseDTO;
import com.fooddiary.api.entity.notice.Notice;
import com.fooddiary.api.entity.user.Role;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    public List<NoticeResponseDTO> getMoreList(NoticeGetListRequestDTO noticeGetListRequestDTO) {
        Pageable pageable = PageRequest.of(0, noticeGetListRequestDTO.getSize());
        final List<Notice> boardList = noticeRepository.selectMoreNoticeListById(noticeGetListRequestDTO.getStartId(), true, pageable);

        final List<NoticeResponseDTO> noticeResponseDTOList = new ArrayList<>();
        boardList.forEach(element -> {
            final NoticeResponseDTO noticeResponseDTO = new NoticeResponseDTO();
            BeanUtils.copyProperties(element, noticeResponseDTO);
            noticeResponseDTOList.add(noticeResponseDTO);
        });
        return noticeResponseDTOList;
    }

    public List<NoticeResponseDTO> getPagingNoticeList(Pageable pageable) {
        final List<Notice> boardList = noticeRepository.selectPagingNoticeListById(true, pageable);

        final List<NoticeResponseDTO> noticeResponseDTOList = new ArrayList<>();
        boardList.forEach(element -> {
            final NoticeResponseDTO noticeResponseDTO = new NoticeResponseDTO();
            BeanUtils.copyProperties(element, noticeResponseDTO);
            noticeResponseDTOList.add(noticeResponseDTO);
        });
        return noticeResponseDTOList;
    }

    public void newNotice(NoticeNewRequestDTO noticeNewRequestDTO) {
        final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole() != Role.ADMIN) {
            throw new BizException("not admin");
        }
        final Notice notice = new Notice();
        BeanUtils.copyProperties(noticeNewRequestDTO, notice);
        notice.setCreateUserId(user.getId());
        noticeRepository.save(notice);
    }

    public void modifyNotice(NoticeModifyRequestDTO noticeModifyRequestDTO) {
        final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole() != Role.ADMIN) {
            throw new BizException("not admin");
        }
        if (noticeRepository.getReferenceById(noticeModifyRequestDTO.getId()).getId() == null) {
            throw new BizException("invalid id");
        }
        final Notice notice = new Notice();
        BeanUtils.copyProperties(noticeModifyRequestDTO, notice);
        notice.setUpdateAt(LocalDateTime.now());
        notice.setUpdateUserId(user.getId());
        noticeRepository.save(notice);
    }
}
