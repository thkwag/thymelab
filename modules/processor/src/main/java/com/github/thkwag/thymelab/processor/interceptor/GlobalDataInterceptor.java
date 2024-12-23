package com.github.thkwag.thymelab.processor.interceptor;

import com.github.thkwag.thymelab.processor.service.ThymeleafDataService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Slf4j
@Component
public class GlobalDataInterceptor implements HandlerInterceptor {

    private final ThymeleafDataService thymeleafDataService;

    public GlobalDataInterceptor(ThymeleafDataService thymeleafDataService) {
        this.thymeleafDataService = thymeleafDataService;
    }

    @Override
    public void postHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable ModelAndView modelAndView) {

        if (modelAndView != null) {
            thymeleafDataService.loadJsonData("global", modelAndView.getModel());
            log.debug("Added global data to model for URI: {}", request.getRequestURI());
        }
    }
} 