package com.fooddiary.api.controller;


import com.fooddiary.api.dto.response.version.AppVersionResponseDTO;
import com.fooddiary.api.service.version.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class AppVersionController {

    private final VersionService versionService;

    @GetMapping("/version")
    public ResponseEntity<AppVersionResponseDTO> getVersion() {
        return ResponseEntity.ok(versionService.getReleaseVersion());
    }
}
