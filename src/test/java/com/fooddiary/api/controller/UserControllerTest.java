package com.fooddiary.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.dto.request.user.UserLoginRequestDTO;
import com.fooddiary.api.dto.request.user.UserNewPasswordRequestDTO;
import com.fooddiary.api.dto.request.user.UserResetPasswordRequestDTO;
import com.fooddiary.api.dto.response.ErrorResponseDTO;
import com.fooddiary.api.dto.response.user.RefreshTokenResponseDTO;
import com.fooddiary.api.dto.response.user.UserInfoResponseDTO;
import com.fooddiary.api.dto.response.user.UserNewPasswordResponseDTO;
import com.fooddiary.api.dto.response.user.UserResponseDTO;
import com.fooddiary.api.entity.user.Role;
import com.fooddiary.api.entity.user.Session;
import com.fooddiary.api.entity.user.Status;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.user.SessionRepository;
import com.fooddiary.api.service.user.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static com.fooddiary.api.common.util.HttpUtil.makeHeader;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 컨트롤러 계층에 대한 테스트 입니다. API 문서생성도 같이하고 있습니다.
 */
@SpringBootTest
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@ActiveProfiles(Profiles.TEST)
public class UserControllerTest {

    @MockBean
    PasswordEncoder passwordEncoder;
    @MockBean
    Interceptor interceptor;
    @MockBean
    UserService userService;
    @Autowired
    WebApplicationContext context;
    MockMvc mockMvc;
    static User principal;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                                 .apply(documentationConfiguration(restDocumentation)).build();
        given(interceptor.preHandle(any(), any(), any())).willReturn(true);

        principal = new User();
        principal.setEmail("test@test.com");
        principal.setPw("1234");
        principal.setId(1);

        final Authentication authentication = mock(RememberMeAuthenticationToken.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void user_info() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        UserInfoResponseDTO userInfoResponseDTO = new UserInfoResponseDTO();
        userInfoResponseDTO.setRole(Role.ADMIN);
        userInfoResponseDTO.setStatus(Status.ACTIVE);

        when(userService.getUserInfo(any(User.class))).thenReturn(userInfoResponseDTO);

        String servletResponse = mockMvc.perform(get("/user/info")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(makeHeader()))
                .andDo(document("user info"))
                .andReturn().getResponse().getContentAsString();

        Assertions.assertEquals(servletResponse, objectMapper.writeValueAsString(userInfoResponseDTO));
    }

    @Test
    void is_login() throws Exception {
        given(userService.getValidUser(anyString(), anyString())).willReturn(new User());

        mockMvc.perform(get("/user/is-login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(makeHeader()))
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



        final ArgumentCaptor<UserLoginRequestDTO> loginRequestDto = ArgumentCaptor.forClass(UserLoginRequestDTO.class);

        then(userService).should(times(1)).loginUser(loginRequestDto.capture());

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
        final String email = "jasuil@daum.net";
        final UserResponseDTO userResponseDto = new UserResponseDTO();
        userResponseDto.setStatus(UserResponseDTO.Status.SUCCESS);
        given(userService.resetPw(email)).willReturn(userResponseDto);
        final ObjectMapper objectMapper = new ObjectMapper();

        final UserResetPasswordRequestDTO userResetPasswordRequestDTO = new UserResetPasswordRequestDTO();
        userResetPasswordRequestDTO.setEmail(email);

        mockMvc.perform(post("/user/reset-password")
                        .content(objectMapper.writeValueAsString(userResetPasswordRequestDTO))
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpectAll(status().isOk(),
                             content().json(objectMapper.writeValueAsString(userResponseDto)))
               .andDo(document("reset password"));
    }

    @Test
    void new_password() throws Exception {
        final String password = "Food1234@!";
        final String newPassword = "myFood1234@!";

        final UserNewPasswordRequestDTO userNewPasswordRequestDTO = new UserNewPasswordRequestDTO();
        userNewPasswordRequestDTO.setPassword(password);
        userNewPasswordRequestDTO.setNewPassword(newPassword);
        final SecurityContext securityContext = mock(SecurityContext.class);
        User user = new User();
        user.setPw(password);
        final ArrayList<SimpleGrantedAuthority> simpleGrantedAuthority = new ArrayList<>();
        simpleGrantedAuthority.add(new SimpleGrantedAuthority("all"));
        final RememberMeAuthenticationToken userDataAuthenticationTokenByEmail =
                new RememberMeAuthenticationToken(
                        "jasuil@daum.net", user, simpleGrantedAuthority);

        when(securityContext.getAuthentication()).thenReturn(userDataAuthenticationTokenByEmail);
        SecurityContextHolder.setContext(securityContext);

        final UserNewPasswordResponseDTO userNewPasswordResponseDTO = new UserNewPasswordResponseDTO();
        userNewPasswordResponseDTO.setStatus(UserNewPasswordResponseDTO.Status.SUCCESS);
        when(userService.updatePassword(any(UserNewPasswordRequestDTO.class))).thenReturn(userNewPasswordResponseDTO);
        when(passwordEncoder.encode(password)).thenReturn(password);
        when(passwordEncoder.encode(newPassword)).thenReturn(newPassword);
        final ObjectMapper objectMapper = new ObjectMapper();

        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(post("/user/new-password").content(
                                                                                                            objectMapper.writeValueAsString(userNewPasswordRequestDTO))
                                                                                                    .contentType(
                                                                                                            MediaType.APPLICATION_JSON)
                                                                                                    .headers(makeHeader()))
                                                                       .andExpect(status().isOk())
                                                                       .andDo(document("new password"))
                                                                       .andReturn().getResponse();

        verify(userService, times(1)).updatePassword(any(UserNewPasswordRequestDTO.class));

        Assertions.assertEquals(mockHttpServletResponse.getStatus(), HttpStatus.OK.value());
        Assertions.assertEquals(mockHttpServletResponse.getContentAsString(),
                                objectMapper.writeValueAsString(userNewPasswordResponseDTO));
    }

    @Test
    void refresh_token() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final String newToken = "newToken";
        final RefreshTokenResponseDTO refreshTokenResponseDTO = new RefreshTokenResponseDTO();
        refreshTokenResponseDTO.setAccessToken(newToken);
        refreshTokenResponseDTO.setRefreshToken("refreshToken");
        refreshTokenResponseDTO.setAccessTokenExpireAt(now.plusDays(1L).toInstant(ZoneOffset.UTC).toEpochMilli());

        when(userService.refreshAccessToken(anyString(), anyString(), anyString())).thenReturn(refreshTokenResponseDTO);

        final ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("/user/refresh-token")
                                                  .contentType(
                                                          MediaType.APPLICATION_JSON)
                                                  .headers(makeHeader()))
               .andExpect(status().isOk())
               .andDo(document("refresh token"));


    }

    @Test
    void logout() throws Exception {
        mockMvc.perform(post("/user/logout")
                                                  .contentType(
                                                          MediaType.APPLICATION_JSON)
                                                  .headers(makeHeader()))
               .andExpect(status().isOk())
               .andDo(document("logout"));
    }

    @Test
    void google_login_callback() throws Exception {
        mockMvc.perform(get("/user/google-login-callback?code=googleAuthToken")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                )
               .andExpect(status().isOk())
               .andDo(document("google login callback"));
    }

}
