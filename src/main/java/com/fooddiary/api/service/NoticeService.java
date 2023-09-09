package com.fooddiary.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fooddiary.api.dto.request.NoticeGetListRequestDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
    private final EntityManager entityManager;

    public NoticeResponseDTO getMoreNoticeList(NoticeGetListRequestDTO noticeGetListRequestDTO) {
        NoticeResponseDTO noticeResponseDTO = new NoticeResponseDTO();
        noticeResponseDTO.setCount(noticeRepository.count());

        if (noticeResponseDTO.getCount() > 0L) {
            final Pageable pageable = PageRequest.of(0, noticeGetListRequestDTO.getSize());
            final List<Notice> noticeList = noticeRepository.selectMoreNoticeListById(noticeGetListRequestDTO.getStartId(), true, pageable);

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

    public NoticeResponseDTO getPagingNoticeList(Pageable pageable) {
        NoticeResponseDTO noticeResponseDTO = new NoticeResponseDTO();
        noticeResponseDTO.setCount(noticeRepository.count());

        if (noticeResponseDTO.getCount() > 0L) {
            final List<Notice> noticeList = noticeRepository.selectPagingNoticeListById(pageable);

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

    // todo - jpql 동적쿼리 생성
    private List<Notice> setCondition(String title, String content, Boolean available, LocalDate noticeAt, Pageable pageable) {
        String jpql = "select n from Notice n";
        String order = " order by n.id desc";
        String where = " where";
        List<String> whereCondition = new LinkedList<>();

        // string 동적생성
        if (title != null) {
            whereCondition.add(" n.title like concat('%',:title,'%')");
        }

        jpql += String.join(" and ", whereCondition) + order;

        TypedQuery<Notice> jpaQuery = entityManager.createQuery(jpql, Notice.class);
        // param 동적 생성
        if (title != null) {
            jpaQuery.setParameter("title", title);
        }

        return jpaQuery.setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize() + 1)
                .getResultList();
    }
}
