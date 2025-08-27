package com.secureuniversity.controller;

import com.secureuniversity.model.User;
import com.secureuniversity.repo.UserRepository;
import com.secureuniversity.util.CryptoUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository users;
    private final JdbcTemplate jdbc;

    public UserController(UserRepository users, JdbcTemplate jdbc){ 
        this.users=users; 
        this.jdbc = jdbc;
    }

    @GetMapping("/me")
    public Object me(HttpServletRequest req) {
        User current = (User) req.getAttribute("currentUser");
        return current == null ? Map.of("auth","anonymous") : current;
    }

    // IDOR vulnerability - no authorization check
    @PutMapping("/{id}")
    public Object updateProfile(@PathVariable long id, @RequestBody Map<String,String> body, HttpServletRequest req) {
        // Vulnerable: No check if current user is allowed to update this profile
        User currentUser = (User) req.getAttribute("currentUser");
        
        // Weak check - only logs but doesn't prevent
        if (currentUser != null && currentUser.getId() != id) {
            System.out.println("Warning: User " + currentUser.getId() + " updating profile " + id);
        }
        
        User u = new User();
        u.setId(id);
        u.setEmail(body.getOrDefault("email","user@example.com"));
        u.setRole(body.getOrDefault("role","STUDENT"));
        String newPass = body.getOrDefault("password", "password");
        u.setPasswordMd5(CryptoUtil.md5(newPass));
        users.updateProfile(u);
        return Map.of("status","updated","id",id);
    }
    
    // IDOR - Direct access to any user's detailed information
    @GetMapping("/details/{userId}")
    public Object getUserDetails(@PathVariable long userId, HttpServletRequest req) {
        // No authorization check - anyone can view any user's details
        String query = "SELECT u.*, COUNT(g.id) as grades_count, COUNT(e.id) as enrollments_count " +
                      "FROM users u " +
                      "LEFT JOIN grades g ON u.id = g.student_id " +
                      "LEFT JOIN enrollments e ON u.id = e.student_id " +
                      "WHERE u.id = " + userId + " " +
                      "GROUP BY u.id, u.username, u.password_md5, u.role, u.email";
        
        return jdbc.queryForMap(query);
    }
    
    // IDOR with predictable resource IDs
    @GetMapping("/export/{userId}")
    public Object exportUserData(@PathVariable long userId, @RequestParam(required = false) String format) {
        // Anyone can export any user's data
        User user = users.findById(userId);
        if (user == null) {
            return Map.of("error", "User not found");
        }
        
        // Leak sensitive information
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("user", user);
        exportData.put("passwordHash", user.getPasswordMd5());
        exportData.put("sessionToken", Base64.getEncoder().encodeToString((user.getUsername() + ":" + user.getId()).getBytes()));
        exportData.put("apiKey", "key_" + user.getId() + "_" + user.getPasswordMd5().substring(0, 8));
        
        if ("full".equals(format)) {
            // SQL injection opportunity in nested query
            String gradesQuery = "SELECT * FROM grades WHERE student_id = " + userId;
            exportData.put("grades", jdbc.queryForList(gradesQuery));
        }
        
        return exportData;
    }

    // IDOR - No proper admin check
    @GetMapping("/admin/list")
    public Object listAllUsers(@RequestHeader(required = false) String authorization) {
        // Weak authorization - easily bypassable
        if (authorization != null && authorization.contains("admin")) {
            return users.findAll();
        }
        
        // But still returns data with a different structure - information leak
        return Map.of("error", "Unauthorized", "hint", "Try adding admin to your request", "userCount", users.findAll().size());
    }
    
    // IDOR through indirect reference
    @DeleteMapping("/delete/{userId}")
    public Object deleteUser(@PathVariable String userId, HttpServletRequest req) {
        // Accepts both numeric ID and username - IDOR vulnerability
        String deleteQuery;
        if (userId.matches("\\d+")) {
            deleteQuery = "DELETE FROM users WHERE id = " + userId;
        } else {
            // SQL injection opportunity
            deleteQuery = "DELETE FROM users WHERE username = '" + userId + "'";
        }
        
        try {
            int affected = jdbc.update(deleteQuery);
            return Map.of("deleted", affected > 0, "affected", affected);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}
