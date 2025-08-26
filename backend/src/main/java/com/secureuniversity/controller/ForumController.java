package com.secureuniversity.controller;

import com.secureuniversity.model.User;
import com.secureuniversity.repo.ForumRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/forum")
public class ForumController {
    private final ForumRepository forum;
    public ForumController(ForumRepository forum){ this.forum=forum; }

    @PostMapping("/post")
    public Object post(@RequestBody Map<String,String> body, HttpServletRequest req) {
        User u = (User) req.getAttribute("currentUser");
        long authorId = (u != null) ? u.getId() : 1L;
        String content = body.getOrDefault("content","<i>empty</i>");
        forum.addPost(authorId, content);
        return Map.of("status","posted");
    }

    @GetMapping("/list")
    public Object list() { return forum.list(); }
}
