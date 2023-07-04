package com.fooddiary.api.controller;

import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.interceptor.Interceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

        //Mock파일생성
        MockMultipartFile image1 = new MockMultipartFile(
                "apple", //name
                "apple.png", //originalFilename
                "png",
                new FileInputStream("C:\\Users\\qortm\\OneDrive\\사진\\Saved Pictures\\images\\apple.png")
        );

        MockHttpServletResponse result = mockMvc.perform(
                multipart("/saveImage")
                        .file(image1)
                        .param("localDateTime", LocalDateTime.now().toString())
        ).andReturn().getResponse();

        String contentAsString = result.getContentAsString();

        System.out.println("contentAsString = " + contentAsString);

    }

    @Test
    public void byteArrayConvertToImageFile(byte[] imageByte) throws IOException {


        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageByte);
        BufferedImage bufferedImage = ImageIO.read(inputStream);

        ImageIO.write(bufferedImage, "png", new File("/var/opt/image.png")); //저장하고자 하는 파일 경로를 입력합니다.
    }



}
