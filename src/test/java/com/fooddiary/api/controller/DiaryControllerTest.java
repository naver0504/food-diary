package com.fooddiary.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.dto.request.diary.DiaryMemoRequestDTO;
import com.fooddiary.api.dto.request.diary.PlaceInfoDTO;
import com.fooddiary.api.dto.response.diary.DiaryDetailResponseDTO;
import com.fooddiary.api.dto.response.diary.HomeDayResponseDTO;
import com.fooddiary.api.dto.response.diary.HomeResponseDTO;
import com.fooddiary.api.dto.response.image.ImageResponseDTO;
import com.fooddiary.api.entity.diary.DiaryTime;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.diary.DiaryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles(Profiles.LOCAL)
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
public class DiaryControllerTest {

    @MockBean
    Interceptor interceptor;
    @Autowired
    WebApplicationContext context;
    MockMvc mockMvc;
    static User principal;
    @MockBean
    DiaryService diaryService;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {

        principal = new User();
        principal.setEmail("test@test.com");
        principal.setPw("1234");
        principal.setId(1);

        Authentication authentication = mock(RememberMeAuthenticationToken.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)).build();

        given(interceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    void getHome() throws Exception {
        final String param = YearMonth.now().toString();

        List<HomeResponseDTO> responseDTOList = new LinkedList<>();
        HomeResponseDTO responseDTO = new HomeResponseDTO();
        responseDTO.setId(1);
        responseDTO.setBytes(new byte[]{'a','c','d','e'});
        responseDTO.setTime(LocalDate.now());
        responseDTOList.add(responseDTO);

        Mockito.when(diaryService.getHome(any(YearMonth.class), any(User.class))).thenReturn(responseDTOList);

        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(get("/diary/home?yearMonth=" + param)
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .headers(getHeader()))
                .andExpect(status().isOk())
                .andDo(document("get home"))
                .andReturn()
                .getResponse();

        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Assertions.assertEquals(
                mockHttpServletResponse.getContentAsString(StandardCharsets.UTF_8),
                objectMapper.writeValueAsString(responseDTOList));
    }

    @Test
    void homeDay() throws Exception {
        final String param = LocalDate.now().toString();

        HomeDayResponseDTO homeDayResponseDTO = new HomeDayResponseDTO();
        List<HomeDayResponseDTO.HomeDay> homeDayList = new ArrayList<>();
        ImageResponseDTO imageResponseDTO = new ImageResponseDTO();
        imageResponseDTO.setImageId(2);
        imageResponseDTO.setBytes(new byte[]{'a','c','f'});
        HomeDayResponseDTO.HomeDay homeDay = HomeDayResponseDTO.HomeDay.builder()
                .diaryTime(DiaryTime.BRUNCH)
                .id(1)
                .image(imageResponseDTO)
                .memo("메모닷")
                .place("서울역")
                .tags(Arrays.asList("아침겸점심","심심해서"))
                .latitude(-200D)
                .longitude(-200D)
                .build();
        homeDayList.add(homeDay);
        homeDayResponseDTO.setHomeDayList(homeDayList);

        when(diaryService.getHomeDay(any(LocalDate.class), any(User.class))).thenReturn(homeDayResponseDTO);

        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(get("/diary/home-day?date=" + param)
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .headers(getHeader()))
                .andExpect(status().isOk())
                .andDo(document("get home day"))
                .andReturn()
                .getResponse();

        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Assertions.assertEquals(
                mockHttpServletResponse.getContentAsString(StandardCharsets.UTF_8),
                objectMapper.writeValueAsString(homeDayResponseDTO));
    }

    @Test
    void newDiary() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("files", "t.jpg", "image/jpeg", new byte[]{'i','m','a','g','e'});
        MockMultipartFile mockMultipartFile2 = new MockMultipartFile("files", "t2.jpg", "image/jpeg", new byte[]{'i','m','a','g','e','2'});
        PlaceInfoDTO placeInfoDTO = new PlaceInfoDTO();
        placeInfoDTO.setPlace("부산시 노포동");
        placeInfoDTO.setLongitude(-200D);
        placeInfoDTO.setLatitude(-200D);
        byte[] requestPlaceInfo = objectMapper.writeValueAsString(placeInfoDTO).getBytes(StandardCharsets.UTF_8);;
        MockPart jsonPart = new MockPart("placeInfo", "json", requestPlaceInfo);
        jsonPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        doNothing().when(diaryService).createDiary(any(List.class), any(LocalDate.class), any(PlaceInfoDTO.class), any(User.class));

        mockMvc.perform(multipart("/diary/new")
                        .file(mockMultipartFile)
                        .file(mockMultipartFile2)
                        .part(jsonPart)
                        .queryParam("createTime", LocalDate.now().toString())
                        .headers(getHeader())
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk()).andDo(document("new diary"));
    }

