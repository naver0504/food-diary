package com.fooddiary.api.repository.version;

import com.fooddiary.api.entity.version.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface VersionRepository extends JpaRepository<Version, Integer> {

    @Query("select v.version from Version v where v.releaseAt <= :now and v.isRelease = true order by v.releaseAt desc limit 1")
    String findLatestReleaseVersion(LocalDateTime now);

}
