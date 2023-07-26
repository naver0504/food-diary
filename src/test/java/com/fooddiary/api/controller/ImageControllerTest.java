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
import com.fooddiary.api.repository.UserRepository;
import com.fooddiary.api.service.DayImageService;
import com.fooddiary.api.service.UserService;
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
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;

@SpringBootTest
@ActiveProfiles(Profiles.TEST)
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
public class ImageControllerTest {

    @MockBean
    private Interceptor interceptor;
    @MockBean
    private UserService userService;

    @MockBean
    private DayImageService dayImageService;

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    private static User principal;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {
        principal = new User();
        principal.setEmail("qortmdwls1234@naver.com");
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
    public void storeImage() throws Exception {

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

        when(userService.getValidUser(any(), any())).thenReturn(principal);
        given(dayImageService.saveImage(any(), any(), any())).willReturn(saveImageResponseDto);

        mockMvc.perform(
                        multipart("/image/saveImage")
                                .file(image1).part(new MockPart("localDateTime", "2021-11-08T11:58:20.551705".getBytes(StandardCharsets.UTF_8)))
                                .headers(httpHeaders)
                )
                .andDo(document("save image"));
    }



    @Test
    @Transactional
    public void getDayImage() throws Exception {

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
        dayImageDto2.setTimeStatus(TimeStatus.BREAKFAST.getCode());
        dayImageDtos.add(dayImageDto2);

        int year = LocalDateTime.now().getYear();
        int month = LocalDateTime.now().getMonth().getValue();
        int day = LocalDateTime.now().getDayOfMonth();


        when(userService.getValidUser(any(), any())).thenReturn(principal);
        when(dayImageService.getDayImage(year, month, day, principal)).thenReturn(dayImageDtos);


        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();

        queryParams.add("year", String.valueOf(year));
        queryParams.add("month", String.valueOf(month));
        queryParams.add("day", String.valueOf(day));
        mockMvc.perform(get("/image/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParams(queryParams)
                        .headers(httpHeaders))
                .andDo(document("get image"));

    }

    @Test
    public void getDayImages() throws Exception {

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

        when(userService.getValidUser(any(), any())).thenReturn(principal);
        when(dayImageService.getDayImages(year, month, principal)).thenReturn(dayImagesDtos);


        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();

        queryParams.add("year", String.valueOf(year));
        queryParams.add("month", String.valueOf(month));
        mockMvc.perform(get("/image/images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParams(queryParams)
                        .headers(httpHeaders))
                .andDo(document("get images"));

    }




}
