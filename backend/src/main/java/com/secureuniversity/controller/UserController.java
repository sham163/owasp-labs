package com.secureuniversity.controller;

import com.secureuniversity.model.User;
import com.secureuniversity.repo.UserRepository;
import com.secureuniversity.util.CryptoUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository users;

    public UserController(UserRepository users){ this.users=users; }

    @GetMapping("/me")
    public Object me(HttpServletRequest req) {
        User current = (User) req.getAttribute("currentUser");
        return current == null ? Map.of("auth","anonymous") : current;
    }

    @PutMapping("/{id}")
    public Object updateProfile(@PathVariable long id, @RequestBody Map<String,String> body) {
        User u = new User();
        u.setId(id);
        u.setEmail(body.getOrDefault("email","user@example.com"));
        u.setRole(body.getOrDefault("role","STUDENT"));
        String newPass = body.getOrDefault("password", "password");
        u.setPasswordMd5(CryptoUtil.md5(newPass));
        users.updateProfile(u);
        return Map.of("status","updated","id",id);
    }

    @GetMapping("/admin/list")
    public Object listAllUsers() {
        return users.findAll();
    }
}
