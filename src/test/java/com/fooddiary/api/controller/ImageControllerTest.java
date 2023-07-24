package com.fooddiary.api.controller;

import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.dto.request.UserLoginRequestDto;
import com.fooddiary.api.dto.response.DayImageDto;
import com.fooddiary.api.dto.response.DayImagesDto;
import com.fooddiary.api.dto.response.SaveImageResponseDto;
import com.fooddiary.api.entity.image.DayImage;
import com.fooddiary.api.entity.image.Time;
import com.fooddiary.api.entity.image.TimeStatus;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.DayImageRepository;
import com.fooddiary.api.repository.UserRepository;
import com.fooddiary.api.service.DayImageService;
import com.fooddiary.api.service.UserService;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@ActiveProfiles(Profiles.TEST)
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
public class ImageControllerTest {

    @MockBean
    private Interceptor interceptor;
    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DayImageService dayImageService;

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;



    @Captor
    private ArgumentCaptor<UserLoginRequestDto> loginRequestDto;



    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)).build();
        given(interceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    public void storeImage() throws Exception {

        User user = new User();
        user.setEmail("qortmdwls1234@naver.com");
        user.setName("Baek");
        user.setPw("1234");
        final String token = "2$asdf1g1";
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "qortmdwls1234@naver.com");
        httpHeaders.add("token", token);

        //Mock파일생성
        MockMultipartFile image1 = new MockMultipartFile(
                "files",
                "apple.png",
                "image/png",
                new FileInputStream("src/test/resources/image/apple.png")
        );

        SaveImageResponseDto saveImageResponseDto = new SaveImageResponseDto();
        saveImageResponseDto.setStatus(SaveImageResponseDto.Status.SUCCESS);

        when(userService.getValidUser(any(), any())).thenReturn(user);
        given(dayImageService.saveImage(any(), any(), any())).willReturn(saveImageResponseDto);

        MockHttpServletResponse result = mockMvc.perform(
                multipart("/image/saveImage")
                        .file(image1).part(new MockPart("localDateTime", "2021-11-08T11:58:20.551705".getBytes(StandardCharsets.UTF_8)))
                        .headers(httpHeaders)
        )
                .andDo(document("save image"))
                .andReturn()
                .getResponse();

    }



    @Test
    @Transactional
    public void getDayImage() throws Exception {

        User user = new User();
        user.setEmail("qortmdwls1234@naver.com");
        user.setName("Baek");
        user.setPw("1234");
        user.setId(1);
        DayImage dayImage = new DayImage();
        dayImage.setUser(user);
        dayImage.setTime(new Time(LocalDateTime.now()));




        final String token = "2$asdf1g1";
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "qortmdwls1234@naver.com");
        httpHeaders.add("token", token);

        List<DayImageDto> dayImageDtos = new ArrayList<>();
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/image/apple.png");
        byte[] bytes = fileInputStream.readAllBytes();
        DayImageDto dayImageDto = new DayImageDto();
        dayImageDto.setBytes(bytes);
        dayImageDto.setTimeStatus(TimeStatus.DINNER.getCode());
        dayImageDtos.add(dayImageDto);
        DayImageDto dayImageDto2 = new DayImageDto();
        dayImageDto2.setBytes(bytes);
        dayImageDto2.setTimeStatus(TimeStatus.MORNING.getCode());
        dayImageDtos.add(dayImageDto2);

        int year = LocalDateTime.now().getYear();
        int month = LocalDateTime.now().getMonth().getValue();
        int day = LocalDateTime.now().getDayOfMonth();


        when(userService.getValidUser(any(), any())).thenReturn(user);
        when(dayImageService.getDayImage(year, month, day, user)).thenReturn(dayImageDtos);


        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();

        queryParams.add("year", String.valueOf(year));
        queryParams.add("month", String.valueOf(month));
        queryParams.add("day", String.valueOf(day));
        ResultActions resultActions = mockMvc.perform(get("/image/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParams(queryParams)
                        .headers(httpHeaders))
                .andDo(document("get image"));

    }

    @Test
    public void getDayImages() throws Exception {

        User user = new User();
        user.setEmail("qortmdwls1234@naver.com");
        user.setName("Baek");
        user.setPw("1234");
        user.setId(1);

        final String token = "2$asdf1g1";
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "qortmdwls1234@naver.com");
        httpHeaders.add("token", token);

        List<DayImagesDto> dayImagesDtos = new ArrayList<>();
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/image/apple.png");
        Time time1 = new Time(LocalDateTime.now().minusDays(1));
        byte[] bytes = fileInputStream.readAllBytes();
        DayImagesDto dayImagesDto = new DayImagesDto();
        dayImagesDto.setBytes(bytes);
        dayImagesDto.setTime(time1);
        dayImagesDtos.add(dayImagesDto);
        Time time2 = new Time(LocalDateTime.now());

        DayImagesDto dayImagesDto2 = new DayImagesDto();
        dayImagesDto2.setBytes(bytes);
        dayImagesDto2.setTime(time2);
        dayImagesDtos.add(dayImagesDto2);

        int year = LocalDateTime.now().getYear();
        int month = LocalDateTime.now().getMonth().getValue();


        when(userService.getValidUser(any(), any())).thenReturn(user);
        when(dayImageService.getDayImages(year, month, user)).thenReturn(dayImagesDtos);


        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();

        queryParams.add("year", String.valueOf(year));
        queryParams.add("month", String.valueOf(month));
        ResultActions resultActions = mockMvc.perform(get("/image/images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParams(queryParams)
                        .headers(httpHeaders))
                .andDo(document("get images"));

    }




}
