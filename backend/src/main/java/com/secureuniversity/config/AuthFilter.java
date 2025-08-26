package com.secureuniversity.config;

import com.secureuniversity.repo.UserRepository;
import com.secureuniversity.model.User;
import com.secureuniversity.util.CryptoUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class AuthFilter implements Filter {

    private final UserRepository userRepo;

    public AuthFilter(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        String username = request.getHeader("X-User");

        if (username == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                Arrays.stream(cookies)
                        .filter(c -> "rememberMe".equalsIgnoreCase(c.getName()))
                        .findFirst()
                        .ifPresent(c -> {
                            try {
                                String decoded = new String(Base64.decodeBase64(c.getValue()), StandardCharsets.UTF_8);
                                String[] parts = decoded.split(":", 2);
                                if (parts.length == 2) {
                                    String u = parts[0];
                                    String p = parts[1];
                                    User user = userRepo.findByUsername(u);
                                    if (user != null && user.getPasswordMd5().equalsIgnoreCase(CryptoUtil.md5(p))) {
                                        request.setAttribute("currentUser", user);
                                    }
                                }
                            } catch (Exception ignored) {}
                        });
            }
        }

        if (username != null && request.getAttribute("currentUser") == null) {
            User u = userRepo.findByUsername(username);
            if (u != null) request.setAttribute("currentUser", u);
        }

        chain.doFilter(req, res);
    }
}
