package com.fooddiary.api.entity.image;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ImageTest {

    @Test
    public void imageTest() {
        LocalDateTime now = LocalDateTime.now();
        Image image = Image.createImage(now.minusDays(2), "Practice");
        System.out.println("image = " + image);
        System.out.println("image = " + image.getTime());
    }

}