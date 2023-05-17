package com.fooddiary.api.controller;

import java.io.IOException;
import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fooddiary.api.FileStorageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/storage")
public class FileStorageController {


    @Autowired
    FileStorageService fileStorageService;

    // @GetMapping("/s3")
    public ResponseEntity<byte[]> getObject(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            final byte[] fileBuffer = fileStorageService.getObject("");
            return ResponseEntity.ok()
                                 .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode("test.JPG", "utf-8").replaceAll("\\+", "%20") + ';')
                                 .body(fileBuffer);

    }
}
