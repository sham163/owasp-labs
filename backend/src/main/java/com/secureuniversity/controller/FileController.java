package com.secureuniversity.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object upload(@RequestParam("file") MultipartFile file) throws Exception {
        Path dest = Paths.get(uploadDir, file.getOriginalFilename());
        Files.createDirectories(dest.getParent());
        try (InputStream in = file.getInputStream(); OutputStream out = Files.newOutputStream(dest)) {
            StreamUtils.copy(in, out);
        }
        String publicUrl = "/uploads/" + file.getOriginalFilename();
        return Map.of("status","saved","path", publicUrl);
    }

    @GetMapping("/list")
    public Object list() throws IOException {
        File d = new File(uploadDir);
        String[] names = d.list();
        return Map.of("files", names == null? List.of(): Arrays.asList(names));
    }
}
