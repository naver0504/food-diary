package com.fooddiary.api.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class AppVersionController {

    @AllArgsConstructor
    @Getter
    class AppVersion {
        private String appVersion;
    }

    @Value("${app.version}")
    private String appVersion;

    @GetMapping("/version")
    public ResponseEntity<AppVersion> getVersion() {

        return ResponseEntity.ok(new AppVersion(appVersion));
    }
}
