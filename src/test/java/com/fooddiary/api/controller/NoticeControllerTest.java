package com.fooddiary.api.controller;

import static com.fooddiary.api.common.util.HttpUtil.makeHeader;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.dto.request.notice.NoticeGetListRequestDTO;
import com.fooddiary.api.dto.request.notice.NoticeModifyRequestDTO;
import com.fooddiary.api.dto.request.notice.NoticeNewRequestDTO;
import com.fooddiary.api.dto.response.notice.NoticeResponseDTO;
import com.fooddiary.api.service.notice.NoticeService;

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
    void getMoreNoticeList() throws Exception {
        final NoticeResponseDTO noticeResponseDTO = makeNoticeList();
        when(noticeService.getMoreNoticeList(any(NoticeGetListRequestDTO.class))).thenReturn(noticeResponseDTO);
        final String param = "?startId=0&size=10";
        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(get("/notice/more" + param)
                                                                                        .contentType(
                                                                                                MediaType.APPLICATION_JSON)
                                                                                        .headers(makeHeader()))
                                                                       .andExpect(status().isOk())
                                                                       .andDo(document("get more notice"))
                                                                       .andReturn()
                                                                       .getResponse();

        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Assertions.assertEquals(
                mockHttpServletResponse.getContentAsString(StandardCharsets.UTF_8),
                objectMapper.writeValueAsString(noticeResponseDTO));
    }

    @Test
    void newNotice() throws Exception {
        final NoticeNewRequestDTO noticeNewRequestDTO = new NoticeNewRequestDTO();
        noticeNewRequestDTO.setTitle("[공지]신규 기능 베타버전 출시");
        noticeNewRequestDTO.setContent("친구 추가하고 친구가 공유한 일기에 좋아요 및 댓글 기능이 추가되었습니다. 해당 기능은 실험실 메뉴에서 이용가능합니다.");
        noticeNewRequestDTO.setAvailable(true);
        noticeNewRequestDTO.setNoticeAt(LocalDate.now());

        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        mockMvc.perform(post("/notice/new")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(noticeNewRequestDTO))
                                .headers(makeHeader()))
               .andExpect(status().isOk())
               .andDo(document("new notice"));

        final ArgumentCaptor<NoticeNewRequestDTO> noticeNewRequestDTOArgumentCaptor = ArgumentCaptor.forClass(
                NoticeNewRequestDTO.class);

        verify(noticeService, times(1)).newNotice(noticeNewRequestDTOArgumentCaptor.capture());
        final NoticeNewRequestDTO requestDTOArgumentCaptorValue = noticeNewRequestDTOArgumentCaptor.getValue();
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
        noticeModifyRequestDTO.setNoticeAt(LocalDate.now());

        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        mockMvc.perform(put("/notice/modify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(noticeModifyRequestDTO))
                                .headers(makeHeader()))
               .andExpect(status().isOk())
               .andDo(document("modify notice"));

        final ArgumentCaptor<NoticeModifyRequestDTO> noticeNewRequestDTOArgumentCaptor =
                ArgumentCaptor.forClass(NoticeModifyRequestDTO.class);

        verify(noticeService, times(1)).modifyNotice(noticeNewRequestDTOArgumentCaptor.capture());
        final NoticeModifyRequestDTO requestDTOArgumentCaptorValue = noticeNewRequestDTOArgumentCaptor.getValue();
        Assertions.assertEquals(requestDTOArgumentCaptorValue.getId(), noticeModifyRequestDTO.getId());
        Assertions.assertEquals(requestDTOArgumentCaptorValue.getTitle(), noticeModifyRequestDTO.getTitle());
        Assertions.assertEquals(requestDTOArgumentCaptorValue.getContent(),
                                noticeModifyRequestDTO.getContent());
        Assertions.assertEquals(requestDTOArgumentCaptorValue.isAvailable(),
                                noticeModifyRequestDTO.isAvailable());
    }

    @Test
    void getPagingNotice() throws Exception {
        final NoticeResponseDTO noticeResponseDTO = makeNoticeList();
        final String title = noticeResponseDTO.getList().get(0).getTitle().substring(0, 2);
        final String param = "?page=0&size=10&title=" + title;
        when(noticeService.getPagingNoticeList(anyString(), any(), any(), any(), any(),
                                               any(Pageable.class))).thenReturn(noticeResponseDTO);
        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(get("/notice/paging" + param)
                                                                                        .contentType(
                                                                                                MediaType.APPLICATION_JSON)
                                                                                        .headers(makeHeader()))
                                                                       .andExpect(status().isOk())
                                                                       .andDo(document("get paging notice"))
                                                                       .andReturn()
                                                                       .getResponse();

        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Assertions.assertEquals(
                mockHttpServletResponse.getContentAsString(StandardCharsets.UTF_8),
                objectMapper.writeValueAsString(noticeResponseDTO));

        final ArgumentCaptor<String> noticeNewRequestDTOArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(noticeService, times(1)).getPagingNoticeList(noticeNewRequestDTOArgumentCaptor.capture(), any(),
                                                            any(), any(), any(), any(Pageable.class));
        Assertions.assertEquals(title, noticeNewRequestDTOArgumentCaptor.getValue());
    }

    @Test
    void deleteNotice() throws Exception {
        willDoNothing().given(noticeService).deleteNotice(eq(1));
        mockMvc.perform(delete("/notice/{noticeId}", 1)
                .contentType(MediaType.APPLICATION_JSON).headers(makeHeader()))
                .andExpect(status().isOk())
                .andDo(document("delete notice"));
    }

    private static NoticeResponseDTO makeNoticeList() {
        final List<NoticeResponseDTO.NoticeDTO> noticeList = new ArrayList<>();
        final NoticeResponseDTO noticeResponseDTO = new NoticeResponseDTO();
        NoticeResponseDTO.NoticeDTO notice = new NoticeResponseDTO.NoticeDTO();
        notice.setId(1);
        notice.setTitle("[공지]신규 서비스 오픈 안내");
        notice.setContent("2024년 1월 2일 신규 서비스 오픈 되었습니다. 앞으로 새로운 기능도 추가될 예정이니 기대해주세요");
        notice.setNoticeAt(LocalDate.of(2024, 1, 2));
        noticeList.add(notice);

        notice = new NoticeResponseDTO.NoticeDTO();
        notice.setId(2);
        notice.setTitle("[공지]신규 서비스 관련 이벤트 당첨 안내");
        notice.setContent("이벤트 응모관련 당첨자는 이메일 확인 부탁드립니다.");
        notice.setNoticeAt(LocalDate.now().minusDays(2));
        noticeList.add(notice);
        noticeResponseDTO.setList(noticeList);

        return noticeResponseDTO;
    }

}
