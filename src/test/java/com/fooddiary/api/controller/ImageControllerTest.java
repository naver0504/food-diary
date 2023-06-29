package com.fooddiary.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.dto.request.ImageCreateDto;
import com.fooddiary.api.dto.request.UserLoginRequestDto;
import com.fooddiary.api.dto.response.UserResponseDto;
import com.fooddiary.api.service.ImageService;
import com.fooddiary.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;

@SpringBootTest
@ActiveProfiles(Profiles.TEST)
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
public class ImageControllerTest {

    @MockBean
    private Interceptor interceptor;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;


    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)).build();
        given(interceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    public void storeImage() throws Exception {

        final String contentType = "png"; //파일타입
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(2);
        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\qortm\\OneDrive\\사진\\Saved Pictures\\images\\apple.png");
        MockMultipartFile image1 = new MockMultipartFile(
                "apple", //name
                "apple" + "." + contentType, //originalFilename
                contentType,
                fileInputStream
        );
        MockHttpServletResponse result = mockMvc.perform(
                        multipart("/imageTest")
                                .file(image1)
                                .param("localDateTime", String.valueOf(localDateTime))
                ).andReturn()
                .getResponse();

        System.out.println("result.getContentAsString() = " + result.getContentAsString());
    }


}
