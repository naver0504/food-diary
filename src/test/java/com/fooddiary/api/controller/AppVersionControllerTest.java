package com.fooddiary.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.dto.response.version.AppVersionResponseDTO;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.version.VersionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import java.nio.charset.StandardCharsets;

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
public class AppVersionControllerTest {

    static User principal;
    @MockBean
    Interceptor interceptor;
    @Autowired
    WebApplicationContext context;
    MockMvc mockMvc;
    @MockBean
    VersionService versionService;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {

        principal = new User();
        principal.setEmail("test@test.com");
        principal.setPw("1234");
        principal.setId(1);

        final Authentication authentication = mock(RememberMeAuthenticationToken.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)
                        .uris()
                        .withHost("www.myfooddiarybook.click")
                        .withScheme("https")
                        .withPort(443)
                ).build();

        given(interceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    void get_version() throws Exception {
        final String version = "1.0.0";
        AppVersionResponseDTO appVersionResponseDTO = AppVersionResponseDTO.builder().version(version).build();
        when(versionService.getReleaseVersion()).thenReturn(appVersionResponseDTO);

        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(get("/app/version")
                        .contentType(
                                MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get version"))
                .andReturn()
                .getResponse();

        final ObjectMapper objectMapper = new ObjectMapper();
        Assertions.assertEquals(
                mockHttpServletResponse.getContentAsString(StandardCharsets.UTF_8),
                objectMapper.writeValueAsString(appVersionResponseDTO));
    }
}
