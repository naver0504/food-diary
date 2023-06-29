package com.fooddiary.api.service;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.common.CommonSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;


@SpringBootTest
class ImageServiceTest {

    @Autowired
    ImageService imageService;

    @Test
    @Transactional
    public void imgServiceTest() throws IOException {

        String filePath = "C:\\Users\\qortm\\OneDrive\\사진\\Saved Pictures\\images\\apple";
        File file = new File(filePath);
        DiskFileItem fileItem = new DiskFileItem("file", Files.probeContentType(file.toPath()), false, file.getName(), (int) file.length() , file.getParentFile());

        InputStream input = new FileInputStream(file);
        OutputStream os = fileItem.getOutputStream();
        IOUtils.copy(input, os);


    }


}