    @Test
    void addImages() throws Exception {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("files", "t.jpg", "image/jpeg", new byte[]{'i','m','a','g','e'});
        MockMultipartFile mockMultipartFile2 = new MockMultipartFile("files", "t2.jpg", "image/jpeg", new byte[]{'i','m','a','g','e','2'});

        doNothing().when(diaryService).addImages(any(Integer.class), any(List.class), any(User.class));

        mockMvc.perform(multipart("/diary/{diaryId}/images", 1)
                .file(mockMultipartFile)
                .file(mockMultipartFile2)
                .headers(getHeader()))
                .andExpect(status().isOk()).andDo(document("add images"));
    }

    @Test
    void updateImage() throws Exception {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "t.jpg", "image/jpeg", new byte[]{'i','m','a','g','e'});

        doNothing().when(diaryService).updateImage(any(Integer.class), any(MultipartFile.class), any(User.class));

        mockMvc.perform(multipart("/diary/image/{imageId}", 1).file(mockMultipartFile)
                        .with(new RequestPostProcessor() {
                            @Override
                            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                                request.setMethod("PATCH");
                                return request;
                            }
                        })
                        .headers(getHeader())
                )
                .andExpect(status().isOk()).andDo(document("update image"));
    }

    @Test
    void getDiaryDetail() throws Exception {
        DiaryDetailResponseDTO diaryDetailResponseDTO = new DiaryDetailResponseDTO();
        diaryDetailResponseDTO.setDiaryTime(DiaryTime.SNACK.name());
        diaryDetailResponseDTO.setDate(LocalDate.now());
        diaryDetailResponseDTO.setMemo("간식");

        List<DiaryDetailResponseDTO.TagResponse> tagResponseList = new ArrayList<>();
        DiaryDetailResponseDTO.TagResponse tagResponse = new DiaryDetailResponseDTO.TagResponse();
        tagResponse.setId(1L);
        tagResponse.setName("과자");
        tagResponseList.add(tagResponse);
        diaryDetailResponseDTO.setTags(tagResponseList);

        List<ImageResponseDTO> imageResponseDTOList = new ArrayList<>();
        ImageResponseDTO imageResponseDTO = new ImageResponseDTO();
        imageResponseDTO.setImageId(1);
        imageResponseDTO.setBytes(new byte[]{'s','n','a','c','k'});
        imageResponseDTOList.add(imageResponseDTO);
        diaryDetailResponseDTO.setImages(imageResponseDTOList);

        diaryDetailResponseDTO.setPlace("종로구");
        diaryDetailResponseDTO.setLatitude(-200D);
        diaryDetailResponseDTO.setLatitude(-200D);

        when(diaryService.getDiaryDetail(eq(1), any(User.class))).thenReturn(diaryDetailResponseDTO);

        final MockHttpServletResponse mockHttpServletResponse =  mockMvc.perform(get("/diary/{diaryId}", 1)
                        .headers(getHeader()))
                .andExpect(status().isOk())
                .andDo(document("diary detail"))
                .andReturn()
                .getResponse();

        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Assertions.assertEquals(
                mockHttpServletResponse.getContentAsString(StandardCharsets.UTF_8),
                objectMapper.writeValueAsString(diaryDetailResponseDTO));
    }

    @Test
    void updateMemo() throws Exception {
        DiaryMemoRequestDTO diaryMemoRequestDTO = new DiaryMemoRequestDTO();
        diaryMemoRequestDTO.setMemo("궁금");
        diaryMemoRequestDTO.setDiaryTime(DiaryTime.DINNER);
        diaryMemoRequestDTO.setLatitude(-200D);
        diaryMemoRequestDTO.setLongitude(-200D);
        diaryMemoRequestDTO.setPlace("경복궁 앞");

        List<DiaryMemoRequestDTO.TagRequestDTO> tagRequestDTOList = new ArrayList<>();
        DiaryMemoRequestDTO.TagRequestDTO tagRequestDTO = new DiaryMemoRequestDTO.TagRequestDTO();
        tagRequestDTO.setId(1L);
        tagRequestDTO.setName("과자");
        tagRequestDTOList.add(tagRequestDTO);
        diaryMemoRequestDTO.setTags(tagRequestDTOList);

        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(diaryMemoRequestDTO);

        doNothing().when(diaryService).updateMemo(any(Integer.class), any(DiaryMemoRequestDTO.class), any(User.class));

        mockMvc.perform(put("/diary/{diaryId}/memo", 1)
                        .headers(getHeader())
                        .content(content))
                .andExpect(status().isOk())
                .andDo(document("update memo"));
    }

    @Test
    void deleteDiary() throws Exception {
        mockMvc.perform(delete("/diary/{diaryId}", 1)
                        .headers(getHeader()))
                .andExpect(status().isOk())
                .andDo(document("delete diary"));
    }

    private  HttpHeaders getHeader() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("login-from", "none");
        httpHeaders.add("token", "asdf");
        return httpHeaders;
    }

}
