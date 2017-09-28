package com.swiftelan.rafter.servlet.boundary;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRegistration;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
public class JspViewMessageBodyWriter implements MessageBodyWriter<JspViewContext> {

    private static final String DEFAULT_PREFIX = "/WEB-INF/views/";

    @Context
    Configuration configuration;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return JspViewContext.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(JspViewContext t, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(JspViewContext t, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException {
        Object prefixProperty = configuration.getProperty("JspViewMessageBodyWriter.viewPrefix");
        String viewPrefix = (String) Optional.ofNullable(prefixProperty).orElse(DEFAULT_PREFIX);
        try {

            HttpServletRequest request = new JspRequestWrapper(t.getRequest(), viewPrefix.concat(t.getView()));
            final PrintWriter writer = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(entityStream, StandardCharsets.UTF_8)));
            HttpServletResponse response = new JspResponseWrapper(t.getResponse(), writer, entityStream);
            t.getModel().forEach((key, value) -> t.getRequest().setAttribute(key, value));
            getJspDispatcher(t.getRequest().getServletContext()).forward(request, response);
            writer.flush();
        } catch (ServletException e) {
            throw new InternalServerErrorException(e);
        } catch (NoSuchElementException e) {
            throw new InternalServerErrorException("No servlet registered for JSP processing.", e);
        }
    }

    RequestDispatcher getJspDispatcher(ServletContext context) {
        Map<String, ? extends ServletRegistration> registrations = context.getServletRegistrations();
        Optional<? extends ServletRegistration> first = registrations.values().stream()
                .filter(r -> r.getMappings().contains("*.jsp")).findFirst();
        return context.getNamedDispatcher(first.get().getName());
    }

    class JspResponseWrapper extends HttpServletResponseWrapper {
        private final PrintWriter writer;
        private final OutputStream stream;

        public JspResponseWrapper(HttpServletResponse response, PrintWriter writer, OutputStream stream) {
            super(response);
            this.writer = writer;
            this.stream = stream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return writer;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return new ServletOutputStream() {

                @Override
                public void write(int b) throws IOException {
                    stream.write(b);
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {

                }
            };
        }
    }

    class JspRequestWrapper extends HttpServletRequestWrapper {
        private final String view;

        public JspRequestWrapper(HttpServletRequest request, String view) {
            super(request);
            this.view = view;
        }

        @Override
        public String getRequestURI() {
            return view;
        }

        @Override
        public String getServletPath() {
            return view;
        }

        @Override
        public String getPathInfo() {
            return null;
        }

        @Override
        public StringBuffer getRequestURL() {
            return new StringBuffer(view);
        }
    }
}
