package com.secureuniversity.controller;

import com.secureuniversity.model.User;
import com.secureuniversity.repo.CourseRepository;
import com.secureuniversity.repo.GradeRepository;
import com.secureuniversity.repo.UserRepository;
import com.secureuniversity.util.CryptoUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository users;
    private final CourseRepository courses;
    private final GradeRepository grades;

    public AdminController(UserRepository users, CourseRepository courses, GradeRepository grades) {
        this.users=users; this.courses=courses; this.grades=grades;
    }

    // Demo endpoints (cmd + ssrf-lite)
    @GetMapping("/ping")
    public Object ping(@RequestParam String target) throws Exception {
        Process p = new ProcessBuilder("sh", "-c", "ping -c 1 " + target).start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder sb = new StringBuilder(); String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        return Map.of("output", sb.toString());
    }

    @GetMapping("/fetch")
    public Object fetch(@RequestParam String url) {
        RestTemplate rt = new RestTemplate();
        String body = rt.getForObject(url, String.class);
        return Map.of("url", url, "body", body);
    }

    // Real admin actions
    @PostMapping("/users/create")
    public Object createUser(@RequestBody Map<String,String> b) {
        String u = b.get("username");
        String p = b.getOrDefault("password","password");
        String email = b.getOrDefault("email", u+"@example.com");
        users.save(new User(null, u, CryptoUtil.md5(p), "STUDENT", email));
        return Map.of("status","ok");
    }

    @PostMapping("/courses/create")
    public Object createCourse(@RequestBody Map<String,String> b) {
        String name = b.getOrDefault("name","Untitled");
        String desc = b.getOrDefault("description","");
        long id = courses.create(name, desc);
        return Map.of("status","ok", "id", id, "name", name);
    }

    @PostMapping("/grades/set")
    public Object setGrade(@RequestBody Map<String,String> b) {
        String username = b.get("username");
        long studentId = users.findByUsername(username).getId();
        long courseId = Long.parseLong(b.get("courseId"));
        String grade = b.getOrDefault("grade","A");
        grades.upsert(studentId, courseId, grade);
        return Map.of("status","ok");
    }
}
