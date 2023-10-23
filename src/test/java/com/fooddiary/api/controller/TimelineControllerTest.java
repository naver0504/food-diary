package com.fooddiary.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.filter.LoggingFilter;
import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.dto.response.timeline.TimeLineResponseDTO;
import com.fooddiary.api.dto.response.timeline.TimelineDiaryDTO;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.timeline.TimelineService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {TimelineController.class})
@ActiveProfiles(Profiles.TEST)
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@EnableAutoConfiguration(exclude = {JpaRepositoriesAutoConfiguration.class})
public class TimelineControllerTest {

    @Autowired
    WebApplicationContext context;
    MockMvc mockMvc;
    static User principal;
    @MockBean
    TimelineService timelineService;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)).build();

        principal = new User();
        principal.setEmail("test@test.com");
        principal.setPw("1234");
        principal.setId(1);

        Authentication authentication = mock(RememberMeAuthenticationToken.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void timelineShow() throws Exception {
        List<TimeLineResponseDTO> timeLineResponseDTOList = new ArrayList<>();
        TimeLineResponseDTO timeLineResponseDTO = new TimeLineResponseDTO();
        timeLineResponseDTO.setDate(LocalDate.now());

        List<TimelineDiaryDTO> timelineDiaryDTOList = new ArrayList<>();
        TimelineDiaryDTO timelineDiaryDTO = new TimelineDiaryDTO();
        timelineDiaryDTO.setDiaryId(1);
        timelineDiaryDTO.setBytes(new byte[]{'d','i','a','r','y'});
        timelineDiaryDTOList.add(timelineDiaryDTO);
        timeLineResponseDTO.setDiaryList(timelineDiaryDTOList);

        timeLineResponseDTOList.add(timeLineResponseDTO);

        when(timelineService.getTimeline(any(LocalDate.class), any(User.class))).thenReturn(timeLineResponseDTOList);

        final String date = LocalDate.now().toString();
        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(get("/timeline/show")
                        .param("date", date))
                .andExpect(status().isOk())
                .andDo(document("show timeline"))
                .andReturn()
                .getResponse();

        ArgumentCaptor<LocalDate> requestDate = ArgumentCaptor.forClass(LocalDate.class);
        then(timelineService).should(times(1)).getTimeline(requestDate.capture(), any(User.class));

        Assertions.assertEquals(requestDate.getValue().toString(), date);

        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Assertions.assertEquals(
                mockHttpServletResponse.getContentAsString(StandardCharsets.UTF_8),
                objectMapper.writeValueAsString(timeLineResponseDTOList));
    }

    @Test
    void showMoreDiary() throws Exception {
        List<TimelineDiaryDTO> timelineDiaryDTOList = new ArrayList<>();
        TimelineDiaryDTO timelineDiaryDTO = new TimelineDiaryDTO();
        timelineDiaryDTO.setDiaryId(1);
        timelineDiaryDTO.setBytes(new byte[]{'d','i','a','r','y'});
        timelineDiaryDTOList.add(timelineDiaryDTO);

        when(timelineService.getMoreDiary(any(LocalDate.class), any(Integer.class), any(User.class))).thenReturn(timelineDiaryDTOList);
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.set("date", LocalDate.now().toString());
        multiValueMap.set("offset", "1");

        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(get("/timeline/show/more-diary")
                        .params(multiValueMap))
                .andExpect(status().isOk())
                .andDo(document("show more diary"))
                .andReturn()
                .getResponse();

        ArgumentCaptor<LocalDate> requestDate = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<Integer> requestStartId = ArgumentCaptor.forClass(Integer.class);
        then(timelineService).should(times(1)).getMoreDiary(requestDate.capture(), requestStartId.capture(), any(User.class));

        Assertions.assertEquals(requestDate.getValue().toString(), Objects.requireNonNull(multiValueMap.get("date")).get(0));
        Assertions.assertEquals(requestStartId.getValue().toString(), Objects.requireNonNull(multiValueMap.get("offset")).get(0));

        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Assertions.assertEquals(
                mockHttpServletResponse.getContentAsString(StandardCharsets.UTF_8),
                objectMapper.writeValueAsString(timelineDiaryDTOList));
    }
}
