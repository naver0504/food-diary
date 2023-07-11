package com.fooddiary.api.controller;

import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.dto.request.UserLoginRequestDto;
import com.fooddiary.api.dto.response.DayImageDto;
import com.fooddiary.api.dto.response.SaveImageResponseDto;
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
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
    @Transactional
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

        MockMultipartFile jsonFile = new MockMultipartFile("json", "", "application/json", "{\"localDateTime\": \"2021-11-08T11:58:20.551705\n\"}".getBytes());




        SaveImageResponseDto saveImageResponseDto = new SaveImageResponseDto();
        saveImageResponseDto.setStatus(SaveImageResponseDto.Status.SUCCESS);

        given(dayImageService.saveImage(any(), any(), any())).willReturn(saveImageResponseDto);


        MockHttpServletResponse result2 = mockMvc.perform(
                multipart("/image/saveImage")
                        .file(image1)
                        .file(jsonFile)
                        .headers(httpHeaders)
        )
                .andDo(document("save image"))
                .andReturn()
                .getResponse();

    }



    @Test
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
        DayImageDto dayImageDto2 = new DayImageDto();
        dayImageDto2.setBytes(bytes);
        dayImageDto2.setTimeStatus(TimeStatus.MORNING.getCode());
        dayImageDtos.add(dayImageDto2);

        given(dayImageService.getDayImage(1, 2, 3, new User())).willReturn(dayImageDtos);

        mockMvc.perform(get("/image/image?year=1&month=2&day=3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(httpHeaders))
                .andDo(document("get image"));

    }



}
