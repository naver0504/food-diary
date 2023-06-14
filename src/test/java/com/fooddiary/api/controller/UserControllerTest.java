package com.fooddiary.api.controller;

import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@ActiveProfiles("test")
public class UserControllerTest {

    @MockBean
    private Interceptor interceptor;
    @MockBean
    private UserService userService;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)).build();
        given(interceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    void test() throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("email", "jasuil@daum.net");
        httpHeaders.add("token", "asdf");

        this.mockMvc.perform(get("/user/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(httpHeaders))
                .andExpect(status().isOk())
                .andDo(document("test"));
    }

    @Test
    void create_user() throws Exception {
        given(userService.createUser(any())).willReturn("2$asdf1g1");
        String body = "{\"email\":\"jasuil@daum.net\",\"name\":\"성일짱\",\"password\":\"1212\"}";
        this.mockMvc.perform(post("/user/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(status().isOk(),
                        content().string("2$asdf1g1"))
                .andDo(document("create user"));
    }

}
