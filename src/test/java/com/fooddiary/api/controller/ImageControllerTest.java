package com.fooddiary.api.controller;

import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.dto.response.DayImageDTO;
import com.fooddiary.api.dto.response.DayImagesDTO;
import com.fooddiary.api.dto.response.SaveImageResponseDTO;
import com.fooddiary.api.entity.image.Time;
import com.fooddiary.api.entity.image.TimeStatus;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.DayImageService;
import com.fooddiary.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
        httpHeaders.add("login-from", "none");
        httpHeaders.add("token", token);

        //Mock파일생성
        final MockMultipartFile image1 = new MockMultipartFile(
                "files",
                "apple.png",
                "image/png",
                new FileInputStream("src/test/resources/image/apple.png")
        );

        final SaveImageResponseDTO saveImageResponseDto = SaveImageResponseDTO.builder()
                .status(SaveImageResponseDTO.Status.SUCCESS).build();

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
        httpHeaders.add("login-from", "none");
        httpHeaders.add("token", token);

        final List<DayImageDTO> dayImageDTOS = new ArrayList<>();
        final FileInputStream fileInputStream = new FileInputStream("src/test/resources/image/apple.png");
        final byte[] bytes = fileInputStream.readAllBytes();

        final DayImageDTO dayImageDto = DayImageDTO.builder()
                .bytes(bytes)
                .id(1)
                .timeStatus(TimeStatus.BREAKFAST.getCode())
                .build();

        dayImageDTOS.add(dayImageDto);
        final DayImageDTO dayImageDTO2 = DayImageDTO.builder()
                .bytes(bytes)
                .id(2)
                .timeStatus(TimeStatus.DINNER.getCode())
                .build();
        dayImageDTOS.add(dayImageDTO2);

        final int year = LocalDateTime.now().getYear();
        final int month = LocalDateTime.now().getMonth().getValue();
        final int day = LocalDateTime.now().getDayOfMonth();


        when(userService.getValidUser(any(), any())).thenReturn(principal);
        when(dayImageService.getDayImage(year, month, day, principal)).thenReturn(dayImageDTOS);


        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

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
        httpHeaders.add("login-from", "none");
        httpHeaders.add("token", token);

        final List<DayImagesDTO> dayImagesDtos = new ArrayList<>();
        final FileInputStream fileInputStream = new FileInputStream("src/test/resources/image/apple.png");
        final Time time1 = new Time(LocalDateTime.now().minusDays(1));
        final byte[] bytes = fileInputStream.readAllBytes();
        final DayImagesDTO dayImagesDto = DayImagesDTO.builder()
                .id(1)
                .bytes(bytes)
                .time(time1)
                .build();
        dayImagesDtos.add(dayImagesDto);
        final Time time2 = new Time(LocalDateTime.now());

        final DayImagesDTO dayImagesDto2 = DayImagesDTO.builder()
                .id(2)
                .bytes(bytes)
                .time(time2)
                .build();

        dayImagesDtos.add(dayImagesDto2);

        final int year = LocalDateTime.now().getYear();
        final int month = LocalDateTime.now().getMonth().getValue();

        when(userService.getValidUser(any(), any())).thenReturn(principal);
        when(dayImageService.getDayImages(year, month, principal)).thenReturn(dayImagesDtos);


        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

        queryParams.add("year", String.valueOf(year));
        queryParams.add("month", String.valueOf(month));
        mockMvc.perform(get("/image/images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParams(queryParams)
                        .headers(httpHeaders))
                .andDo(document("get images"));

    }




}
