package com.dsu.hello_server;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// The 403 twin of ApiAuthenticationEntryPoint: a URL rule (rule D) refuses in a filter,
// before any controller, so the exception is handed to ApiExceptionHandler here.
@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final HandlerExceptionResolver resolver;

    public ApiAccessDeniedHandler(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException e) {
        resolver.resolveException(request, response, null, e);
    }
}
