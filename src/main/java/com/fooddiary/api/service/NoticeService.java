package com.fooddiary.api.service;

import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.dto.request.NoticeGetListRequestDTO;
import com.fooddiary.api.dto.request.NoticeModifyRequestDTO;
import com.fooddiary.api.dto.request.NoticeNewRequestDTO;
import com.fooddiary.api.dto.response.NoticeResponseDTO;
import com.fooddiary.api.entity.notice.Notice;
import com.fooddiary.api.entity.user.Role;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.NoticeQuerydslRepository;
import com.fooddiary.api.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeQuerydslRepository noticeQuerydslRepository;

    public NoticeResponseDTO getMoreNoticeList(NoticeGetListRequestDTO noticeGetListRequestDTO) {
        final NoticeResponseDTO noticeResponseDTO = new NoticeResponseDTO();
        noticeResponseDTO.setCount(noticeRepository.count());

        if (noticeResponseDTO.getCount() > 0L) {
            final Pageable pageable = PageRequest.of(0, noticeGetListRequestDTO.getSize());
            final List<Notice> noticeList = noticeRepository.selectMoreNoticeListById(
                    noticeGetListRequestDTO.getStartId(), true, pageable);

            final List<NoticeResponseDTO.NoticeDTO> noticeResponseDTOList = new ArrayList<>();
            noticeList.forEach(element -> {
                final NoticeResponseDTO.NoticeDTO noticeDTO = new NoticeResponseDTO.NoticeDTO();
                BeanUtils.copyProperties(element, noticeDTO);
                noticeResponseDTOList.add(noticeDTO);
            });
            noticeResponseDTO.setList(noticeResponseDTOList);
        }
        return noticeResponseDTO;
    }

    public NoticeResponseDTO getPagingNoticeList(String title, String content, Boolean available,
                                                 LocalDate noticeAtStart, LocalDate noticeAtEnd, Pageable pageable) {
        final NoticeResponseDTO noticeResponseDTO = new NoticeResponseDTO();
        noticeResponseDTO.setCount(
                noticeQuerydslRepository.selectCount(title, content, available, noticeAtStart, noticeAtEnd));

        if (noticeResponseDTO.getCount() > 0L) {

            final List<Notice> noticeList = noticeQuerydslRepository.selectList(title, content,
                                                                                        available, noticeAtStart, noticeAtEnd,
                                                                                        pageable);

            final List<NoticeResponseDTO.NoticeDTO> noticeResponseDTOList = new ArrayList<>();
            noticeList.forEach(element -> {
                final NoticeResponseDTO.NoticeDTO noticeDTO = new NoticeResponseDTO.NoticeDTO();
                BeanUtils.copyProperties(element, noticeDTO);
                noticeResponseDTOList.add(noticeDTO);
            });
            noticeResponseDTO.setList(noticeResponseDTOList);
        }
        return noticeResponseDTO;
    }

    public NoticeResponseDTO.NoticeDTO getDetailNotice(int id) {
        final Notice notice = noticeRepository.findById(id).orElse(new Notice());
        final NoticeResponseDTO.NoticeDTO noticeResponseDTO = new NoticeResponseDTO.NoticeDTO();
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
        final Notice notice = noticeRepository.findById(noticeModifyRequestDTO.getId()).orElse(new Notice());
        if (notice.getId() == null) {
            throw new BizException("invalid id");
        }
        BeanUtils.copyProperties(noticeModifyRequestDTO, notice);
        notice.setUpdateAt(LocalDateTime.now());
        notice.setUpdateUserId(user.getId());
        noticeRepository.save(notice);
    }

}
