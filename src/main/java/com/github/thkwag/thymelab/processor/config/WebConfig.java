package com.github.thkwag.thymelab.processor.config;

import com.github.thkwag.thymelab.processor.interceptor.GlobalDataInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.resource.VersionResourceResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final GlobalDataInterceptor globalDataInterceptor;
    @Value("${watch.directory.static:classpath:/default/static/}")
    private String staticDir;

    private static final String[] STATIC_RESOURCE_PATTERNS = {
        "/**/*.ico", "/**/*.css", "/**/*.js", 
        "/**/*.png", "/**/*.jpg", "/**/*.jpeg", 
        "/**/*.gif", "/**/*.svg", "/**/*.woff",
        "/**/*.woff2", "/**/*.ttf", "/**/*.eot",
        "/**/*.otf", "/**/*.pdf", "/**/*.json",
        "/**/*.xml", "/**/*.csv", "/**/*.txt",
        "/**/*.mp4", "/**/*.webm", "/**/*.ogg",
        "/**/*.mp3", "/**/*.wav", "/**/*.webp",
        "/**/*.bmp", "/**/*.tiff"
    };

    public WebConfig(GlobalDataInterceptor globalDataInterceptor) {
        this.globalDataInterceptor = globalDataInterceptor;
    }

    @Bean
    public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
        return new ResourceUrlEncodingFilter();
    }
    
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String resolvedStaticDir = staticDir.startsWith("classpath:")
                ? staticDir
                : "file:" + staticDir + "/";

        registry.addResourceHandler(STATIC_RESOURCE_PATTERNS)
               .addResourceLocations(resolvedStaticDir)
               .setCacheControl(CacheControl.noCache())
               .setCachePeriod(0)
               .resourceChain(true)
               .addResolver(new VersionResourceResolver().addContentVersionStrategy(STATIC_RESOURCE_PATTERNS));

        registry.addResourceHandler("/thymeleaf/js/**")
               .addResourceLocations("classpath:/static/thymeleaf/js/")
               .setCachePeriod(0)
               .resourceChain(true)
               .addResolver(new VersionResourceResolver().addContentVersionStrategy("/thymeleaf/js/**"));
               
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(globalDataInterceptor)
                .excludePathPatterns(STATIC_RESOURCE_PATTERNS)
                .excludePathPatterns("/thymeleaf/js/**")
                .addPathPatterns("/**")
                .order(2);
    }

} 