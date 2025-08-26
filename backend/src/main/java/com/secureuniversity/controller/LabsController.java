package com.secureuniversity.controller;

import com.secureuniversity.repo.*;
import com.secureuniversity.model.*;
import com.secureuniversity.util.CryptoUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;

@RestController
@RequestMapping("/api/labs")
public class LabsController {

    private final JdbcTemplate jdbc;
    private final UserRepository users;
    private final CourseRepository courses;
    private final ForumRepository forum;

    public LabsController(JdbcTemplate jdbc, UserRepository users, CourseRepository courses, ForumRepository forum) {
        this.jdbc = jdbc; this.users = users; this.courses = courses; this.forum = forum;
    }

    // SQLi
    @GetMapping("/sqli/easy")
    public Object sqliEasy(@RequestParam String q) {
        String sql = "select * from courses where name like '%" + q + "%' or description like '%" + q + "%'";
        return jdbc.query(sql, (rs,i)-> Map.of("id", rs.getLong("id"), "name", rs.getString("name")));
    }
    @GetMapping("/sqli/medium")
    public Object sqliMedium(@RequestParam String q) {
        String qq = q.replace("'", "''");
        String sql = "select * from courses where name like '%" + qq + "%'";
        return jdbc.query(sql, (rs,i)-> Map.of("id", rs.getLong("id"), "name", rs.getString("name")));
    }
    @GetMapping("/sqli/hard")
    public Object sqliHard(@RequestParam String q, @RequestParam(defaultValue="id") String sort) {
        String base = "select id,name,description from courses where description like ? order by " + sort;
        return jdbc.query(base, new Object[]{"%"+q+"%"}, (rs,i)-> Map.of("id", rs.getLong("id"), "name", rs.getString("name")));
    }

    // Reflected XSS
    @GetMapping(value="/xss/reflected/easy", produces = MediaType.TEXT_HTML_VALUE)
    public String xssReflectedEasy(@RequestParam String q) {
        return "<h3>Results for: " + q + "</h3><div>No results</div>";
    }
    @GetMapping(value="/xss/reflected/medium", produces = MediaType.TEXT_HTML_VALUE)
    public String xssReflectedMedium(@RequestParam String q) {
        String sanitized = q.replaceAll("(?i)<\\s*script", "&lt;script");
        return "<div>Query: " + sanitized + "</div>";
    }
    @GetMapping(value="/xss/reflected/hard", produces = MediaType.TEXT_HTML_VALUE)
    public String xssReflectedHard(@RequestParam String q) {
        String sanitized = q.replace("<","&lt;").replace(">","&gt;");
        return "<div data-q=\"" + sanitized + "\">" + sanitized + "</div>";
    }

    // Stored XSS
    @PostMapping("/xss/stored/{level}/post")
    public Object xssStoredPost(@PathVariable String level, @RequestBody Map<String,String> body, HttpServletRequest req) {
        String content = body.getOrDefault("content","<i>empty</i>");
        long author = Optional.ofNullable((User) req.getAttribute("currentUser")).map(User::getId).orElse(1L);

        String toStore = content;
        if ("medium".equalsIgnoreCase(level)) {
            toStore = content.replaceAll("(?i)<\\s*script[^>]*>", "").replaceAll("(?i)</\\s*script\\s*>", "");
        } else if ("hard".equalsIgnoreCase(level)) {
            toStore = content.replaceAll("(?i)<\\s*script", "&lt;script").replaceAll("(?i)on\\w+\\s*=", "onx=");
        }
        forum.addPost(author, toStore);
        return Map.of("status","posted", "storedLevel", level);
    }
    @GetMapping("/xss/stored/{level}/list")
    public Object xssStoredList(@PathVariable String level) {
        return forum.list();
    }

    // A01 Broken Access Control
    @GetMapping("/bac/easy/admin-users")
    public Object bacEasy() { return users.findAll(); }
    @GetMapping("/bac/medium/admin-users")
    public Object bacMedium(@RequestHeader(value="X-Admin", required=false) String admin) {
        if ("true".equalsIgnoreCase(admin)) return users.findAll();
        return Map.of("error","admin header required");
    }
    @GetMapping("/bac/hard/admin-users")
    public Object bacHard(@RequestParam(defaultValue="STUDENT") String role) {
        if ("ADMIN".equalsIgnoreCase(role)) return users.findAll();
        return Map.of("error","not admin");
    }

    // A02 Crypto failures
    @PostMapping("/crypto/{level}/register")
    public Object cryptoRegister(@PathVariable String level, @RequestBody Map<String,String> body) {
        String u = body.get("username");
        String p = body.get("password");
        String email = body.getOrDefault("email", u+"@example.com");
        String hash = switch (level.toLowerCase()) {
            case "easy" -> CryptoUtil.md5(p);
            case "medium" -> CryptoUtil.sha1(p);
            default -> "$2a$04$" + Base64.getEncoder().encodeToString("static-salt".getBytes()) + CryptoUtil.md5(p);
        };
        users.save(new User(null, u, hash, "STUDENT", email));
        return Map.of("status","ok","storedHash",hash);
    }

    // A04 Insecure Design
    static final Map<String,String> resetCodes = new HashMap<>();
    @PostMapping("/design/easy/request-reset")
    public Object designEasy(@RequestBody Map<String,String> body) {
        resetCodes.put(body.get("username"), "000000");
        return Map.of("status","sent");
    }
    @PostMapping("/design/medium/request-reset")
    public Object designMedium(@RequestBody Map<String,String> body) {
        String code = String.valueOf((int)(Math.random()*1000000));
        resetCodes.put(body.get("username"), code);
        return Map.of("status","sent","hint","code re-usable & no expiry");
    }
    @PostMapping("/design/hard/request-reset")
    public Object designHard(@RequestBody Map<String,String> body) {
        String code = java.util.UUID.randomUUID().toString().substring(0,8);
        resetCodes.put(body.get("username"), code);
        return Map.of("status","sent","debugCode",code);
    }

