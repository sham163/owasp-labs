package com.secureuniversity.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class SearchController {
    @GetMapping(value="/search", produces = MediaType.TEXT_HTML_VALUE)
    public String reflected(@RequestParam("q") String q) {
        return "<html><body><h3>Search results for: " + q + "</h3><div>No results.</div></body></html>";
    }
}
