package com.fooddiary.api.service.user;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.common.util.Random;
import com.fooddiary.api.dto.request.user.UserLoginRequestDTO;
import com.fooddiary.api.dto.request.user.UserNewPasswordRequestDTO;
import com.fooddiary.api.dto.request.user.UserNewRequestDTO;
import com.fooddiary.api.dto.response.user.*;
import com.fooddiary.api.dto.response.user.KakaoUserInfo.KakaoAccount;
import com.fooddiary.api.entity.user.Session;
import com.fooddiary.api.entity.user.CreatePath;
import com.fooddiary.api.entity.user.Role;
import com.fooddiary.api.entity.user.Status;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.user.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.BindException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private static final int PW_EXPIRED_DAY_LIMIT = 90;
    private static final String GOOGLE = "google";
    private static final String KAKAO = "kakao";
    private static final String GOOGLE_AUTH_WEB_CLIENT_ID = "496603773945-n4ksng46582k26b3tk6k3k5tvaal9444.apps.googleusercontent.com"; // todo - ssl인증필요
    private static final String KAKAO_SERVICE_APP_KEY = "217748336456a750c01563ee2749086f";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final UserResignService userResignService;
    private final String EMAIL_PATTERN = "^(.+)@(\\S+)$";
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
            } else if (!Pattern.compile(EMAIL_PATTERN).matcher(userDto.getEmail()).matches()) {
                userResponseDto.setStatus(UserResponseDTO.Status.INVALID_EMAIL);
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

    public UserInfoResponseDTO getUserInfo(User user) {
        final UserInfoResponseDTO userInfoResponseDTO = new UserInfoResponseDTO();
        userInfoResponseDTO.setRole(user.getRole());
        userInfoResponseDTO.setStatus(user.getStatus());
        return userInfoResponseDTO;
    }

    public UserResponseDTO loginUser(UserLoginRequestDTO userDto) {
        final UserResponseDTO userResponseDto = new UserResponseDTO();
        final User user = userRepository.findByEmailAndCreatePathAndStatus(userDto.getEmail(), CreatePath.NONE, Status.ACTIVE)
                .stream()
                .findFirst()
                .orElse(null);

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
     * @param token request header 에서 가져옴
     * @return 유효하면 User 객체반환
     */
    @Nullable
    public User getValidUser(String loginFrom, String token) throws GeneralSecurityException, IOException, InterruptedException {
        // todo
        if (!StringUtils.hasText(loginFrom) || !StringUtils.hasText(token)) {return null;}
        switch (loginFrom) {
            case GOOGLE -> {
                HttpTransport transport = new NetHttpTransport();
                JsonFactory jsonFactory = new GsonFactory();
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                        // Specify the CLIENT_ID of the app that accesses the backend:
                        .setAudience(Collections.singletonList(GOOGLE_AUTH_WEB_CLIENT_ID)) // todo - test, android와 ios와 web용 구분이 필요함
                        // Or, if multiple clients access the backend:
                        //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                        .build();

// (Receive idTokenString by HTTPS POST)

                GoogleIdToken idToken = verifier.verify(token);
                if (idToken != null) {
                    GoogleIdToken.Payload payload = idToken.getPayload();

                    // Print user identifier
                    String userId = payload.getSubject();

                    // Get profile information from payload
                    String email = payload.getEmail();
                    boolean emailVerified = payload.getEmailVerified();
                    String name = (String) payload.get("name");
                    String pictureUrl = (String) payload.get("picture");
                    String locale = (String) payload.get("locale");
                    String familyName = (String) payload.get("family_name");
                    String givenName = (String) payload.get("given_name");

                    User user = userRepository.findByEmailAndCreatePathAndStatus(email, CreatePath.GOOGLE, Status.ACTIVE)
                            .stream()
                            .findFirst()
                            .orElse(null);
                    if (user == null) {
                        user = new User();
                        user.setRole(Role.CLIENT);
                        user.setStatus(Status.ACTIVE);
                        user.setEmail(email);
                        user.setCreatePath(CreatePath.GOOGLE);
                    }
                    user.setLastAccessAt(LocalDateTime.now());
                    userRepository.save(user);
                    return user;

                } else {
                    return null;
                }
            }
            case KAKAO -> {
                HttpClient client = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofSeconds(5))
                        .proxy(ProxySelector.getDefault())
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://kapi.kakao.com/v2/user/me"))
                        .header("Authorization", "Bearer " + token)
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != HttpServletResponse.SC_OK) {
                    throw new BizException("kakao user info api error");
                }
                ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                KakaoUserInfo kakaoUserInfo = objectMapper.readValue(response.body(), KakaoUserInfo.class);

                if (kakaoUserInfo.getKakao_account() == null) {
                    throw new BizException("fail finding user info");
                }
                KakaoAccount kakaoAccount = kakaoUserInfo.getKakao_account();
                if (!kakaoAccount.getIs_email_valid() || !kakaoAccount.getIs_email_verified()) {
                    throw new BizException("invalid kakao email");
                }

                User user = userRepository.findByEmailAndCreatePathAndStatus(kakaoUserInfo.getKakao_account().getEmail(), CreatePath.KAKAO, Status.ACTIVE)
                        .stream()
                        .findFirst()
                        .orElse(null);
                if (user == null) {
                    user = new User();
                    user.setRole(Role.CLIENT);
                    user.setStatus(Status.ACTIVE);
                    user.setEmail(kakaoAccount.getEmail());
                    user.setCreatePath(CreatePath.KAKAO);

                }
                user.setLastAccessAt(LocalDateTime.now());
                userRepository.save(user);
                return user;
            }
            default -> {
                final Session session = sessionService.getSession(token);
                if (session != null) {
                    if (session.getUser().getStatus() != Status.ACTIVE) {
                        return null;
                    }
                    User user = session.getUser();
                    user.setLastAccessAt(LocalDateTime.now());
                    userRepository.save(user);
                    return user;
                }
            }
        }

        return null;
    }

    /**
     * 유효한 식사일기 전용계정을 반환합니다.
     * @param email
     * @return
     */
    @Nullable
    public User getValidUser(String email) {
        if (!StringUtils.hasText(email)) {return null;}

        return userRepository.findByEmailAndCreatePathAndStatus(email, CreatePath.NONE, Status.ACTIVE)
                .stream()
                .findFirst()
                .orElse(null);
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
                    new jakarta.mail.Authenticator() {
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

                final LocalDateTime now = LocalDateTime.now();
                user.setPw(passwordEncoder.encode(tempPw));
                user.setPwTry(0);
                user.setPwUpdateAt(now);
                user.setPwUpdateDelayAt(now.plusDays(PW_EXPIRED_DAY_LIMIT));
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

    public void resign(String loginFrom, String token)
            throws IOException, InterruptedException, GeneralSecurityException {
        switch (loginFrom) {
            case GOOGLE -> {
                HttpTransport transport = new NetHttpTransport();
                JsonFactory jsonFactory = new GsonFactory();
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                        // Specify the CLIENT_ID of the app that accesses the backend:
                        .setAudience(Collections.singletonList(GOOGLE_AUTH_WEB_CLIENT_ID)) // todo - test, android와 ios와 web용 구분이 필요함
                        // Or, if multiple clients access the backend:
                        //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                        .build();

// (Receive idTokenString by HTTPS POST)

                GoogleIdToken idToken = verifier.verify(token);
                if (idToken != null) {
                    GoogleIdToken.Payload payload = idToken.getPayload();

                    // Print user identifier
                    String userId = payload.getSubject();

                    // Get profile information from payload
                    String email = payload.getEmail();
                    boolean emailVerified = payload.getEmailVerified();

                    User user = userRepository.findByEmailAndCreatePathAndStatus(email, CreatePath.GOOGLE, Status.ACTIVE)
                            .stream()
                            .findFirst()
                            .orElse(null);

                    if (user != null) {
                        user.setStatus(Status.SUSPENDED);
                        userRepository.save(user);
                        userResignService.resign(user);
                    }
                }
            }
            case KAKAO -> {
                String email = unlinkKakao(token);
                User user = userRepository.findByEmailAndCreatePathAndStatus(email, CreatePath.KAKAO, Status.ACTIVE)
                        .stream()
                        .findFirst()
                        .orElse(null);

                if (user != null) {
                    user.setStatus(Status.SUSPENDED);
                    userRepository.save(user);
                    userResignService.resign(user);
                }
            }
            default -> {
                final Session session = sessionService.getSession(token);
                if (session != null) {
                    User user = session.getUser();
                    if (user.getStatus() == Status.ACTIVE && user.getCreatePath() == CreatePath.NONE) {
                        user.setStatus(Status.SUSPENDED);
                        userRepository.save(user);
                        userResignService.resign(user);
                    }
                }
            }
        }
    }
    public String unlinkKakao(String token) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newBuilder()
                                      .version(HttpClient.Version.HTTP_1_1)
                                      .connectTimeout(Duration.ofSeconds(5))
                                      .proxy(ProxySelector.getDefault())
                                      .build();
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create("https://kapi.kakao.com/v2/user/me"))
                                         .header("Authorization", "Bearer " + token)
                                         .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HttpServletResponse.SC_OK) {
            throw new BizException("kakao user info api error");
        }

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        KakaoUserInfo kakaoUserInfo = objectMapper.readValue(response.body(), KakaoUserInfo.class);

        if (kakaoUserInfo.getKakao_account() == null) {
            throw new BizException("fail finding user info");
        }
        KakaoAccount kakaoAccount = kakaoUserInfo.getKakao_account();
        if (!kakaoAccount.getIs_email_valid() || !kakaoAccount.getIs_email_verified()) {
            throw new BizException("invalid kakao email");
        }

        final String form = "target_id_type=user_id&" + "target_id=" + kakaoUserInfo.getId();
        request = HttpRequest.newBuilder()
                                         .uri(URI.create("https://kapi.kakao.com/v1/user/unlink"))
                                         .POST(HttpRequest.BodyPublishers.ofString(form))
                                         .header("Authorization", "KakaoAK " + KAKAO_SERVICE_APP_KEY)
                                         .headers("Content-Type", "application/x-www-form-urlencoded")
                                         .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        KakaoUnlink kakaoUnlink = objectMapper.readValue(response.body(), KakaoUnlink.class);

        if (!kakaoUnlink.getId().equals(kakaoUserInfo.getId())) {
            throw new BindException("fail unlinking kakao");
        }

        return kakaoAccount.getEmail();
    }
}
