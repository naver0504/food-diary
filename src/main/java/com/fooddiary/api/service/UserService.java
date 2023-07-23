package com.fooddiary.api.service;

import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.common.util.Random;
import com.fooddiary.api.dto.request.UserLoginRequestDto;
import com.fooddiary.api.dto.request.UserNewRequestDto;
import com.fooddiary.api.dto.response.UserResponseDto;
import com.fooddiary.api.entity.session.Session;
import com.fooddiary.api.entity.user.Status;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.UserRepository;
import jakarta.mail.Message;
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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    @Value("food-diary.pw-try-limit")
    private Integer pwTryLimit;
    @Value("food-diary.pw-reset-size")
    private Integer pwResetSize;


    public String createUser(UserNewRequestDto userDto) {
        LocalDateTime now = LocalDateTime.now();
        final User user = new User();
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        user.setPw(passwordEncoder.encode(userDto.getPassword()));
        user.setPwUpdateAt(now);
        user.setPwUpdateDelayAt(now);
        userRepository.save(user);

        final Session session = sessionService.createSession(user);
        return session.getToken();
    }

    public UserResponseDto loginUser(UserLoginRequestDto userDto) {
        UserResponseDto userResponseDto = new UserResponseDto();
        User user = userRepository.findByEmailAndStatus(userDto.getEmail(), Status.ACTIVE);

        if (user == null){
            userResponseDto.setStatus(UserResponseDto.Status.INVALID_USER);
            return userResponseDto;
        } else if (user.getPwTry() >= pwTryLimit) {
            userResponseDto.setStatus(UserResponseDto.Status.PASSWORD_LIMIT_OVER);
            return userResponseDto;
        } else if (!passwordEncoder.matches(userDto.getPassword(), user.getPw())) {
            userResponseDto.setStatus(UserResponseDto.Status.INVALID_PASSWORD);
            user.setPwTry(user.getPwTry() + 1);
            userRepository.save(user);
            return userResponseDto;
        }
        List<Session> sessionList = user.getSession();

        if (sessionList.size() > 10) {
            sessionList.stream().sorted(Comparator.comparing(Session::getTerminateAt).reversed()).skip(10).forEach(sessionService::deleteSession);
        }

        Session session = sessionService.createSession(user);
        userResponseDto.setStatus(UserResponseDto.Status.SUCCESS);
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

    public UserResponseDto passwordReset() {
        UserResponseDto userResponseDto = new UserResponseDto();
        User user = (User) SecurityContextHolder.getContext().getAuthentication();
        user = getValidUser(user.getEmail());

        if (user == null) {
            userResponseDto.setStatus(UserResponseDto.Status.INVALID_USER);
        } else {
            // pw reset
            String tempPw = Random.RandomString(pwResetSize);

            final String username = "jasuil1212@gmail.com";
            final String password = "vyyqzspyrhfmzivy";

            Properties prop = new Properties();
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", 465);
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.ssl.enable", "true");
            prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            prop.put("mail.smtp.ssl.protocols", "TLSv1.2");

            jakarta.mail.Session session = jakarta.mail.Session.getInstance(prop,
                    new jakarta.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("jasuil1212@gmail.com"));
                message.setRecipients(
                        Message.RecipientType.TO,
                        InternetAddress.parse(user.getEmail())
                );
                message.setSubject("식사일기 임시 비밀번호입니다.");
                String origin = "임시비밀번호: " + tempPw;
                message.setText(origin);

                Transport.send(message);

                userResponseDto.setStatus(UserResponseDto.Status.SUCCESS);
            } catch (Exception e) {
                log.error("임시 비번발급 에러 "  + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return userResponseDto;
    }

    public void updatePw(String pw) throws BizException {
        pw = pw.trim();
        if (pw.length() < 8) throw new BizException("pw length is short");
        boolean isSymbol = false;
        boolean isAlphabet = false;
        boolean isDigit = false;
        for (int i = 0; i < pw.length(); i++) {
            if (!isAlphabet && Character.isAlphabetic(pw.charAt(i))) isAlphabet = true;
            if (!isDigit && Character.isDigit(pw.charAt(i))) isDigit = true;
            if (!isSymbol && String.valueOf(pw.charAt(i)).matches("[^a-zA-Z0-9\\s]")) isSymbol = true;
        }
        if (!isAlphabet || !isDigit || !isSymbol) throw new BizException("invalid pw");

        User user = (User) SecurityContextHolder.getContext().getAuthentication();
        user.setPw(pw);
        userRepository.save(user);
    }
}
