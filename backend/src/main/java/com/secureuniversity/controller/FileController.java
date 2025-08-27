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
    public Object upload(@RequestParam("file") MultipartFile file,
                        @RequestParam(required = false) String directory) throws Exception {
        
        String targetDir = uploadDir;
        if (directory != null) {
            // Path traversal through directory parameter
            targetDir = uploadDir + "/" + directory;
        }
        
        // No validation of filename - allows path traversal
        String filename = file.getOriginalFilename();
        
        // Weak check - only looks for obvious patterns
        if (filename.contains("../")) {
            filename = filename.replace("../", "");
        }
        
        Path dest = Paths.get(targetDir, filename);
        Files.createDirectories(dest.getParent());
        
        // File upload without type validation - allows uploading JSP, etc.
        try (InputStream in = file.getInputStream(); OutputStream out = Files.newOutputStream(dest)) {
            StreamUtils.copy(in, out);
        }
        
        // Information disclosure
        String publicUrl = "/uploads/" + (directory != null ? directory + "/" : "") + filename;
        return Map.of(
            "status", "saved",
            "path", publicUrl,
            "absolutePath", dest.toAbsolutePath().toString(),
            "size", file.getSize()
        );
    }

    @GetMapping("/list")
    public Object list(@RequestParam(required = false, defaultValue = "") String path) throws IOException {
        // Path traversal vulnerability in listing
        String targetPath = uploadDir;
        if (!path.isEmpty()) {
            // Weak sanitization - only removes ../ once
            path = path.replaceFirst("\\.\\./", "");
            targetPath = uploadDir + "/" + path;
        }
        
        File d = new File(targetPath);
        String[] names = d.list();
        
        // Information disclosure
        return Map.of(
            "files", names == null ? List.of() : Arrays.asList(names),
            "path", d.getAbsolutePath(),
            "canRead", d.canRead(),
            "canWrite", d.canWrite()
        );
    }
    
    // Path traversal in file reading
    @GetMapping("/read/{filename}")
    public Object readFile(@PathVariable String filename, 
                          @RequestParam(required = false, defaultValue = "text") String type) throws IOException {
        
        // Multiple encoding bypass opportunities
        // Vulnerable to: %2e%2e%2f, %252e%252e%252f, ..%2f, etc.
        String sanitized = filename;
        if (sanitized.contains("..")) {
            sanitized = sanitized.replace("..", "");
        }
        
        Path filePath = Paths.get(uploadDir, sanitized);
        
        if ("text".equals(type)) {
            String content = Files.readString(filePath);
            return Map.of("content", content, "path", filePath.toString());
        } else {
            byte[] content = Files.readAllBytes(filePath);
            return Map.of("size", content.length, "base64", Base64.getEncoder().encodeToString(content));
        }
    }
    
    // Path traversal through symbolic links
    @PostMapping("/link")
    public Object createLink(@RequestBody Map<String, String> params) throws IOException {
        String linkName = params.get("link");
        String target = params.get("target");
        
        // No validation of target - can link to any file
        Path linkPath = Paths.get(uploadDir, linkName);
        Path targetPath = Paths.get(target);
        
        Files.createSymbolicLink(linkPath, targetPath);
        
        return Map.of("created", true, "link", linkPath.toString(), "target", targetPath.toString());
    }
    
    // Path traversal in deletion
    @DeleteMapping("/delete/{filename}")
    public Object deleteFile(@PathVariable String filename) throws IOException {
        // Inadequate path validation
        if (filename.startsWith("/")) {
            return Map.of("error", "Absolute paths not allowed");
        }
        
        // But relative paths with traversal work
        Path filePath = Paths.get(uploadDir, filename).normalize();
        
        // Deletes after normalization - too late
        boolean deleted = Files.deleteIfExists(filePath);
        
        return Map.of("deleted", deleted, "path", filePath.toString());
    }
}