    // A05 Misconfiguration
    @GetMapping("/misconfig/{level}/boom")
    public Object misconfig(@PathVariable String level) {
        if ("easy".equalsIgnoreCase(level)) throw new RuntimeException("Full stacktrace and secrets will leak!");
        if ("medium".equalsIgnoreCase(level)) throw new IllegalArgumentException("Detailed message leaked");
        throw new RuntimeException("Generic error but stacktrace still enabled by config");
    }

    // A06 Outdated components
    @PostMapping("/components/{level}/substitute")
    public Object components(@PathVariable String level, @RequestBody Map<String,String> body) {
        String template = body.getOrDefault("template", "Hello ${name}");
        Map<String,String> vars = new HashMap<>(body);
        String out = StringSubstitutor.replace(template, vars);
        if ("hard".equalsIgnoreCase(level)) {
            out = StringSubstitutor.replaceSystemProperties(out);
        }
        return Map.of("result", out, "componentVersion","commons-text-1.9");
    }

    // A07 Identification & Auth
    @PostMapping("/auth/{level}/rememberme")
    public Object authRemember(@PathVariable String level, @RequestBody Map<String,String> body) {
        String token;
        if ("easy".equalsIgnoreCase(level)) {
            token = Base64.getEncoder().encodeToString((body.get("u")+":"+body.get("p")).getBytes());
        } else if ("medium".equalsIgnoreCase(level)) {
            token = body.get("u")+ ":" + CryptoUtil.md5(body.get("p"));
        } else {
            token = "rm:"+body.get("u")+":ts="+System.currentTimeMillis()+":sig=STATIC";
        }
        return Map.of("rememberMe", token);
    }

    // A08 Insecure deserialization
    @PostMapping("/integrity/easy/deserialize")
    public Object deserEasy(@RequestBody Map<String,String> body) throws Exception {
        byte[] data = Base64.getDecoder().decode(body.get("blob"));
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            Object o = ois.readObject();
            return Map.of("class", o.getClass().getName(), "toString", String.valueOf(o));
        }
    }
    @PostMapping("/integrity/medium/deserialize")
    public Object deserMedium(@RequestBody Map<String,String> body) throws Exception {
        byte[] data = Base64.getDecoder().decode(body.get("blob"));
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            Object o = ois.readObject();
            if (o instanceof Map || o instanceof java.util.List) return Map.of("ok","map/list", "toString", String.valueOf(o));
            return Map.of("blocked","class not allowed", "class", o.getClass().getName());
        }
    }
    @PostMapping("/integrity/hard/deserialize")
    public Object deserHard(@RequestBody Map<String,String> body) throws Exception {
        String blob = body.get("blob");
        String sig = body.getOrDefault("sig","STATIC");
        if (!"STATIC".equals(sig)) return Map.of("error","bad signature");
        byte[] data = Base64.getDecoder().decode(blob);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            Object o = ois.readObject();
            return Map.of("ok","signed", "class", o.getClass().getName());
        }
    }

    // A09 Logging & Monitoring
    static final java.util.List<String> audit = new java.util.ArrayList<>();
    @PostMapping("/logging/{level}/login")
    public Object logging(@PathVariable String level, @RequestBody Map<String,String> body) {
        String u = body.get("username"); String p = body.get("password");
        User user = users.findByUsername(u);
        boolean ok = user != null && (user.getPasswordMd5().equalsIgnoreCase(CryptoUtil.md5(p)) || user.getPasswordMd5().equalsIgnoreCase(p));
        if ("easy".equalsIgnoreCase(level)) {
            // no logging
        } else if ("medium".equalsIgnoreCase(level)) {
            audit.add("login for "+u+" ok="+ok);
        } else {
            audit.add("login attempt user="+u+" ok="+ok+" pwHash="+CryptoUtil.md5(p));
        }
        return Map.of("ok", ok);
    }
    @GetMapping("/logging/audit")
    public Object getAudit(){ return audit; }

    // A10 SSRF
    @GetMapping("/ssrf/easy")
    public Object ssrfEasy(@RequestParam String url) {
        return new RestTemplate().getForObject(url, String.class);
    }
    @GetMapping("/ssrf/medium")
    public Object ssrfMedium(@RequestParam String url) {
        if (url.contains("localhost") || url.contains("127.0.0.1")) return Map.of("blocked","localhost");
        return new RestTemplate().getForObject(url, String.class);
    }
    @GetMapping("/ssrf/hard")
    public Object ssrfHard(@RequestParam String url) {
        if (!(url.startsWith("http://") || url.startsWith("https://"))) return Map.of("error","scheme");
        return new RestTemplate().getForObject(url, String.class);
    }

    // Command Injection
    @GetMapping("/cmd/{level}/ping")
    public Object cmd(@PathVariable String level, @RequestParam String target) throws Exception {
        String cmd = "ping -c 1 " + target;
        if ("medium".equalsIgnoreCase(level)) cmd = "sh -c \"ping -c 1 " + target + "\"";
        if ("hard".equalsIgnoreCase(level)) cmd = "sh -c \"ping -c 1 " + target.replace("`","") + "\"";
        Process p = new ProcessBuilder("sh","-c", cmd).start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return Map.of("output", out);
    }
}
