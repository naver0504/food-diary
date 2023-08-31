package com.fooddiary.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
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
import com.fooddiary.api.dto.response.NoticeResponseDTO;
import com.fooddiary.api.service.NoticeService;

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
    void getNotice() throws Exception {
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
        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(get("/notice/get")
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
}
