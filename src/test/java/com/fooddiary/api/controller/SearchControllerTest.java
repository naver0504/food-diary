package com.fooddiary.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.dto.response.search.DiarySearchResponseDTO;
import com.fooddiary.api.dto.response.timeline.TimelineDiaryDTO;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.search.SearchService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fooddiary.api.common.util.HttpUtil.makeHeader;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles(Profiles.TEST)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class SearchControllerTest {

    @MockBean
    Interceptor interceptor;
    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    static User principal;

    @MockBean
    SearchService searchService;

    final static byte[] BYTES = new byte[]{'t', 'e', 's', 't'};


    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {

        principal = new User();
        principal.setEmail("test@test.com");
        principal.setPw("1234");
        principal.setId(1);

        final Authentication authentication = mock(RememberMeAuthenticationToken.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)).build();

        given(interceptor.preHandle(any(), any(), any())).willReturn(true);
    }


    @Test
    void getSearchResultWithoutCondition() throws Exception {
        final List<DiarySearchResponseDTO> responseDTOList = new ArrayList<>();
        final DiarySearchResponseDTO firstResponseDTO = new DiarySearchResponseDTO();
        firstResponseDTO.setCategoryName("#고기");
        firstResponseDTO.setCount(2);
        firstResponseDTO.setDiaryList(List.of(createTimeLineDiaryDTO(2), createTimeLineDiaryDTO(1)));

        final DiarySearchResponseDTO secondResponseDTO = new DiarySearchResponseDTO();
        secondResponseDTO.setCategoryName("경복궁 앞");
        secondResponseDTO.setCount(1);
        secondResponseDTO.setDiaryList(List.of(createTimeLineDiaryDTO(3)));

        responseDTOList.add(firstResponseDTO);
        responseDTOList.add(secondResponseDTO);

        when(searchService.getSearchResultWithoutCondition(any(User.class), any(Integer.class))).thenReturn(responseDTOList);

        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(get("/search")
                        .headers(makeHeader()))
                .andExpect(status().isOk())
                .andDo(document("search without condition"))
                .andReturn()
                .getResponse();

        final ObjectMapper objectMapper = new ObjectMapper();
        Assertions.assertEquals(
                mockHttpServletResponse.getContentAsString(StandardCharsets.UTF_8),
                objectMapper.writeValueAsString(responseDTOList));


    }

    @Test
    void getMoreSearchResult() throws Exception {
        final List<TimelineDiaryDTO> timelineDiaryDTOList = List.of(createTimeLineDiaryDTO(3), createTimeLineDiaryDTO(2), createTimeLineDiaryDTO(1));

        when(searchService.getMoreSearchResult(any(User.class), any(String.class), any(Integer.class))).thenReturn(timelineDiaryDTOList);

        final MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.set("searchCond", "BREAKFAST");
        multiValueMap.set("offset", "1");

        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(get("/search/more-diary")
                        .headers(makeHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .params(multiValueMap))
                .andExpect(status().isOk())
                .andDo(document("more search result"))
                .andReturn()
                .getResponse();

        final ArgumentCaptor<String> requestCondition = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Integer> requestOffset = ArgumentCaptor.forClass(Integer.class);

        BDDMockito.then(searchService).should(Mockito.times(1)).getMoreSearchResult(any(User.class), requestCondition.capture(), requestOffset.capture());

        Assertions.assertEquals(requestCondition.getValue(), Objects.requireNonNull(multiValueMap.getFirst("searchCond")));
        Assertions.assertEquals(requestOffset.getValue(), Integer.valueOf(Objects.requireNonNull(multiValueMap.getFirst("offset"))));

        final ObjectMapper objectMapper = new ObjectMapper();
        Assertions.assertEquals(
                mockHttpServletResponse.getContentAsString(StandardCharsets.UTF_8),
                objectMapper.writeValueAsString(timelineDiaryDTOList));


    }

    private TimelineDiaryDTO createTimeLineDiaryDTO(final int diaryId) {
        final TimelineDiaryDTO timelineDiaryDTO = new TimelineDiaryDTO();
        timelineDiaryDTO.setDiaryId(diaryId);
        timelineDiaryDTO.setBytes(BYTES);
        return timelineDiaryDTO;
    }



}
