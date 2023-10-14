package com.fooddiary.api.service;

import com.fooddiary.api.dto.response.user.UserNewPasswordResponseDTO;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.user.UserRepository;
import com.fooddiary.api.service.user.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.fooddiary.api.dto.response.user.UserNewPasswordResponseDTO.Status.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class UserServiceUnitTest {
    @InjectMocks
    UserService userService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    UserRepository userRepository;

    @Test
    void validatePassword() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ReflectionTestUtils.setField(userService, "pwResetSize", 10);
        Method method = UserService.class.getDeclaredMethod("validatePassword", String.class);
        method.setAccessible(true);
        UserNewPasswordResponseDTO userNewPasswordResponseDTO = (UserNewPasswordResponseDTO) method.invoke(userService, "");
        Assertions.assertEquals(userNewPasswordResponseDTO.getStatus(), EMPTY_PASSWORD);
        userNewPasswordResponseDTO = (UserNewPasswordResponseDTO) method.invoke(userService, "가나나\\");
        Assertions.assertEquals(userNewPasswordResponseDTO.getStatus(), SHORT_PASSWORD);
        userNewPasswordResponseDTO = (UserNewPasswordResponseDTO) method.invoke(userService, "가나나0sdfffff난몰라왜????라]\\");
        Assertions.assertEquals(userNewPasswordResponseDTO.getStatus(), NOT_ALPHABETIC_PASSWORD);
        userNewPasswordResponseDTO = (UserNewPasswordResponseDTO) method.invoke(userService, "가나나sdfffff라]\\");
        Assertions.assertEquals(userNewPasswordResponseDTO.getStatus(), INCLUDE_DIGIT_CHARACTER);
        userNewPasswordResponseDTO = (UserNewPasswordResponseDTO) method.invoke(userService, "가나나sdfffff9라");
        Assertions.assertEquals(userNewPasswordResponseDTO.getStatus(), INCLUDE_SYMBOLIC_CHARACTER);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        User user = new User();
        final ArrayList<SimpleGrantedAuthority> simpleGrantedAuthority = new ArrayList<>();
        simpleGrantedAuthority.add(new SimpleGrantedAuthority("all"));
        final RememberMeAuthenticationToken userDataAuthenticationTokenByEmail =
                new RememberMeAuthenticationToken(
                        "jasuil@daum.net", user, simpleGrantedAuthority);

        when(securityContext.getAuthentication()).thenReturn(userDataAuthenticationTokenByEmail);
        SecurityContextHolder.setContext(securityContext);

        when(passwordEncoder.encode(anyString())).thenReturn("df33");
        when(userRepository.save(user)).thenReturn(null);

        userNewPasswordResponseDTO = (UserNewPasswordResponseDTO) method.invoke(userService, "fooddiary1#");
        Assertions.assertEquals(userNewPasswordResponseDTO.getStatus(), SUCCESS);
    }
}
