package com.swiftelan.rafter.servlet.boundary;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class DefaultServletRequestFilter implements Filter {
    private RequestDispatcher dispatcher;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Optional<? extends ServletRegistration> servlet = filterConfig.getServletContext().getServletRegistrations()
                .entrySet().stream().filter(entry -> entry.getValue().getMappings().contains("/"))
                .map(e -> e.getValue()).findFirst();
        dispatcher = filterConfig.getServletContext().getNamedDispatcher(servlet.get().getName());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        dispatcher.forward(request, response);
    }

    @Override
    public void destroy() {
        dispatcher = null;
    }
}
