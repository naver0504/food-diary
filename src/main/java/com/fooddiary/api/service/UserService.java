package com.fooddiary.api.service;

import com.fooddiary.api.common.util.Random;
import com.fooddiary.api.dto.request.UserLoginRequestDTO;
import com.fooddiary.api.dto.request.UserNewPasswordRequestDTO;
import com.fooddiary.api.dto.request.UserNewRequestDTO;
import com.fooddiary.api.dto.response.UserNewPasswordResponseDTO;
import com.fooddiary.api.dto.response.UserResponseDTO;
import com.fooddiary.api.entity.session.Session;
import com.fooddiary.api.entity.user.Role;
import com.fooddiary.api.entity.user.Status;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.UserRepository;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private static final int PW_EXPIRED_DAY_LIMIT = 90;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    @Value("${food-diary.pw-try-limit}")
    private Integer pwTryLimit;
    @Value("${food-diary.pw-reset-size}")
    private Integer pwResetSize;

    public UserResponseDTO createUser(UserNewRequestDTO userDto) {
        final UserResponseDTO userResponseDto = new UserResponseDTO();
        final UserNewPasswordResponseDTO userNewPasswordResponseDTO = validatePassword(userDto.getPassword());
        if (userNewPasswordResponseDTO.getStatus() == UserNewPasswordResponseDTO.Status.SUCCESS) {
            if (getValidUser(userDto.getEmail()) != null) {
                userResponseDto.setStatus(UserResponseDTO.Status.DUPLICATED_USER);
                return userResponseDto;
            }
            final LocalDateTime now = LocalDateTime.now();
            final User user = new User();
            user.setEmail(userDto.getEmail());
            user.setRole(Role.CLIENT);
            user.setPw(passwordEncoder.encode(userDto.getPassword()));
            user.setPwUpdateAt(now);
            user.setPwUpdateDelayAt(now.plusDays(PW_EXPIRED_DAY_LIMIT));
            userRepository.save(user);

            userResponseDto.setToken(sessionService.createSession(user).getToken());
        }
        userResponseDto.setPasswordStatus(userNewPasswordResponseDTO.getStatus());
        return userResponseDto;
    }

    public UserResponseDTO loginUser(UserLoginRequestDTO userDto) {
        final UserResponseDTO userResponseDto = new UserResponseDTO();
        final User user = userRepository.findByEmailAndStatus(userDto.getEmail(), Status.ACTIVE);

        if (user == null){
            userResponseDto.setStatus(UserResponseDTO.Status.INVALID_USER);
            return userResponseDto;
        }
        if (user.getPwTry() >= pwTryLimit) {
            userResponseDto.setStatus(UserResponseDTO.Status.PASSWORD_LIMIT_OVER);
            return userResponseDto;
        }
        if (!passwordEncoder.matches(userDto.getPassword(), user.getPw())) {
            userResponseDto.setStatus(UserResponseDTO.Status.INVALID_PASSWORD);
            user.setPwTry(user.getPwTry() + 1);
            userRepository.save(user);
            return userResponseDto;
        }
        final List<Session> sessionList = user.getSession();

        if (sessionList.size() > 10) {
            sessionList.stream().sorted(Comparator.comparing(Session::getTerminateAt).reversed()).skip(10).forEach(sessionService::deleteSession);
        }

        final Session session = sessionService.createSession(user);
        userResponseDto.setStatus(UserResponseDTO.Status.SUCCESS);
        userResponseDto.setToken(session.getToken());
        userResponseDto.setPwExpired(user.getPwUpdateDelayAt().isBefore(LocalDateTime.now()));

        return userResponseDto;
    }

    /**
     * 1. 이메일 유효성 검증
     * 2. 식사일기에 직접 가입한 경우, token 만료를 확인
     * 2. kakao, google 등 외부로 가입한 경우 access token 만료를 확인
     * @param email request header 에서 가져옴
     * @param token request header 에서 가져옴
     * @return 유효하면 User 객체반환
     */
    @Nullable
    public User getValidUser(String email, String token) {
        // todo
        if (!StringUtils.hasText(email) || !StringUtils.hasText(token)) {return null;}

        List<User> userList = userRepository.findByEmail(email);
        userList = userList.stream().filter(u -> u.getStatus() == Status.ACTIVE).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(userList)) {return null;}

        final Session session = sessionService.getSession(userList.get(0).getId(), token);

        if (session != null) {
            userList.get(0).setSession(List.of(session));
            return userList.get(0);
        }

        return null;
    }

    @Nullable
    public User getValidUser(String email) {
        if (!StringUtils.hasText(email)) {return null;}

        List<User> userList = userRepository.findByEmail(email);
        userList = userList.stream().filter(u -> u.getStatus() == Status.ACTIVE).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(userList)) {return null;}

        return userList.get(0);
    }

    public UserResponseDTO resetPw(String email) {
        final UserResponseDTO userResponseDto = new UserResponseDTO();
        final User user = getValidUser(email);

        if (user == null) {
            userResponseDto.setStatus(UserResponseDTO.Status.INVALID_USER);
        } else {
            // pw reset
            final String tempPw = Random.RandomString(pwResetSize);

            final String username = "jasuil1212@gmail.com";
            final String password = "vyyqzspyrhfmzivy";

            final Properties prop = new Properties();
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", 465);
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.ssl.enable", "true");
            prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            prop.put("mail.smtp.ssl.protocols", "TLSv1.2");

            final jakarta.mail.Session session = jakarta.mail.Session.getInstance(prop,
                    new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            try {
                final Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("jasuil1212@gmail.com"));
                message.setRecipients(
                        RecipientType.TO,
                        InternetAddress.parse(user.getEmail())
                );
                message.setSubject("식사일기 임시 비밀번호입니다.");
                final String origin = "임시비밀번호: " + tempPw;
                message.setText(origin);

                Transport.send(message);

                user.setPw(passwordEncoder.encode(tempPw));
                userRepository.save(user);
                userResponseDto.setStatus(UserResponseDTO.Status.SUCCESS);
            } catch (Exception e) {
                log.error("임시 비번발급 에러 "  + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return userResponseDto;
    }

    public UserNewPasswordResponseDTO updatePassword(UserNewPasswordRequestDTO userNewPasswordRequestDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user = userRepository.getReferenceById(user.getId()); // 저장 전에 한번 더 확인한다.
        if (!passwordEncoder.matches(userNewPasswordRequestDTO.getPassword(), user.getPw())) {
            final UserNewPasswordResponseDTO userNewPasswordResponseDTO = new UserNewPasswordResponseDTO();
            userNewPasswordResponseDTO.setStatus(UserNewPasswordResponseDTO.Status.INVALID_PASSWORD);
            return userNewPasswordResponseDTO;
        }
        final UserNewPasswordResponseDTO userNewPasswordResponseDTO = validatePassword(userNewPasswordRequestDTO.getNewPassword());
        if (userNewPasswordResponseDTO.getStatus() == UserNewPasswordResponseDTO.Status.SUCCESS) {
            user.setPw(passwordEncoder.encode(userNewPasswordRequestDTO.getNewPassword()));
            userRepository.save(user);
        }
        return userNewPasswordResponseDTO;
    }

    private UserNewPasswordResponseDTO validatePassword(String password) {
        final UserNewPasswordResponseDTO userNewPasswordResponseDTO = new UserNewPasswordResponseDTO();
        if (!StringUtils.hasText(password)) {
            userNewPasswordResponseDTO.setStatus(UserNewPasswordResponseDTO.Status.EMPTY_PASSWORD);
            return userNewPasswordResponseDTO;
        }
        password = password.trim();
        if (password.length() < pwResetSize) {
            userNewPasswordResponseDTO.setStatus(UserNewPasswordResponseDTO.Status.SHORT_PASSWORD);
            return userNewPasswordResponseDTO;
        }
        int symbolCount = 0;
        int alphabetCount = 0;
        int digitCount = 0;
        for (int i = 0; i < password.length(); i++) {
            final char character = password.charAt(i);
            if (character >= 65 && character <= 90 || character >= 97 && character <= 122 ) {
                alphabetCount++;
            }
            if (Character.isDigit(character)) {
                digitCount++;
            }
            if ((character >= 33 && character <= 47) || (character >= 58 && character <= 64) ||
                    (character >= 91 && character <= 96) || (character >= 123 && character <= 126)) {
                symbolCount++;
            }
        }

        if (digitCount == 0) {
            userNewPasswordResponseDTO.setStatus(UserNewPasswordResponseDTO.Status.INCLUDE_DIGIT_CHARACTER);
            return userNewPasswordResponseDTO;
        }
        if (symbolCount == 0) {
            userNewPasswordResponseDTO.setStatus(UserNewPasswordResponseDTO.Status.INCLUDE_SYMBOLIC_CHARACTER);
            return userNewPasswordResponseDTO;
        }
        if (alphabetCount + symbolCount + digitCount < password.length()) {
            userNewPasswordResponseDTO.setStatus(UserNewPasswordResponseDTO.Status.NOT_ALPHABETIC_PASSWORD);
            return userNewPasswordResponseDTO;
        }

        userNewPasswordResponseDTO.setStatus(UserNewPasswordResponseDTO.Status.SUCCESS);
        return userNewPasswordResponseDTO;
    }
}
