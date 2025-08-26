package com.secureuniversity.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

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
}
