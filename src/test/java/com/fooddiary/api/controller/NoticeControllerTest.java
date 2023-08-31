package com.fooddiary.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.dto.request.NoticeModifyRequestDTO;
import com.fooddiary.api.dto.request.NoticeNewRequestDTO;
import com.fooddiary.api.dto.response.NoticeResponseDTO;
import com.fooddiary.api.service.NoticeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles(Profiles.TEST)
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
class NoticeControllerTest {
    @MockBean
    private Interceptor interceptor;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;
    @MockBean
    private NoticeService noticeService;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                                 .apply(documentationConfiguration(restDocumentation)).build();
        given(interceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    void getNoticeList() throws Exception {
        final Pageable pageRequest = PageRequest.of(0, 10);
        final List<NoticeResponseDTO> noticeList = new ArrayList<>();
        NoticeResponseDTO notice = new NoticeResponseDTO();
        notice.setId(1);
        notice.setTitle("[공지]신규 서비스 오픈 안내");
        notice.setContent("2024년 1월 2일 신규 서비스 오픈 되었습니다. 앞으로 새로운 기능도 추가될 예정이니 기대해주세요");
        notice.setCreateAt(LocalDate.of(2024, 1, 2));
        noticeList.add(notice);

        notice = new NoticeResponseDTO();
        notice.setId(2);
        notice.setTitle("[공지]신규 서비스 관련 이벤트 당첨 안내");
        notice.setContent("이벤트 응모관련 당첨자는 이메일 확인 부탁드립니다.");
        notice.setCreateAt(LocalDate.now().minusDays(2));
        noticeList.add(notice);

        when(noticeService.getNoticeList(any(Pageable.class))).thenReturn(noticeList);
        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(get("/notice/list")
                                                                                  .contentType(
                                                                                          MediaType.APPLICATION_JSON))
                                                                 .andExpect(status().isOk())
                                                                 .andDo(document("get notice")).andReturn()
                                                                 .getResponse();

        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Assertions.assertEquals(
                mockHttpServletResponse.getContentAsString(StandardCharsets.UTF_8),
                objectMapper.writeValueAsString(noticeList));
    }

    @Test
    void newNotice() throws Exception {
        final NoticeNewRequestDTO noticeNewRequestDTO = new NoticeNewRequestDTO();
        noticeNewRequestDTO.setTitle("[공지]신규 기능 베타버전 출시");
        noticeNewRequestDTO.setContent("친구 추가하고 친구가 공유한 일기에 좋아요 및 댓글 기능이 추가되었습니다. 해당 기능은 실험실 메뉴에서 이용가능합니다.");
        noticeNewRequestDTO.setAvailable(true);

        final ObjectMapper objectMapper = new ObjectMapper();
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "jasuil@daum.net");
        httpHeaders.add("token", "asdf");

        mockMvc.perform(post("/notice/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noticeNewRequestDTO))
                        .headers(httpHeaders))
                .andExpect(status().isOk())
                .andDo(document("new notice"));

        final ArgumentCaptor<NoticeNewRequestDTO> noticeNewRequestDTOArgumentCaptor = ArgumentCaptor.forClass(NoticeNewRequestDTO.class);

        verify(noticeService, times(1)).newNotice(noticeNewRequestDTOArgumentCaptor.capture());
        NoticeNewRequestDTO requestDTOArgumentCaptorValue = noticeNewRequestDTOArgumentCaptor.getValue();
        Assertions.assertEquals(requestDTOArgumentCaptorValue.getTitle(), noticeNewRequestDTO.getTitle());
        Assertions.assertEquals(requestDTOArgumentCaptorValue.getContent(), noticeNewRequestDTO.getContent());
        Assertions.assertEquals(requestDTOArgumentCaptorValue.isAvailable(), noticeNewRequestDTO.isAvailable());
    }

    @Test
    void modifyNotice() throws Exception {
        final NoticeModifyRequestDTO noticeModifyRequestDTO = new NoticeModifyRequestDTO();
        noticeModifyRequestDTO.setId(1);
        noticeModifyRequestDTO.setTitle("[공지]신규 기능 베타버전 출시");
        noticeModifyRequestDTO.setContent("친구 추가하고 친구가 공유한 일기에 좋아요 및 댓글 기능이 추가되었습니다. 해당 기능은 실험실 메뉴에서 이용가능합니다.");
        noticeModifyRequestDTO.setAvailable(true);

        final ObjectMapper objectMapper = new ObjectMapper();
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "jasuil@daum.net");
        httpHeaders.add("token", "asdf");

        mockMvc.perform(put("/notice/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noticeModifyRequestDTO))
                        .headers(httpHeaders))
                .andExpect(status().isOk())
                .andDo(document("modify notice"));

        final ArgumentCaptor<NoticeModifyRequestDTO> noticeNewRequestDTOArgumentCaptor = ArgumentCaptor.forClass(NoticeModifyRequestDTO.class);

        verify(noticeService, times(1)).modifyNotice(noticeNewRequestDTOArgumentCaptor.capture());
        NoticeModifyRequestDTO requestDTOArgumentCaptorValue = noticeNewRequestDTOArgumentCaptor.getValue();
        Assertions.assertEquals(requestDTOArgumentCaptorValue.getId(), noticeModifyRequestDTO.getId());
        Assertions.assertEquals(requestDTOArgumentCaptorValue.getTitle(), noticeModifyRequestDTO.getTitle());
        Assertions.assertEquals(requestDTOArgumentCaptorValue.getContent(), noticeModifyRequestDTO.getContent());
        Assertions.assertEquals(requestDTOArgumentCaptorValue.isAvailable(), noticeModifyRequestDTO.isAvailable());
    }
}
