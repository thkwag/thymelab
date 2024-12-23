package com.github.thkwag.thymelab.processor.hotreload;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Component
@Order(1)
public class HotReloadFilter implements Filter {

    private static final String HOT_RELOAD_SCRIPT = "<script src=\"/thymeleaf/js/hot-reload.js\"></script></body>";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        String acceptHeader = httpRequest.getHeader("Accept");
        if (acceptHeader == null || !acceptHeader.contains("text/html")) {
            chain.doFilter(request, response);
            return;
        }

        ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
        chain.doFilter(request, responseWrapper);

        String content = responseWrapper.getCapturedContent();
        if (content != null && !content.isEmpty()) {
            content = content.replace("</body>", HOT_RELOAD_SCRIPT);
            byte[] modifiedContent = content.getBytes(StandardCharsets.UTF_8);
            response.setContentLength(modifiedContent.length);
            response.getOutputStream().write(modifiedContent);
        }
    }
}

class ResponseWrapper extends HttpServletResponseWrapper {
    private final CharArrayWriter charArrayWriter = new CharArrayWriter();
    private final PrintWriter writer = new PrintWriter(charArrayWriter);
    private ServletOutputStream outputStream;

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new ServletOutputStreamWrapper(charArrayWriter);
        }
        return outputStream;
    }

    public String getCapturedContent() {
        writer.flush();
        return charArrayWriter.toString();
    }
}

class ServletOutputStreamWrapper extends ServletOutputStream {
    private final CharArrayWriter charArrayWriter;

    ServletOutputStreamWrapper(CharArrayWriter charArrayWriter) {
        this.charArrayWriter = charArrayWriter;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        // Not used
    }

    @Override
    public void write(int b) throws IOException {
        charArrayWriter.write(b);
    }
} 