package com.fooddiary.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fooddiary.api.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.dto.request.UserLoginRequestDTO;
import com.fooddiary.api.dto.request.UserNewPasswordRequestDTO;
import com.fooddiary.api.dto.response.ErrorResponseDTO;
import com.fooddiary.api.dto.response.UserNewPasswordResponseDTO;
import com.fooddiary.api.dto.response.UserResponseDTO;
import com.fooddiary.api.entity.user.User;

/**
 * 컨트롤러 계층에 대한 테스트 입니다. API 문서생성도 같이하고 있습니다.
 */
@SpringBootTest
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@ActiveProfiles(Profiles.TEST)
public class UserControllerTest {

    @MockBean
    private Interceptor interceptor;
    @MockBean
    private UserService userService;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;
    @Captor
    private ArgumentCaptor<UserLoginRequestDTO> loginRequestDto;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                                 .apply(documentationConfiguration(restDocumentation)).build();
        given(interceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    void is_login() throws Exception {
        given(userService.getValidUser(anyString(), anyString())).willReturn(new User());
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "jasuil@daum.net");
        httpHeaders.add("token", "asdf");

        mockMvc.perform(get("/user/is-login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(httpHeaders))
               .andExpect(status().isOk())
               .andDo(document("is login"));
    }

    @Test
    void create_user() throws Exception {
        final UserResponseDTO userResponseDto = new UserResponseDTO();
        userResponseDto.setToken("2$asdf1g1");
        userResponseDto.setPasswordStatus(UserNewPasswordResponseDTO.Status.SUCCESS);
        given(userService.createUser(any())).willReturn(userResponseDto);
        final String body = "{\"email\":\"jasuil@daum.net\",\"name\":\"성일짱\",\"password\":\"1212\"}";
        final ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/user/new")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
               .andExpectAll(status().isOk(),
                             content().json(objectMapper.writeValueAsString(userResponseDto)))
               .andDo(document("create user"));
    }

    /**
     * 식사일기 전용계정의 로그인 확인
     */
    @Test
    void login() throws Exception {
        final UserLoginRequestDTO userNewRequestDto = new UserLoginRequestDTO();
        userNewRequestDto.setEmail("jasuil@daum.net");
        userNewRequestDto.setPassword("1212");
        final String token = "2$asdf1g1";
        final UserResponseDTO userResponseDto = new UserResponseDTO();
        userResponseDto.setToken(token);
        userResponseDto.setStatus(UserResponseDTO.Status.SUCCESS);

        given(userService.loginUser(any())).willReturn(userResponseDto);

        final ObjectMapper objectMapper = new ObjectMapper();

        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(post("/user/login")
                                                                                        .contentType(
                                                                                                MediaType.APPLICATION_JSON)
                                                                                        .content(
                                                                                                objectMapper.writeValueAsString(
                                                                                                        userNewRequestDto)))
                                                                       .andDo(document("login user"))
                                                                       .andReturn()
                                                                       .getResponse();

        Assertions.assertEquals(mockHttpServletResponse.getStatus(), HttpStatus.OK.value());
        Assertions.assertEquals(mockHttpServletResponse.getContentAsString(),
                                objectMapper.writeValueAsString(userResponseDto));

        then(userService).should(timeout(1)).loginUser(loginRequestDto.capture());

        final List<UserLoginRequestDTO> servletLoginRequest = loginRequestDto.getAllValues();

        Assertions.assertEquals(servletLoginRequest.size(), 1);
        Assertions.assertEquals(servletLoginRequest.get(0).getEmail(), userNewRequestDto.getEmail());
        Assertions.assertEquals(servletLoginRequest.get(0).getPassword(), userNewRequestDto.getPassword());
    }

    /**
     * 서버에러 응답값의 문서화를 위한 테스트
     */
    @Test
    void login_error() throws Exception {
        final UserLoginRequestDTO userNewRequestDto = new UserLoginRequestDTO();
        userNewRequestDto.setEmail("jasuil@daum.net");
        userNewRequestDto.setPassword("1212");
        final ErrorResponseDTO errorResponseDto = new ErrorResponseDTO("system error");

        given(userService.loginUser(any())).willThrow(new RuntimeException("test"));

        final ObjectMapper objectMapper = new ObjectMapper();

        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(post("/user/login")
                                                                                        .contentType(
                                                                                                MediaType.APPLICATION_JSON)
                                                                                        .content(
                                                                                                objectMapper.writeValueAsString(
                                                                                                        userNewRequestDto)))
                                                                       .andDo(document("error response"))
                                                                       .andReturn()
                                                                       .getResponse();

        Assertions.assertEquals(mockHttpServletResponse.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        Assertions.assertEquals(mockHttpServletResponse.getContentAsString(),
                                objectMapper.writeValueAsString(errorResponseDto));
    }

    @Test
    void reset_password() throws Exception {
        final UserResponseDTO userResponseDto = new UserResponseDTO();
        userResponseDto.setStatus(UserResponseDTO.Status.SUCCESS);
        given(userService.resetPw()).willReturn(userResponseDto);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "jasuil@daum.net");
        httpHeaders.add("token", "asdf");
        final ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/user/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(httpHeaders))
               .andExpectAll(status().isOk(),
                             content().json(objectMapper.writeValueAsString(userResponseDto)))
               .andDo(document("reset password"));
    }

    @Test
    void new_password() throws Exception {
        final String newPassword = "myFood1234@!";
        final UserNewPasswordResponseDTO userNewPasswordResponseDTO = new UserNewPasswordResponseDTO();
        userNewPasswordResponseDTO.setStatus(UserNewPasswordResponseDTO.Status.SUCCESS);
        when(userService.updatePassword(newPassword)).thenReturn(userNewPasswordResponseDTO);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "jasuil@daum.net");
        httpHeaders.add("token", "asdf");
        final ObjectMapper objectMapper = new ObjectMapper();
        final UserNewPasswordRequestDTO userNewPasswordRequestDTO = new UserNewPasswordRequestDTO();
        userNewPasswordRequestDTO.setPassword(newPassword);

        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(post("/user/new-password").content(
                                                                                                            objectMapper.writeValueAsString(userNewPasswordRequestDTO))
                                                                                                    .contentType(
                                                                                                            MediaType.APPLICATION_JSON)
                                                                                                    .headers(
                                                                                                            httpHeaders))
                                                                       .andExpect(status().isOk())
                                                                       .andDo(document("new password"))
                                                                       .andReturn().getResponse();

        verify(userService, times(1)).updatePassword(newPassword);

        Assertions.assertEquals(mockHttpServletResponse.getStatus(), HttpStatus.OK.value());
        Assertions.assertEquals(mockHttpServletResponse.getContentAsString(),
                                objectMapper.writeValueAsString(userNewPasswordResponseDTO));
    }

}