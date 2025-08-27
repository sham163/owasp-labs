package com.secureuniversity.controller;

import com.secureuniversity.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/advsearch")
public class AdvancedSearchController {
    private final JdbcTemplate jdbc;
    private static final Map<String, List<String>> searchCache = new HashMap<>();
    
    public AdvancedSearchController(JdbcTemplate jdbc) { 
        this.jdbc = jdbc;
    }
    
    // Advanced blind SQL injection with boolean-based detection
    @GetMapping("/query")
    public Object advancedSearch(@RequestParam String q, 
                                 @RequestParam(required = false, defaultValue = "courses") String table,
                                 @RequestParam(required = false, defaultValue = "name") String column) {
        
        // Boolean-based blind SQL injection
        String query = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " LIKE '%" + q + "%'";
        
        try {
            int count = jdbc.queryForObject(query, Integer.class);
            
            if (count > 0) {
                // Fetch actual results with another injectable query
                String dataQuery = "SELECT * FROM " + table + " WHERE " + column + " LIKE '%" + q + "%'";
                List<Map<String, Object>> results = jdbc.queryForList(dataQuery);
                return Map.of("found", true, "count", count, "results", results);
            } else {
                return Map.of("found", false, "count", 0);
            }
        } catch (Exception e) {
            // Information leak through error differentiation
            if (e.getMessage().contains("syntax")) {
                return Map.of("error", "Invalid search syntax", "debug", e.getMessage().substring(0, Math.min(100, e.getMessage().length())));
            }
            return Map.of("error", "Search failed");
        }
    }
    
    // Time-based blind SQL injection
    @GetMapping("/filter")
    public Object filterSearch(@RequestParam String term,
                              @RequestParam(required = false, defaultValue = "1=1") String filter) {
        
        long startTime = System.nanoTime();
        
        // Intentionally vulnerable to time-based injection
        String query = "SELECT * FROM courses WHERE name LIKE '%" + term + "%' AND (" + filter + ")";
        
        try {
            List<Map<String, Object>> results = jdbc.queryForList(query);
            
            long endTime = System.nanoTime();
            long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            
            // Leak timing information for blind injection
            return Map.of(
                "results", results,
                "queryTime", duration,
                "cached", false
            );
        } catch (Exception e) {
            long endTime = System.nanoTime();
            long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            
            return Map.of(
                "error", "Query failed",
                "queryTime", duration,
                "hint", e.getClass().getSimpleName()
            );
        }
    }
    
    // UNION-based SQL injection
    @GetMapping("/union")
    public Object unionSearch(@RequestParam String category,
                             @RequestParam(required = false, defaultValue = "id") String orderBy) {
        
        // Classic UNION injection vulnerability
        String query = "SELECT id, name, description FROM courses WHERE description LIKE '%" + category + 
                      "%' ORDER BY " + orderBy;
        
        try {
            return jdbc.queryForList(query);
        } catch (Exception e) {
            // Error-based injection opportunity
            return Map.of("error", "Query error", "message", e.getMessage());
        }
    }
    
    // Reflected XSS with JSON injection
    @GetMapping(value = "/suggest", produces = MediaType.TEXT_HTML_VALUE)
    public String searchSuggestions(@RequestParam String prefix) {
        // Build HTML with injected content
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h3>Suggestions for: " + prefix + "</h3>");
        html.append("<script>\n");
        html.append("var suggestions = [");
        
        // JSON injection leading to XSS
        try {
            List<Map<String, Object>> results = jdbc.queryForList(
                "SELECT name FROM courses WHERE name LIKE '" + prefix + "%' LIMIT 5"
            );
            
            for (Map<String, Object> row : results) {
                // No escaping in JSON context
                html.append("'" + row.get("name") + "',");
            }
        } catch (Exception e) {
            html.append("'" + e.getMessage() + "'");
        }
        
        html.append("];\n");
        html.append("suggestions.forEach(s => document.write('<div>' + s + '</div>'));\n");
        html.append("</script>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    // Stored XSS through search history
    @PostMapping("/history")
    public Object saveSearchHistory(@RequestBody Map<String, String> params, HttpServletRequest req) {
        User currentUser = (User) req.getAttribute("currentUser");
        String searchTerm = params.get("term");
        String description = params.getOrDefault("description", "");
        
        if (currentUser != null) {
            // Store search with XSS payload
            String historyEntry = "User " + currentUser.getUsername() + " searched for: " + searchTerm + 
                                " (" + description + ")";
            
            // SQL injection in INSERT
            String insertQuery = "INSERT INTO forum_posts (author_id, content) VALUES (" +
                               currentUser.getId() + ", '" + historyEntry + "')";
            jdbc.update(insertQuery);
            
            // Cache with potential XSS
            searchCache.computeIfAbsent(currentUser.getUsername(), k -> new ArrayList<>())
                      .add(searchTerm);
            
            return Map.of("saved", true, "term", searchTerm);
        }
        
        return Map.of("error", "Not authenticated");
    }
    
    // Get search history with XSS
    @GetMapping(value = "/history/{username}", produces = MediaType.TEXT_HTML_VALUE)
    public String getSearchHistory(@PathVariable String username) {
        List<String> history = searchCache.getOrDefault(username, List.of());
        
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>Search History for " + username + "</h2>");
        html.append("<ul>");
        
        for (String term : history) {
            // Direct injection of cached XSS payloads
            html.append("<li>" + term + "</li>");
        }
        
        html.append("</ul>");
        html.append("</body></html>");
        
        return html.toString();
    }
}