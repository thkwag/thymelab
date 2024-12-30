package com.github.thkwag.thymelab.processor.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusObj = request.getAttribute("jakarta.servlet.error.status_code");
        int status = statusObj != null ? (int) statusObj : 500;
        
        model.addAttribute("status", status);
        model.addAttribute("error", HttpStatus.valueOf(status).getReasonPhrase());
        
        // Get error message and exception
        String message = (String) request.getAttribute("jakarta.servlet.error.message");
        Throwable throwable = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
        
        if (throwable != null) {
            List<String> errorMessages = new ArrayList<>();
            Set<String> uniqueMessages = new HashSet<>();
            Throwable current = throwable;
            
            while (current != null) {
                if (current.getMessage() != null) {
                    String msg = current.getClass().getSimpleName() + ": " + current.getMessage();
                    if (uniqueMessages.add(msg)) {  // Set.add() returns true if element was added
                        errorMessages.add(msg);
                    }
                }
                current = current.getCause();
            }
            
            if (!errorMessages.isEmpty()) {
                model.addAttribute("errorMessages", errorMessages);
                message = errorMessages.get(0);
            }
        }
        
        model.addAttribute("message", message != null ? message : "No message available");
        model.addAttribute("timestamp", System.currentTimeMillis());

        // Add detailed stack trace
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            model.addAttribute("trace", sw.toString());
        }

        return "error";
    }
} 