package com.swiftelan.rafter.servlet.boundary;

import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DefaultServletRequestFilterTest {

	private DefaultServletRequestFilter filter;
	private FilterConfig config;

	@Before
	public void initialize() {
		filter = new DefaultServletRequestFilter();
		config = Mockito.mock(FilterConfig.class);
		ServletContext context = Mockito.mock(ServletContext.class);
		Mockito.when(config.getServletContext()).thenReturn(context);
	}

	@Test
	public void defaultName() throws ServletException {
		RequestDispatcher dispatcher = Mockito.mock(RequestDispatcher.class);
		Mockito.when(config.getServletContext().getNamedDispatcher(DefaultServletRequestFilter.DEFAULT_SERVLET_NAME)).thenReturn(dispatcher);
		filter.init(config);
	}
	
	@Test
	public void servletNameFromInitParam() throws ServletException {
		RequestDispatcher dispatcher = Mockito.mock(RequestDispatcher.class);
		String servletName = "foo";
		Mockito.when(config.getInitParameter(DefaultServletRequestFilter.SERVLET_PARAMETER_NAME)).thenReturn(servletName);
		Mockito.when(config.getServletContext().getNamedDispatcher(servletName)).thenReturn(dispatcher);
		filter.init(config);
	}

	@Test(expected = ServletException.class)
	public void noServletWithName() throws ServletException {
		filter.init(config);
	}
}
