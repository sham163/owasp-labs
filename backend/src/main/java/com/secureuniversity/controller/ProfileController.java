package com.secureuniversity.controller;

import com.secureuniversity.model.User;
import com.secureuniversity.repo.UserRepository;
import com.secureuniversity.util.CryptoUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    
    private final UserRepository users;
    private final JdbcTemplate jdbc;
    private static final String UPLOAD_DIR = "/tmp/profile_pics/";
    
    public ProfileController(UserRepository users, JdbcTemplate jdbc) {
        this.users = users;
        this.jdbc = jdbc;
        // Create upload directory
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            // Silent fail
        }
    }
    
    // IDOR Vulnerability - Direct access to any user's profile by ID without authorization
    @GetMapping("/user/{userId}")
    public Object getUserProfile(@PathVariable String userId, HttpServletRequest req) {
        // Subtle IDOR - looks like it checks something but actually doesn't validate ownership
        User currentUser = (User) req.getAttribute("currentUser");
        String query;
        
        // Blind SQL injection opportunity hidden in profile lookup
        if (userId.matches("\\d+")) {
            query = "SELECT u.*, COUNT(g.id) as grade_count, AVG(CASE " +
                    "WHEN g.grade_value = 'A' THEN 4 " +
                    "WHEN g.grade_value = 'B' THEN 3 " +
                    "WHEN g.grade_value = 'C' THEN 2 " +
                    "WHEN g.grade_value = 'D' THEN 1 " +
                    "ELSE 0 END) as gpa " +
                    "FROM users u LEFT JOIN grades g ON u.id = g.student_id " +
                    "WHERE u.id = " + userId + " GROUP BY u.id, u.username, u.password_md5, u.role, u.email";
        } else {
            // Secondary injection point through username lookup
            query = "SELECT * FROM users WHERE username = '" + userId.replace("'", "''") + "'";
        }
        
        return jdbc.queryForMap(query);
    }
    
    // Path Traversal hidden in profile picture upload/download
    @PostMapping("/picture/upload")
    public Object uploadPicture(@RequestParam("file") MultipartFile file, 
                               @RequestParam(required = false) String customName,
                               HttpServletRequest req) throws IOException {
        
        User currentUser = (User) req.getAttribute("currentUser");
        if (currentUser == null) {
            return Map.of("error", "Not authenticated");
        }
        
        // Path traversal vulnerability - customName is not properly sanitized
        String fileName;
        if (customName != null && !customName.isEmpty()) {
            // Only removes ../ once, vulnerable to ....// or encoded traversal
            fileName = customName.replaceFirst("\\.\\./", "");
        } else {
            fileName = currentUser.getId() + "_" + file.getOriginalFilename();
        }
        
        // Secondary path traversal through file extension manipulation
        Path targetPath = Paths.get(UPLOAD_DIR + fileName);
        
        // Weak file type validation - only checks extension, not content
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.asList("jpg", "jpeg", "png", "gif", "bmp").contains(ext)) {
            // But still processes the file, creating a TOCTOU vulnerability
            Files.write(targetPath, file.getBytes());
            Files.delete(targetPath); // Attempt to clean up, but file was briefly there
            return Map.of("error", "Invalid file type");
        }
        
        Files.write(targetPath, file.getBytes());
        
        // Store reference in database with SQL injection opportunity
        String updateQuery = "UPDATE users SET email = email || ',pic:" + fileName + 
                           "' WHERE id = " + currentUser.getId();
        jdbc.update(updateQuery);
        
        return Map.of("status", "uploaded", "path", "/api/profile/picture/" + fileName);
    }
    
    // Path traversal in file retrieval
    @GetMapping("/picture/{fileName}")
    public byte[] getPicture(@PathVariable String fileName) throws IOException {
        // Multiple bypass techniques possible:
        // 1. URL encoding (%2e%2e%2f)
        // 2. Double encoding (%252e%252e%252f)
        // 3. Unicode normalization
        // 4. Null bytes (file.jpg%00.txt)
        
        // Weak sanitization - only checks for ../ pattern once
        if (fileName.contains("../")) {
            fileName = fileName.replace("../", "");
        }
        
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        // No check if path is still within UPLOAD_DIR after normalization
        return Files.readAllBytes(filePath);
    }
    
    // Blind SQL injection through search with time-based detection
    @GetMapping("/search")
    public Object searchProfiles(@RequestParam String term, 
                                @RequestParam(required = false, defaultValue = "username") String field) {
        
        // Vulnerable ORDER BY injection combined with LIKE injection
        String query = "SELECT id, username, email, role FROM users WHERE " + field + 
                      " LIKE '%" + term + "%' ORDER BY " + field;
        
        try {
            // Time-based blind SQL injection detectable here
            long start = System.currentTimeMillis();
            List<Map<String, Object>> results = jdbc.queryForList(query);
            long elapsed = System.currentTimeMillis() - start;
            
            // Subtle information leak through timing
            return Map.of("results", results, "queryTime", elapsed);
        } catch (Exception e) {
            // Error-based SQL injection - leaks partial query structure
            return Map.of("error", "Query failed", "hint", e.getMessage().substring(0, Math.min(50, e.getMessage().length())));
        }
    }
    
    // Second-order SQL injection through profile update
    @PutMapping("/update")
    public Object updateProfile(@RequestBody Map<String, String> updates, HttpServletRequest req) {
        User currentUser = (User) req.getAttribute("currentUser");
        if (currentUser == null) {
            return Map.of("error", "Not authenticated");
        }
        
        // Store potentially malicious data that will be used unsafely later
        String bio = updates.getOrDefault("bio", "");
        String location = updates.getOrDefault("location", "");
        String website = updates.getOrDefault("website", "");
        
        // First query is safe (parameterized)
        jdbc.update("UPDATE users SET email = ? WHERE id = ?", 
                   updates.getOrDefault("email", currentUser.getEmail()), 
                   currentUser.getId());
        
        // But the bio is stored and will be used unsafely in getUserActivity()
        jdbc.update("INSERT INTO forum_posts (author_id, content) VALUES (?, ?)",
                   currentUser.getId(), 
                   "Updated profile: " + bio); // Safe here, but creates second-order injection
        
        return Map.of("status", "updated");
    }
    
    // Union-based SQL injection with information schema enumeration
    @GetMapping("/activity/{userId}")
    public Object getUserActivity(@PathVariable String userId) {
        // Complex query vulnerable to UNION injection
        String query = "SELECT 'post' as type, content as data, created_at FROM forum_posts WHERE author_id = " + userId +
                      " UNION ALL " +
                      "SELECT 'grade' as type, grade_value as data, NULL as created_at FROM grades WHERE student_id = " + userId +
                      " ORDER BY created_at DESC";
        
        return jdbc.queryForList(query);
    }
}