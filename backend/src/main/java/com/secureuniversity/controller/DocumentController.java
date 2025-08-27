package com.secureuniversity.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.secureuniversity.model.User;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    
    private final JdbcTemplate jdbc;
    private static final String DOC_ROOT = "/tmp/documents/";
    private static final String TEMPLATE_DIR = "/tmp/templates/";
    
    public DocumentController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        try {
            Files.createDirectories(Paths.get(DOC_ROOT));
            Files.createDirectories(Paths.get(TEMPLATE_DIR));
            // Create some default templates
            Files.write(Paths.get(TEMPLATE_DIR, "report.html"), 
                       "<html><body><h1>{{TITLE}}</h1><div>{{CONTENT}}</div></body></html>".getBytes());
        } catch (IOException e) {
            // Silent fail
        }
    }
    
    // Advanced Path Traversal with multiple encoding bypasses
    @GetMapping("/download/{category}/{filename}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String category, 
                                                    @PathVariable String filename,
                                                    HttpServletRequest request) throws IOException {
        
        // Vulnerable to:
        // 1. Double URL encoding
        // 2. Unicode normalization (％２Ｅ％２Ｅ％２Ｆ)
        // 3. Overlong UTF-8 encoding
        // 4. Mixed case (..%2F)
        
        // Decode only once - vulnerable to double encoding
        String decodedCategory = URLDecoder.decode(category, StandardCharsets.UTF_8);
        String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8);
        
        // Weak check - only removes pattern, doesn't prevent it
        if (decodedFilename.contains("..") || decodedCategory.contains("..")) {
            decodedFilename = decodedFilename.replace("..", "");
            decodedCategory = decodedCategory.replace("..", "");
        }
        
        // Build path - vulnerable to traversal after cleaning
        Path filePath = Paths.get(DOC_ROOT, decodedCategory, decodedFilename);
        
        // Normalize AFTER building - allows traversal
        filePath = filePath.normalize();
        
        Resource resource = new UrlResource(filePath.toUri());
        
        // Information disclosure through detailed errors
        if (!resource.exists()) {
            throw new RuntimeException("File not found: " + filePath.toString());
        }
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + decodedFilename + "\"")
                .body(resource);
    }
    
    // Stored XSS through document metadata with filter bypass
    @PostMapping("/upload/metadata")
    public Object uploadWithMetadata(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                                    @RequestParam Map<String, String> metadata,
                                    HttpServletRequest req) throws IOException {
        
        User currentUser = (User) req.getAttribute("currentUser");
        String docId = UUID.randomUUID().toString();
        
        // Store file
        Path filePath = Paths.get(DOC_ROOT, docId + "_" + file.getOriginalFilename());
        Files.write(filePath, file.getBytes());
        
        // Process metadata - multiple XSS vectors
        String title = metadata.getOrDefault("title", "Untitled");
        String description = metadata.getOrDefault("description", "");
        String tags = metadata.getOrDefault("tags", "");
        
        // Weak XSS filter - many bypasses possible
        // Only removes script tags but not other vectors
        title = title.replaceAll("(?i)<script.*?>.*?</script>", "");
        description = description.replaceAll("(?i)<script.*?>.*?</script>", "");
        
        // Store in database - creates stored XSS
        String insertQuery = "INSERT INTO forum_posts (author_id, content) VALUES (" +
                           currentUser.getId() + ", " +
                           "'Document: " + title + " - " + description + " [Tags: " + tags + "]')";
        jdbc.update(insertQuery);
        
        // Return HTML response - reflected XSS opportunity
        String response = "<div class='doc-info' data-tags='" + tags + "'>" +
                         "<h3>" + title + "</h3>" +
                         "<p>" + description + "</p></div>";
        
        return Map.of("status", "uploaded", "docId", docId, "preview", response);
    }
    
    // Template injection leading to XSS
    @PostMapping("/generate")
    public Object generateDocument(@RequestBody Map<String, String> params) throws IOException {
        String template = params.getOrDefault("template", "report");
        String title = params.getOrDefault("title", "");
        String content = params.getOrDefault("content", "");
        
        // Path traversal in template selection
        Path templatePath = Paths.get(TEMPLATE_DIR, template + ".html");
        
        if (!Files.exists(templatePath)) {
            // Allow custom template upload - dangerous
            String customTemplate = params.get("customTemplate");
            if (customTemplate != null) {
                Files.write(Paths.get(TEMPLATE_DIR, "custom_" + System.currentTimeMillis() + ".html"), 
                          customTemplate.getBytes());
                templatePath = Paths.get(TEMPLATE_DIR, "custom_" + System.currentTimeMillis() + ".html");
            }
        }
        
        String templateContent = Files.readString(templatePath);
        
        // Template injection - no escaping
        templateContent = templateContent.replace("{{TITLE}}", title);
        templateContent = templateContent.replace("{{CONTENT}}", content);
        
        // Additional injection through JavaScript execution context
        if (params.containsKey("script")) {
            templateContent = templateContent.replace("</body>", 
                "<script>" + params.get("script") + "</script></body>");
        }
        
        return Map.of("html", templateContent, "rendered", true);
    }
    
    // XML External Entity (XXE) injection through document parsing
    @PostMapping("/parse/xml")
    public Object parseXmlDocument(@RequestBody String xmlContent) {
        try {
            // XXE vulnerability - external entities enabled
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Explicitly enable dangerous features (hidden vulnerability)
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", true);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
            
            // Extract and return content - can leak files through XXE
            NodeList nodes = doc.getElementsByTagName("*");
            Map<String, String> result = new HashMap<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                result.put(nodes.item(i).getNodeName(), nodes.item(i).getTextContent());
            }
            
            return result;
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
    
    // Zip slip vulnerability in archive extraction
    @PostMapping("/extract")
    public Object extractArchive(@RequestParam("archive") org.springframework.web.multipart.MultipartFile file) throws IOException {
        List<String> extracted = new ArrayList<>();
        
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // Zip Slip vulnerability - no validation of entry paths
                Path targetPath = Paths.get(DOC_ROOT, entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    extracted.add(entry.getName());
                }
            }
        }
        
        return Map.of("extracted", extracted, "location", DOC_ROOT);
    }
    
    // Advanced reflected XSS with multiple contexts
    @GetMapping(value = "/preview", produces = MediaType.TEXT_HTML_VALUE)
    public String previewDocument(@RequestParam String url, 
                                 @RequestParam(required = false) String callback) {
        
        // Multiple XSS contexts in single response
        StringBuilder html = new StringBuilder();
        html.append("<html><head>");
        
        // XSS in JavaScript context
        if (callback != null) {
            html.append("<script>window." + callback + " = function(data) { console.log(data); };</script>");
        }
        
        html.append("</head><body>");
        html.append("<iframe src='" + url + "' width='100%' height='400'></iframe>");
        
        // XSS in HTML attribute context
        html.append("<div data-source='" + url + "' onclick='loadDoc(\"" + url + "\")'>");
        html.append("Click to load: " + url);
        html.append("</div>");
        
        // XSS in CSS context
        html.append("<style>.preview { background: url('" + url + "'); }</style>");
        
        html.append("</body></html>");
        
        return html.toString();
    }
}