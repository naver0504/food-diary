package com.fooddiary.api.service;

import java.util.ArrayList;
import java.util.List;

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
    public List<NoticeResponseDTO> getNoticeList(Pageable pageable) {
        final int startId = (int) pageable.getOffset();
        pageable = PageRequest.of(0, pageable.getPageSize());
        final List<Notice> boardList = noticeRepository.selectgetNoticeListByIdPaging(startId, true, pageable);

        final List<NoticeResponseDTO> noticeResponseDTOList = new ArrayList<>();
        boardList.forEach(element -> {
            final NoticeResponseDTO noticeResponseDTO = new NoticeResponseDTO();
            BeanUtils.copyProperties(noticeResponseDTO, element);
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
        noticeRepository.save(notice);
    }
}
