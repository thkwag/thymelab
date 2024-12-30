package com.github.thkwag.thymelab.processor.controller;

import com.github.thkwag.thymelab.processor.service.ThymeleafDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

@Slf4j
@Controller
public class PageController {

    private final ThymeleafDataService thymeleafDataService;

    public PageController(ThymeleafDataService thymeleafDataService) {
        this.thymeleafDataService = thymeleafDataService;
    }

    @GetMapping({"/", "/**/*.html"})
    public String handleRequest(Model model, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String path = requestUri.equals("/") ? "index" : requestUri.substring(1);

        if (path.endsWith(".html")) {
            path = path.substring(0, path.length() - 5);
        }

        String logMessage = AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW,
                String.format("<<Request>> [%s] %s", request.getMethod(), requestUri)
        );
        log.info(logMessage);

        thymeleafDataService.loadJsonData(path, model);
        return path.equals("index") ? "index" : path;
    }
} 