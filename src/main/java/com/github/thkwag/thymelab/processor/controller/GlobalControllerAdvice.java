package com.github.thkwag.thymelab.processor.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice {
    
    @ModelAttribute("req")
    public Map<String, Object> getRequestInfo(HttpServletRequest request) {
        Map<String, Object> requestInfo = new HashMap<>();

        requestInfo.put("requestURI", request.getRequestURI());
        requestInfo.put("requestURL", request.getRequestURL().toString());
        requestInfo.put("method", request.getMethod());
        requestInfo.put("protocol", request.getProtocol());
        requestInfo.put("scheme", request.getScheme());
        requestInfo.put("serverName", request.getServerName());
        requestInfo.put("serverPort", request.getServerPort());
        requestInfo.put("contextPath", request.getContextPath());
        requestInfo.put("servletPath", request.getServletPath());
        requestInfo.put("pathInfo", request.getPathInfo());
        requestInfo.put("queryString", request.getQueryString());
        requestInfo.put("remoteAddr", request.getRemoteAddr());
        requestInfo.put("remoteHost", request.getRemoteHost());
        requestInfo.put("remotePort", request.getRemotePort());
        requestInfo.put("localAddr", request.getLocalAddr());
        requestInfo.put("localName", request.getLocalName());
        requestInfo.put("localPort", request.getLocalPort());
        
        return requestInfo;
    }
} 