package com.fooddiary.api.repository;

import com.fooddiary.api.entity.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {


    @Query("select i from Image i left join fetch i.tags T where i.id = :id and i.user.id = :userId")
    Optional<Image> findByIdWithTag(@Param("id") int id, @Param("userId") int userId);

    @Query("select i from Image i where i.parentImage.id = :id order by i.id asc")
    List<Image> findByParentImageId(@Param("id") int id);



}
