package com.fooddiary.api.service.version;

import com.fooddiary.api.dto.response.version.AppVersionResponseDTO;
import com.fooddiary.api.repository.version.VersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VersionService {

    private final VersionRepository versionRepository;

    public AppVersionResponseDTO getReleaseVersion() {
        LocalDateTime now = LocalDateTime.now();
        String version = versionRepository.findLatestReleaseVersion(now);
        return AppVersionResponseDTO.builder()
                .version(version)
                .build();

    }
}
