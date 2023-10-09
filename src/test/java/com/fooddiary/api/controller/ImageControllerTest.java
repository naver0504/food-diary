package com.fooddiary.api.controller;

import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.dto.request.SaveImageRequestDTO;
import com.fooddiary.api.dto.request.UpdateImageDetailDTO;
import com.fooddiary.api.dto.response.*;
import com.fooddiary.api.entity.image.Time;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.DayImageService;
import com.fooddiary.api.service.ImageService;
import com.fooddiary.api.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    private ImageService imageService;

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
    public void storeImageTest() throws Exception {

        final String token = "2$asdf1g1";
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("login-from", "none");
        httpHeaders.add("token", token);

        final SaveImageRequestDTO saveImageRequestDTO = SaveImageRequestDTO.builder()
                .localDateTime(LocalDateTime.now())
                .longitude(1.0)
                .latitude(2.0)
                .build();

        //Mock파일생성
        final MockMultipartFile image1 = new MockMultipartFile(
                "files",
                "apple.png",
                "image/png",
                new FileInputStream("src/test/resources/image/apple.png")
        );

        final MockMultipartFile image2 = new MockMultipartFile(
                "files",
                "banana.png",
                "image/png",
                new FileInputStream("src/test/resources/image/apple.png")
        );

        final String imageDetail = "{\"localDateTime\":\"2023-07-16T18:36:25\",\"longitude\":1.0,\"latitude\":2.0}";

        final MockMultipartFile imageDetails = new MockMultipartFile(
                "imageDetails",
                null,
                MediaType.APPLICATION_JSON_VALUE,
                imageDetail.getBytes(StandardCharsets.UTF_8)
        );



        final StatusResponseDTO saveImageResponseDto = StatusResponseDTO.builder()
                .status(StatusResponseDTO.Status.SUCCESS).build();

        when(userService.getValidUser(any(), any())).thenReturn(principal);
        given(dayImageService.saveImage(any(), any(), any())).willReturn(saveImageResponseDto);


        mockMvc.perform(
                        multipart("/image/saveImage")
                                .file(image1)
                                .file(image2)
                                .file(imageDetails)
                                .headers(httpHeaders)

                )
                .andDo(document("save image"));
    }



    @Test
    public void getImageTest() throws Exception {

        final String token = "2$asdf1g1";
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("login-from", "none");
        httpHeaders.add("token", token);

        final byte[] bytes = getBytes();

        final Time firstDay = new Time(LocalDateTime.of(2023, 10, 5, 0, 0, 0));
        final Time secondDay = new Time(LocalDateTime.of(2023, 10, 6, 0, 0, 0));
        final Time thirdDay = new Time(LocalDateTime.of(2023, 10, 10, 0, 0, 0));

        final List<ShowImageOfDayDTO.ImageDTO> imageDTOS = new ArrayList<>();
        imageDTOS.add(ShowImageOfDayDTO.ImageDTO.builder()
                        .id(1)
                        .bytes(bytes)
                        .tags(List.of("샐러드", "맛있다"))
                        .time("아침")
                        .build());
        imageDTOS.add(ShowImageOfDayDTO.ImageDTO.builder()
                .id(4)
                .bytes(bytes)
                .tags(List.of("샌드위치"))
                .time("점심")
                .build());
        imageDTOS.add(ShowImageOfDayDTO.ImageDTO.builder()
                .id(5)
                .bytes(bytes)
                .time("야식")
                .build());

        final ShowImageOfDayDTO dayImageDto = ShowImageOfDayDTO.builder()
                .beforeTime(TimeDetailDTO.of(firstDay))
                .todayTime(TimeDetailDTO.of(secondDay))
                .afterTime(TimeDetailDTO.of(thirdDay))
                .images(imageDTOS)
                .build();

        final int year = LocalDateTime.now().getYear();
        final int month = LocalDateTime.now().getMonth().getValue();
        final int day = LocalDateTime.now().getDayOfMonth();


        when(userService.getValidUser(any(), any())).thenReturn(principal);
        when(imageService.getImages(year, month, day, principal)).thenReturn(dayImageDto);


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
    public void getDayImagesTest() throws Exception {

        final String token = "2$asdf1g1";
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("login-from", "none");
        httpHeaders.add("token", token);

        final List<ThumbNailImagesDTO> dayImagesDtos = new ArrayList<>();
        final FileInputStream fileInputStream = new FileInputStream("src/test/resources/image/apple.png");
        final Time time1 = new Time(LocalDateTime.now().minusDays(1));
        final byte[] bytes = fileInputStream.readAllBytes();
        final ThumbNailImagesDTO dayImagesDto = ThumbNailImagesDTO.builder()
                .id(1)
                .bytes(bytes)
                .time(time1)
                .build();
        dayImagesDtos.add(dayImagesDto);
        final Time time2 = new Time(LocalDateTime.now());

        final ThumbNailImagesDTO dayImagesDto2 = ThumbNailImagesDTO.builder()
                .id(2)
                .bytes(bytes)
                .time(time2)
                .build();

        dayImagesDtos.add(dayImagesDto2);

        final int year = LocalDateTime.now().getYear();
        final int month = LocalDateTime.now().getMonth().getValue();

        when(userService.getValidUser(any(), any())).thenReturn(principal);
        when(dayImageService.getThumbNailImages(year, month, principal)).thenReturn(dayImagesDtos);


        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

        queryParams.add("year", String.valueOf(year));
        queryParams.add("month", String.valueOf(month));
        mockMvc.perform(get("/image/images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParams(queryParams)
                        .headers(httpHeaders))
                .andDo(document("get images"));
    }

    @Test
    public void getTimeLineTest() throws Exception {

        final String token = "2$asdf1g1";
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "qortmdwls1234@naver.com");
        httpHeaders.add("token", token);


        final byte[] bytes = getBytes();
        List<TimeLineResponseDTO> timeLineResponseDTOS = setTimeLineResponseDTOS(bytes);


        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();

        queryParams.add("year", String.valueOf(2023));
        queryParams.add("month", String.valueOf(10));

        when(userService.getValidUser(any(), any())).thenReturn(principal);
        when(dayImageService.getTimeLine(any(Integer.class), any(Integer.class), any(Integer.class), any(User.class))).thenReturn(timeLineResponseDTOS);

        mockMvc.perform(get("/image/timeline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParams(queryParams)
                        .headers(httpHeaders))
                .andDo(document("get timeLine"));

    }

    private static byte[] getBytes() throws IOException {
        final FileInputStream fileInputStream = new FileInputStream("src/test/resources/image/apple.png");
        final byte[] bytes = fileInputStream.readAllBytes();
        return bytes;
    }

    @Test
    public void getTimeLineWithStartDayTest() throws Exception {
        final String token = "2$asdf1g1";
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "qortmdwls1234@naver.com");
        httpHeaders.add("token", token);


        final byte[] bytes = getBytes();

        final List<TimeLineResponseDTO> timeLineResponseDTOS = setTimeLineResponseDTOS(bytes);

        timeLineResponseDTOS.remove(0);
        timeLineResponseDTOS.remove(0);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();

        queryParams.add("year", String.valueOf(2023));
        queryParams.add("month", String.valueOf(10));
        queryParams.add("startDay", String.valueOf(10));

        when(userService.getValidUser(any(), any())).thenReturn(principal);
        when(dayImageService.getTimeLine(any(Integer.class), any(Integer.class), any(Integer.class), any(User.class)))
                .thenReturn(timeLineResponseDTOS);

        mockMvc.perform(get("/image/timeline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParams(queryParams)
                        .headers(httpHeaders))
                .andDo(document("get timeLine with startDay"));


    }

    @Test
    public void showTimeLineWithStartImageIdTest() throws Exception {
        final String token = "2$asdf1g1";
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "qortmdwls1234@naver.com");
        httpHeaders.add("token", token);

        final byte[] bytes = getBytes();
        final List<TimeLineResponseDTO.ImageResponseDTO> imageResponseDTOS = getImageResponseDTOS(bytes);


        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();

        queryParams.add("year", String.valueOf(2023));
        queryParams.add("month", String.valueOf(10));
        queryParams.add("day", String.valueOf(10));


        when(userService.getValidUser(any(), any())).thenReturn(principal);
        when(imageService.getTimeLineImagesWithStartImageId(any(Integer.class), any(Integer.class), any(Integer.class), any(Integer.class),any(User.class)))
                .thenReturn(imageResponseDTOS);


        mockMvc.perform(get("/image/timeline/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParams(queryParams)
                        .headers(httpHeaders))
                .andDo(document("get timeLine with startImageId"));

    }



    @Test
    public void showImageDetailTest() throws Exception {
        final String token = "2$asdf1g1";
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "qortmdwls1234@naver.com");
        httpHeaders.add("token", token);

        final byte[] bytes = getBytes();
        final List<TimeLineResponseDTO.ImageResponseDTO> imageResponseDTOS = new ArrayList<>();
        imageResponseDTOS.add(TimeLineResponseDTO.ImageResponseDTO.createImageResponseDTO(14, bytes));
        imageResponseDTOS.add(TimeLineResponseDTO.ImageResponseDTO.createImageResponseDTO(15, bytes));
        imageResponseDTOS.add(TimeLineResponseDTO.ImageResponseDTO.createImageResponseDTO(16, bytes));

        final List<String> tags = List.of("샐러드", "맛있다");
        final String timeStatus = "아침";
        final String memo = "오늘 아침에는 샌드위치를 먹었다.......";
        final TimeDetailDTO timeDetailDTO = TimeDetailDTO.of(new Time(LocalDateTime.of(2023, 10, 10, 0, 0, 0)));

        final ImageDetailResponseDTO imageDetailDTO = ImageDetailResponseDTO.builder()
                .images(imageResponseDTOS)
                .tags(tags)
                .memo(memo)
                .timeDetail(timeDetailDTO)
                .timeStatus(timeStatus)
                .build();

        when(userService.getValidUser(any(), any())).thenReturn(principal);
        when(imageService.getImageDetail(any(Integer.class), any(User.class))).thenReturn(imageDetailDTO);

        mockMvc.perform(get("/image/14")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(httpHeaders))
                .andDo(document("get image detail"));

    }

    @Test
    public void updateImageFileTest() throws Exception {

        final String token = "2$asdf1g1";
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "qortmdwls1234@naver.com");
        httpHeaders.add("token", token);

        final StatusResponseDTO statusResponseDTO = StatusResponseDTO.builder()
                .status(StatusResponseDTO.Status.SUCCESS)
                .build();

        //Mock파일생성
        final MockMultipartFile file = new MockMultipartFile(
                "file",
                "apple.png",
                "image/png",
                new FileInputStream("src/test/resources/image/apple.png")
        );




        when(userService.getValidUser(any(), any())).thenReturn(principal);
        when(imageService.updateImage(any(Integer.class), any(MultipartFile.class), any(User.class))).thenReturn(statusResponseDTO);

        mockMvc.perform(
                MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/image/14")
                        .file(file)
                        .headers(httpHeaders))
                .andDo(document("update image file"));
    }

    @Test
    public void uploadDetailImages() throws Exception {
        final String token = "2$asdf1g1";
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "qortmdwls1234@naver.com");
        httpHeaders.add("token", token);

        final StatusResponseDTO statusResponseDTO = StatusResponseDTO.builder()
                .status(StatusResponseDTO.Status.SUCCESS)
                .build();

        //Mock파일생성
        final MockMultipartFile image1 = new MockMultipartFile(
                "files",
                "apple.png",
                "image/png",
                new FileInputStream("src/test/resources/image/apple.png")
        );

        final MockMultipartFile image2 = new MockMultipartFile(
                "files",
                "banana.png",
                "image/png",
                new FileInputStream("src/test/resources/image/apple.png")
        );

        when(userService.getValidUser(any(), any())).thenReturn(principal);
        when(imageService.uploadImageFile(any(List.class), any(Integer.class), any(User.class))).thenReturn(statusResponseDTO);
        mockMvc.perform(
                MockMvcRequestBuilders.multipart(HttpMethod.POST, "/image/detail")
                        .file(image1)
                        .file(image2)
                        .headers(httpHeaders))
                .andDo(document("upload detail images")
        );
    }

    @Test
    public void uploadImageDetailTest() throws Exception {

        final String token = "2$asdf1g1";
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "qortmdwls1234@naver.com");
        httpHeaders.add("token", token);

        final String memo = "오늘 아침에는 샌드위치를 먹었다......." +
                "정말 맛있었다........";
        final List<String> tags = List.of("샌드위치", "맛있다", "배부르다");
        final String timeStatus = "아침";
        final UpdateImageDetailDTO updateImageDetailDTO = UpdateImageDetailDTO.builder()
                .memo(memo)
                .tags(tags)
                .timeStatus(timeStatus)
                .build();



        final StatusResponseDTO statusResponseDTO = StatusResponseDTO.builder()
                .status(StatusResponseDTO.Status.SUCCESS)
                .build();

        when(userService.getValidUser(any(), any())).thenReturn(principal);
        when(imageService.updateImageDetail(any(Integer.class), any(User.class), any(UpdateImageDetailDTO.class))).thenReturn(statusResponseDTO);

        final ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(
                post("/image/14/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateImageDetailDTO))
                        .headers(httpHeaders))
                .andDo(document("upload image detail"));



    }








    @NotNull
    private static List<TimeLineResponseDTO.ImageResponseDTO> getImageResponseDTOS(byte[] bytes) {
        final List<TimeLineResponseDTO.ImageResponseDTO> imageResponseDTOS = new ArrayList<>();
        imageResponseDTOS.add(TimeLineResponseDTO.ImageResponseDTO.createImageResponseDTO(7, bytes));
        imageResponseDTOS.add(TimeLineResponseDTO.ImageResponseDTO.createImageResponseDTO(6, bytes));
        return imageResponseDTOS;
    }

    private static List<TimeLineResponseDTO> setTimeLineResponseDTOS(byte[] bytes) {
        final List<TimeLineResponseDTO> timeLineResponseDTOS = new ArrayList<>();


        final Time firstDay = new Time(LocalDateTime.of(2023, 10, 5, 0, 0, 0));
        final Time secondDay = new Time(LocalDateTime.of(2023, 10, 6, 0, 0, 0));
        final Time thirdDay = new Time(LocalDateTime.of(2023, 10, 10, 0, 0, 0));
        final Time fourthDay = new Time(LocalDateTime.of(2023, 10, 11, 0, 0, 0));

        final List<TimeLineResponseDTO.ImageResponseDTO> ImageResponseDTOS1 = new ArrayList<>();
        ImageResponseDTOS1.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                        .imageId(1)
                        .bytes(bytes)
                        .build());

        final TimeLineResponseDTO timeLineDTO1 = TimeLineResponseDTO.builder()
                .timeDetail(TimeDetailDTO.of(firstDay))
                .images(ImageResponseDTOS1)
                .build();

        final List<TimeLineResponseDTO.ImageResponseDTO> imageResponseDTOS2 = new ArrayList<>();

        imageResponseDTOS2.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                .imageId(5)
                .bytes(bytes)
                .build());
        imageResponseDTOS2.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                .imageId(4)
                .bytes(bytes)
                .build());
        imageResponseDTOS2.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                .imageId(2)
                .bytes(bytes)
                .build());
        final TimeLineResponseDTO timeLineDTO2 = TimeLineResponseDTO.builder()
                .timeDetail(TimeDetailDTO.of(secondDay))
                .images(imageResponseDTOS2)
                .build();

        final List<TimeLineResponseDTO.ImageResponseDTO> imageResponseDTOS3 = new ArrayList<>();
        imageResponseDTOS3.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                .imageId(17)
                .bytes(bytes)
                .build());
        imageResponseDTOS3.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                .imageId(14)
                .bytes(bytes)
                .build());
        imageResponseDTOS3.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                .imageId(13)
                .bytes(bytes)
                .build());
        imageResponseDTOS3.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                .imageId(10)
                .bytes(bytes)
                .build());
        imageResponseDTOS3.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                .imageId(8)
                .bytes(bytes)
                .build());
        final TimeLineResponseDTO timeLineDTO3 = TimeLineResponseDTO.builder()
                .timeDetail(TimeDetailDTO.of(thirdDay))
                .images(imageResponseDTOS3)
                .build();
        final List<TimeLineResponseDTO.ImageResponseDTO> imageResponseDTOS4 = new ArrayList<>();
        imageResponseDTOS4.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                .imageId(19)
                .bytes(bytes)
                .build());
        imageResponseDTOS4.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                .imageId(18)
                .bytes(bytes)
                .build());
        final TimeLineResponseDTO timeLineDTO4 = TimeLineResponseDTO.builder()
                .timeDetail(TimeDetailDTO.of(fourthDay))
                .images(imageResponseDTOS4)
                .build();
        timeLineResponseDTOS.add(timeLineDTO4);
        timeLineResponseDTOS.add(timeLineDTO3);
        timeLineResponseDTOS.add(timeLineDTO2);
        timeLineResponseDTOS.add(timeLineDTO1);


        return timeLineResponseDTOS;
    }


}
