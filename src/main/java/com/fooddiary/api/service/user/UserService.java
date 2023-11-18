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
import com.fooddiary.api.entity.user.*;
import com.fooddiary.api.repository.user.SessionRepository;
import com.fooddiary.api.repository.user.UserRepository;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fooddiary.api.common.constants.UserConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private static final int PW_EXPIRED_DAY_LIMIT = 90;
    private static final String GOOGLE = "google";
    private static final String KAKAO = "kakao";
    private static final String GOOGLE_AUTH_WEB_CLIENT_ID = "496603773945-n4ksng46582k26b3tk6k3k5tvaal9444.apps.googleusercontent.com"; // todo - ssl인증필요
    private static final String GOOGLE_AUTH_WEB_CLIENT_SECRET = "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";
    private static final String KAKAO_SERVICE_APP_KEY = "217748336456a750c01563ee2749086f";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final SessionRepository sessionRepository;
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
            sessionList.stream().sorted(Comparator.comparing(Session::getTokenTerminateAt).reversed()).skip(10).forEach(sessionService::deleteSession);
        }

        user.setPwTry(0);
        user.setUpdateAt(LocalDateTime.now());
        userRepository.save(user);

        LocalDateTime now = LocalDateTime.now();
        final Session session = sessionService.createSession(user);
        userResponseDto.setStatus(UserResponseDTO.Status.SUCCESS);
        userResponseDto.setToken(session.getToken());
        userResponseDto.setPwExpired(user.getPwUpdateDelayAt().isBefore(LocalDateTime.now()));
        userResponseDto.setRefreshToken(session.getRefreshToken());
        userResponseDto.setTokenExpireAt(session.getTokenTerminateAt().toEpochSecond(ZoneOffset.of("+09:00")) - now.toEpochSecond(ZoneOffset.of("+09:00")));
        userResponseDto.setRefreshTokenExpireAt(session.getRefreshTokenTerminateAt().toEpochSecond(ZoneOffset.of("+09:00")) - now.toEpochSecond(ZoneOffset.of("+09:00")));

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
            // todo- https://developers.google.com/identity/openid-connect/openid-connect?hl=ko#appsetup
            case GOOGLE -> {
                HttpClient client = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofSeconds(5))
                        .proxy(ProxySelector.getDefault())
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?access_token=" + token))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != HttpServletResponse.SC_OK) {
                    return null;
                }
                ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                GoogleTokenInfoResponseDTO googleTokenInfoResponseDTO = objectMapper.readValue(response.body(), GoogleTokenInfoResponseDTO.class);


                if (googleTokenInfoResponseDTO != null) {

                    User user = userRepository.findByEmailAndCreatePathAndStatus(googleTokenInfoResponseDTO.getEmail(), CreatePath.GOOGLE, Status.ACTIVE)
                            .stream()
                            .findFirst()
                            .orElse(null);
                    if (user == null) {
                        user = new User();
                        user.setRole(Role.CLIENT);
                        user.setStatus(Status.ACTIVE);
                        user.setEmail(googleTokenInfoResponseDTO.getEmail());
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
                    return null;
                }
                ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                KakaoUserInfo kakaoUserInfo = objectMapper.readValue(response.body(), KakaoUserInfo.class);

                if (kakaoUserInfo.getKakao_account() == null) {
                    throw new BizException(FAIL_FINDING_USER_INFO_KEY);
                }
                KakaoAccount kakaoAccount = kakaoUserInfo.getKakao_account();
                if (!kakaoAccount.getIs_email_valid() || !kakaoAccount.getIs_email_verified()) {
                    throw new BizException(INVALID_KAKAO_EMAIL_KEY);
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
                        throw new BizException(NOT_ACTIVE_USER_KEY);
                    }
                    if (session.getTokenTerminateAt().isBefore(LocalDateTime.now())) {
                        throw new BizException(LOGIN_REQUEST_KEY);
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

    public RefreshTokenResponseDTO refreshAccessToken(String loginFrom, String refreshToken) throws IOException, InterruptedException {
        RefreshTokenResponseDTO refreshTokenResponseDTO = new RefreshTokenResponseDTO();

        switch (loginFrom) {
            case GOOGLE -> {
                /*
                refreshTokenResponseDTO.setRefreshToken(refreshToken); // 로그인할때만 있으므로 받은것을 다시 넣는다.
                HttpClient client = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofSeconds(5))
                        .proxy(ProxySelector.getDefault())
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?access_token=" + accessToken))
                        .build();
                HttpResponse<String> tokenResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (tokenResponse.statusCode() != HttpServletResponse.SC_OK) { // 엑세스 토큰이 만료되었다면
                    TokenResponse response = new GoogleRefreshTokenRequest(new NetHttpTransport(), new GsonFactory(), refreshToken, GOOGLE_AUTH_WEB_CLIENT_ID, GOOGLE_AUTH_WEB_CLIENT_SECRET).execute();
                    refreshTokenResponseDTO.setToken(response.getAccessToken());
                    refreshTokenResponseDTO.setTokenExpireAt(response.getExpiresInSeconds());
                }

                 */
                try { // https://developers.google.com/identity/protocols/oauth2/web-server?hl=ko#httprest_7  직접 rest api호출(POST https://oauth2.googleapis.com/token)도 가능하나. 제공하는 library를 써봤습니다.
                    TokenResponse response = new GoogleRefreshTokenRequest(new NetHttpTransport(), new GsonFactory(), refreshToken, GOOGLE_AUTH_WEB_CLIENT_ID, GOOGLE_AUTH_WEB_CLIENT_SECRET).execute();
                    refreshTokenResponseDTO.setToken(response.getAccessToken());
                    refreshTokenResponseDTO.setRefreshToken(response.getRefreshToken());
                    refreshTokenResponseDTO.setTokenExpireAt(response.getExpiresInSeconds()); // google은 갱신토큰 만료일을 안준다. 왜냐면 갱신토큰은 로그인할때만 새로 주기 때문이다.
                } catch (TokenResponseException e) {
                    throw new BizException(LOGIN_REQUEST_KEY);
                }
            }
            case KAKAO -> {
                /*
                HttpClient client = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofSeconds(5))
                        .proxy(ProxySelector.getDefault())
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://kapi.kakao.com/v1/user/access_token_info"))
                        .header("Authorization", "Bearer " + accessToken)
                        .build();
                ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                boolean isRefresh = false;
                if (response.statusCode() == HttpServletResponse.SC_BAD_REQUEST) {
                    KakaoKapiErrorResponseDTO kakaoKapiErrorResponseDTO = objectMapper.readValue(response.body(), KakaoKapiErrorResponseDTO.class);
                    if (kakaoKapiErrorResponseDTO.getCode() == -1) {
                        throw new IOException("KAKAO_SEVER_ERROR");
                    } else {
                        throw new BizException("INVALID_FORMAT");
                    }
                } else if (response.statusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                    isRefresh = true;
                }
                objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                KakaoTokenInfoResponseDTO kakaoTokenInfoResponseDTO = objectMapper.readValue(response.body(), KakaoTokenInfoResponseDTO.class);
                if (kakaoTokenInfoResponseDTO.getExpires_in() <= 1) {
                    isRefresh = true;
                }

                if (isRefresh) {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("grant_type", "refresh_token");
                    parameters.put("client_id", KAKAO_SERVICE_APP_KEY);
                    parameters.put("refresh_token", refreshToken);

                    String form = parameters.entrySet()
                                            .stream()
                                            .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                                            .collect(Collectors.joining("&"));

                    request = HttpRequest.newBuilder()
                            .uri(URI.create("https://kauth.kakao.com/oauth/token"))
                            .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                            .POST(HttpRequest.BodyPublishers.ofString(form))
                            .build();
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    KakaoTokenResponseDTO kakaoTokenResponseDTO = objectMapper.readValue(response.body(), KakaoTokenResponseDTO.class);
                    refreshTokenResponseDTO.setToken(kakaoTokenResponseDTO.getAccess_token());
                    refreshTokenResponseDTO.setTokenExpireAt((long) kakaoTokenResponseDTO.getExpires_in()); // 참고용 정보임
                    refreshTokenResponseDTO.setRefreshToken(kakaoTokenResponseDTO.getRefresh_token());
                }
                */

                Map<String, String> parameters = new HashMap<>();
                parameters.put("grant_type", "refresh_token");
                parameters.put("client_id", KAKAO_SERVICE_APP_KEY);
                parameters.put("refresh_token", refreshToken);

                String form = parameters.entrySet()
                                        .stream()
                                        .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                                        .collect(Collectors.joining("&"));

                HttpRequest request = HttpRequest.newBuilder()
                                     .uri(URI.create("https://kauth.kakao.com/oauth/token"))
                                     .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                                     .POST(HttpRequest.BodyPublishers.ofString(form))
                                     .build();
                HttpClient client = HttpClient.newBuilder()
                                              .version(HttpClient.Version.HTTP_1_1)
                                              .connectTimeout(Duration.ofSeconds(5))
                                              .proxy(ProxySelector.getDefault())
                                              .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != HttpServletResponse.SC_OK) {
                    throw new BizException(LOGIN_REQUEST_KEY);
                }

                ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                KakaoTokenResponseDTO kakaoTokenResponseDTO = objectMapper.readValue(response.body(), KakaoTokenResponseDTO.class);
                refreshTokenResponseDTO.setToken(kakaoTokenResponseDTO.getAccess_token());
                refreshTokenResponseDTO.setTokenExpireAt((long) kakaoTokenResponseDTO.getExpires_in()); // 참고용 정보임
                refreshTokenResponseDTO.setRefreshToken(kakaoTokenResponseDTO.getRefresh_token());
                refreshTokenResponseDTO.setRefreshTokenExpireAt((long) kakaoTokenResponseDTO.getRefresh_token_expires_in());
            }
            default -> {
                Session session = sessionRepository.findByRefreshToken(refreshToken);
                LocalDateTime now = LocalDateTime.now();
                if (session == null || !session.getRefreshToken().equals(refreshToken) || session.getRefreshTokenTerminateAt().isBefore(now)) {
                    throw new BizException(LOGIN_REQUEST_KEY); // 로그인을 너무 오래 유지하는 것도 문제다.
                }
                sessionRepository.delete(session);

                session.setToken(passwordEncoder.encode(session.getUser().getEmail() + now));
                session.setTokenTerminateAt(now.plusDays(1));
                sessionRepository.save(session);
                refreshTokenResponseDTO.setToken(session.getToken());
                refreshTokenResponseDTO.setTokenExpireAt(session.getTokenTerminateAt().toEpochSecond(ZoneOffset.of("+09:00")) - now.toEpochSecond(ZoneOffset.of("+09:00")));
                refreshTokenResponseDTO.setRefreshToken(refreshToken);
            }
        }
        return refreshTokenResponseDTO;
    }
    public void resign(String loginFrom, String token, User user)
            throws IOException, InterruptedException {
        switch (loginFrom) {
            case GOOGLE -> {
                unlinkGoogle(token);
                if (user != null) {
                    user.setStatus(Status.SUSPENDED);
                    userRepository.save(user);
                    userResignService.resign(user);
                }
            }
            case KAKAO -> {
                unlinkKakao(token);
                if (user != null) {
                    user.setStatus(Status.SUSPENDED);
                    userRepository.save(user);
                    userResignService.resign(user);
                }
            }
            default -> {
                final Session session = sessionService.getSession(token);
                if (session != null) {
                    if (user.getStatus() == Status.ACTIVE && user.getCreatePath() == CreatePath.NONE) {
                        user.setStatus(Status.SUSPENDED);
                        userRepository.save(user);
                        userResignService.resign(user);
                        sessionService.deleteSession(session);
                    }
                }
            }
        }
    }

    /**
     * 내 계정에 앱이 로그아웃, 이용해제 되었는지 확인
     * https://support.google.com/accounts/answer/3466521?hl=ko#remove-access
     * @param token
     * @throws IOException
     * @throws InterruptedException
     */
    private static void unlinkGoogle(String token) throws IOException, InterruptedException {
        /*
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .proxy(ProxySelector.getDefault())
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?access_token=" + token))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != HttpServletResponse.SC_OK) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        GoogleTokenInfoResponseDTO googleTokenInfoResponseDTO = objectMapper.readValue(response.body(), GoogleTokenInfoResponseDTO.class);
        if (googleTokenInfoResponseDTO == null || googleTokenInfoResponseDTO.getEmail() == null) {
            throw new BizException("INVALID_USER");
        }
        */
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .proxy(ProxySelector.getDefault())
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/revoke?token=" + token)) // 그냥 로그아웃과 동일..
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HttpServletResponse.SC_OK) {
            throw new BizException(UNLINK_FAIL_KEY);
        }
    }
    private static void unlinkKakao(String token) throws IOException, InterruptedException {

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
            throw new BizException(UNLINK_FAIL_KEY);
        }

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        KakaoUserInfo kakaoUserInfo = objectMapper.readValue(response.body(), KakaoUserInfo.class);

        if (kakaoUserInfo.getKakao_account() == null) {
            throw new BizException(FAIL_FINDING_USER_INFO_KEY);
        }
        KakaoAccount kakaoAccount = kakaoUserInfo.getKakao_account();
        if (!kakaoAccount.getIs_email_valid() || !kakaoAccount.getIs_email_verified()) {
            throw new BizException(INVALID_KAKAO_EMAIL_KEY);
        }

        final String form = "target_id_type=user_id&" + "target_id=" + kakaoUserInfo.getId();
        request = HttpRequest.newBuilder()
                                         .uri(URI.create("https://kapi.kakao.com/v1/user/unlink"))
                                         .POST(HttpRequest.BodyPublishers.ofString(form))
                                         .header("Authorization", "KakaoAK " + KAKAO_SERVICE_APP_KEY)
                                         .headers("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                         .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        KakaoUnlink kakaoUnlink = objectMapper.readValue(response.body(), KakaoUnlink.class);

        if (!kakaoUnlink.getId().equals(kakaoUserInfo.getId())) {
            throw new BizException(UNLINK_FAIL_KEY);
        }

    }

    /**
     * 로그인할때 id token이 아닌 access token을 가져오는 방법
     * <a href="https://idlecomputer.tistory.com/310">사용법 예제</a>
     * <a href="https://developers.google.com/identity/protocols/oauth2/web-server?hl=ko#obtainingaccesstokens">구글 문서</a>
     * @param request
     * @param response
     * @throws Exception
     */
    public void googleSignCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String code = request.getParameter("code");
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> rest_request = getMultiValueMapHttpEntity(code, request.getRequestURL().toString(), headers);

        URI uri = URI.create("https://oauth2.googleapis.com/token");

        ResponseEntity<Map> rest_reponse;
        rest_reponse = restTemplate.postForEntity(uri, rest_request, Map.class);
        log.info("response body: {}",rest_reponse.getBody());

        StringBuilder sb = new StringBuilder();
        // 로그인할때만 refresh 토큰이 부여된다.
        if (rest_reponse.getBody().get("refresh_token") != null) {
            sb.append("&refresh-token=").append(rest_reponse.getBody().get("refresh_token"));
        }
        sb.append("&token=").append(rest_reponse.getBody().get("access_token"));
        String queryString = sb.toString().replaceFirst("&", "?");

        response.sendRedirect(request.getRequestURL().toString().replace(request.getRequestURI(), "") + queryString);
    }

    @NotNull
    private static HttpEntity<MultiValueMap<String, String>> getMultiValueMapHttpEntity(String code, String url, HttpHeaders headers) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("code", code);
        parameters.add("client_id", GOOGLE_AUTH_WEB_CLIENT_ID);
        parameters.add("client_secret", GOOGLE_AUTH_WEB_CLIENT_SECRET);
        parameters.add("grant_type", "authorization_code");
        parameters.add("redirect_uri", url); // 현재 서버의 url

        return new HttpEntity<>(parameters, headers);
    }
    public void logout(String loginFrom, String accessToken) throws IOException, InterruptedException {
        switch (loginFrom) {
            case GOOGLE -> {
                HttpClient client = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofSeconds(5))
                        .proxy(ProxySelector.getDefault())
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://accounts.google.com/o/oauth2/revoke?token=" + accessToken))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != HttpServletResponse.SC_OK) {
                    throw new BizException(LOGOUT_FAIL_KEY);
                }
            }
            case KAKAO -> {
                HttpClient client = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofSeconds(5))
                        .proxy(ProxySelector.getDefault())
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://kapi.kakao.com/v1/user/logout"))
                        .header("Authorization", "Bearer " + accessToken)
                        .headers("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != HttpServletResponse.SC_OK) {
                    throw new BizException(LOGOUT_FAIL_KEY);
                }
            }
            default -> {
                Session session = sessionService.getSession(accessToken);
                if (session != null) {
                    sessionService.deleteSession(session);
                }
            }
        }
    }
}
