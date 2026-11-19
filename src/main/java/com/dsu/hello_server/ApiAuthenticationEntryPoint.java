package com.dsu.hello_server;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// A 401 from a security filter happens before any controller, so @RestControllerAdvice never sees it.
// This hands the exception to the resolver that runs ApiExceptionHandler: same ApiError body.
@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public ApiAuthenticationEntryPoint(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException e) {
        resolver.resolveException(request, response, null, e);
    }
}
