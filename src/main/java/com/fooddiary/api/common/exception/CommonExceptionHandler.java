package com.fooddiary.api.common.exception;

import com.fooddiary.api.dto.response.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@ControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ErrorResponseDTO> RuntimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        final ErrorResponseDTO response = new ErrorResponseDTO("system error");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BizException.class)
    protected ResponseEntity<ErrorResponseDTO> RuntimeExceptionHandler(BizException e) {
        log.error("BizException", e);
        final ErrorResponseDTO response = new ErrorResponseDTO(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex) {

        /*
        final String username = "jasuil1212@gmail.com";
        final String password = "vyyqzspyrhfmzivy";

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", 465);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        prop.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(prop,
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
                    InternetAddress.parse("jasuil1212@gmail.com, jasuil@daum.net")
            );
            message.setSubject("식사일기 에러로그");
            String origin = "where: " + request.getRequestURI() + " => " + StringUtils.truncate(ex.toString(), 100);
            message.setText(origin);

            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

         */

        return null;
    }
}
