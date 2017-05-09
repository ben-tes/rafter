package com.swiftelan.rafter.servlet.boundary;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class DefaultServletRequestFilter implements Filter {
    static final String DEFAULT_SERVLET_NAME = "default";
	static final String SERVLET_PARAMETER_NAME = "DefaultServletFilterName";
	private RequestDispatcher dispatcher;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    	String servletName = Optional.ofNullable(filterConfig.getInitParameter(SERVLET_PARAMETER_NAME)).orElse(DEFAULT_SERVLET_NAME);
        dispatcher = filterConfig.getServletContext().getNamedDispatcher(servletName);
        if (dispatcher == null) {
        	throw new ServletException("No servlet configured with name: " + servletName);
        }
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
