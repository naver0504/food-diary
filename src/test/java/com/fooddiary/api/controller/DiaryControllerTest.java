package com.fooddiary.api.controller;

import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
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
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles(Profiles.TEST)
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
public class DiaryControllerTest {

    @MockBean
    private Interceptor interceptor;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;
    private static User principal;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)).build();
        given(interceptor.preHandle(any(), any(), any())).willReturn(true);

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

        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(get("/diary/home?yearMonth" + param)
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .headers(getHeader()))
                .andExpect(status().isOk())
                .andDo(document("get home"))
                .andReturn()
                .getResponse();
    }

    @Test
    void homeDay() throws Exception {
        final String param = LocalDate.now().toString();

        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(get("/diary/home-day?date" + param)
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .headers(getHeader()))
                .andExpect(status().isOk())
                .andDo(document("get home day"))
                .andReturn()
                .getResponse();
    }

    private  HttpHeaders getHeader() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("login-from", "none");
        httpHeaders.add("token", "asdf");
        return httpHeaders;
    }

}
