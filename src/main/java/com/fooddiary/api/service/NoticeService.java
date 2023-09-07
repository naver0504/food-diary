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
    public List<NoticeResponseDTO> getMoreNoticeList(NoticeGetListRequestDTO noticeGetListRequestDTO) {
        final Pageable pageable = PageRequest.of(0, noticeGetListRequestDTO.getSize());
        final List<Notice> noticeList = noticeRepository.selectMoreNoticeListById(noticeGetListRequestDTO.getStartId(), true, pageable);

        final List<NoticeResponseDTO> noticeResponseDTOList = new ArrayList<>();
        noticeList.forEach(element -> {
            final NoticeResponseDTO noticeResponseDTO = new NoticeResponseDTO();
            BeanUtils.copyProperties(element, noticeResponseDTO);
            noticeResponseDTOList.add(noticeResponseDTO);
        });
        return noticeResponseDTOList;
    }

    public List<NoticeResponseDTO> getPagingNoticeList(Pageable pageable) {
        final List<Notice> noticeList = noticeRepository.selectPagingNoticeListById(pageable);

        final List<NoticeResponseDTO> noticeResponseDTOList = new ArrayList<>();
        noticeList.forEach(element -> {
            final NoticeResponseDTO noticeResponseDTO = new NoticeResponseDTO();
            BeanUtils.copyProperties(element, noticeResponseDTO);
            noticeResponseDTOList.add(noticeResponseDTO);
        });
        return noticeResponseDTOList;
    }

    public NoticeResponseDTO getDetailNotice(int id) {
        final Notice notice = noticeRepository.findById(id).orElse(new Notice());
        final NoticeResponseDTO noticeResponseDTO = new NoticeResponseDTO();
        BeanUtils.copyProperties(notice, noticeResponseDTO);
        return noticeResponseDTO;
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
        Notice notice = noticeRepository.findById(noticeModifyRequestDTO.getId()).orElse(new Notice());
        if (notice.getId() == null) {
            throw new BizException("invalid id");
        }
        BeanUtils.copyProperties(noticeModifyRequestDTO, notice);
        notice.setUpdateAt(LocalDateTime.now());
        notice.setUpdateUserId(user.getId());
        noticeRepository.save(notice);
    }
}
