package com.secureuniversity.controller;

import com.secureuniversity.model.User;
import com.secureuniversity.repo.UserRepository;
import com.secureuniversity.util.CryptoUtil;
import jakarta.servlet.http.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;

    public AuthController(UserRepository users){ this.users=users; }

    @PostMapping("/register")
    public Object register(@RequestBody Map<String,String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String role = body.getOrDefault("role", "STUDENT");
        String email = body.getOrDefault("email", username+"@example.com");
        User existing = users.findByUsername(username);
        if (existing != null) return Map.of("status","error","message","user exists");
        users.save(new User(null, username, CryptoUtil.md5(password), role, email));
        return Map.of("status","ok");
    }

    @PostMapping("/login")
    public Object login(@RequestBody Map<String,String> body, HttpServletResponse resp) {
        String username = body.get("username");
        String password = body.get("password");
        boolean remember = Boolean.parseBoolean(body.getOrDefault("rememberMe","false"));
        String forceSessionId = body.get("forceSessionId");

        User u = users.findByUsername(username);
        if (u != null && u.getPasswordMd5().equalsIgnoreCase(CryptoUtil.md5(password))) {
            if (remember) {
                String token = username + ":" + password;
                String b64 = Base64.encodeBase64String(token.getBytes(StandardCharsets.UTF_8));
                Cookie c = new Cookie("rememberMe", b64);
                c.setPath("/");
                c.setHttpOnly(false);
                resp.addCookie(c);
            }
            if (forceSessionId != null && !forceSessionId.isBlank()) {
                Cookie js = new Cookie("JSESSIONID", forceSessionId);
                js.setPath("/");
                resp.addCookie(js);
            }
            return Map.of("status","ok","user", Map.of("id", u.getId(), "username", u.getUsername(), "role", u.getRole()));
        }
        return Map.of("status","error","message","invalid creds");
    }
}
