package com.swiftelan.rafter.servlet.boundary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class JspViewResponseBodyWriterTest {

    private JspViewMessageBodyWriter writer;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private UriInfo uriInfo;
    private ResourceInfo resourceInfo;
    private Configuration configuration;
    private ServletContext servletContext;
    private RequestDispatcher dispatcher;

    @Before
    public void before() {
        writer = new JspViewMessageBodyWriter();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        uriInfo = Mockito.mock(UriInfo.class);
        resourceInfo = Mockito.mock(ResourceInfo.class);
        configuration = Mockito.mock(Configuration.class);
        writer.configuration = configuration;
        servletContext = Mockito.mock(ServletContext.class);

        Mockito.when(request.getServletContext()).thenReturn(servletContext);

        Map<String, ServletRegistration> registrations = new HashMap<>();
        ServletRegistration registration = Mockito.mock(ServletRegistration.class);
        Mockito.when(registration.getName()).thenReturn("jsp");
        Mockito.when(registration.getMappings()).thenReturn(Collections.singleton("*.jsp"));
        registrations.put("jsp", registration);
        Mockito.when(servletContext.getServletRegistrations()).then(i -> registrations);
        dispatcher = Mockito.mock(RequestDispatcher.class);
        Mockito.when(servletContext.getNamedDispatcher(registration.getName())).thenReturn(dispatcher);

        Mockito.when(uriInfo.getRequestUriBuilder()).then(i -> UriBuilder.fromPath(""));
    }

    @Test
    public void isWriteable() {
        Assert.assertTrue(writer.isWriteable(JspViewContext.class, null, null, null));
        Assert.assertFalse(writer.isWriteable(JsonObject.class, null, null, null));
    }

    @Test
    public void getSize() {
        Assert.assertEquals(-1, writer.getSize(null, null, null, null, null));
    }

    @Test(expected = InternalServerErrorException.class)
    public void noServlets() throws WebApplicationException, IOException {
        Mockito.when(servletContext.getServletRegistrations()).then(i -> Collections.emptyMap());
        JspViewContext context = new JspViewContext("test.jsp", request, response, uriInfo, resourceInfo, configuration,
                Collections.emptyMap());
        OutputStream entityStream = new ByteArrayOutputStream();
        MultivaluedMap<String, Object> httpHeaders = new MultivaluedHashMap<>();
        writer.writeTo(context, null, null, null, null, httpHeaders, entityStream);
    }

    @Test(expected = InternalServerErrorException.class)
    public void noJspServlet() throws WebApplicationException, IOException {
        ServletRegistration registration = Mockito.mock(ServletRegistration.class);
        Mockito.when(registration.getName()).thenReturn("html");
        Mockito.when(registration.getMappings()).thenReturn(Collections.singleton("*.html"));
        Mockito.when(servletContext.getServletRegistrations())
                .then(i -> Collections.singletonMap("html", registration));
        JspViewContext context = new JspViewContext("test.jsp", request, response, uriInfo, resourceInfo, configuration,
                Collections.emptyMap());
        OutputStream entityStream = new ByteArrayOutputStream();
        MultivaluedMap<String, Object> httpHeaders = new MultivaluedHashMap<>();
        writer.writeTo(context, null, null, null, null, httpHeaders, entityStream);
    }

    @Test
    public void noModels() throws WebApplicationException, IOException {
        JspViewContext context = new JspViewContext("test.jsp", request, response, uriInfo, resourceInfo, configuration,
                Collections.emptyMap());
        OutputStream entityStream = new ByteArrayOutputStream();
        MultivaluedMap<String, Object> httpHeaders = new MultivaluedHashMap<>();
        writer.writeTo(context, null, null, null, null, httpHeaders, entityStream);
    }

    @Test
    public void models() throws WebApplicationException, IOException {
        Map<String, Object> models = new HashMap<>();
        models.put("one", "1");
        models.put("two", Integer.valueOf(2));
        JspViewContext context = new JspViewContext("test.jsp", request, response, uriInfo, resourceInfo, configuration,
                models);
        OutputStream entityStream = new ByteArrayOutputStream();
        MultivaluedMap<String, Object> httpHeaders = new MultivaluedHashMap<>();

        final Map<String, Object> attributes = new HashMap<>();
        Mockito.doAnswer(i -> attributes.put(i.getArgument(0), i.getArgument(1)))
                .when(request).setAttribute(Mockito.anyString(), Mockito.any());
        writer.writeTo(context, null, null, null, null, httpHeaders, entityStream);
        Assert.assertEquals(models.get("one"), attributes.get("one"));
        Assert.assertEquals(models.get("two"), attributes.get("two"));
    }

    @Test(expected = InternalServerErrorException.class)
    public void servletException() throws WebApplicationException, IOException, ServletException {
        JspViewContext context = new JspViewContext("test.jsp", request, response, uriInfo, resourceInfo, configuration,
                Collections.emptyMap());
        OutputStream entityStream = new ByteArrayOutputStream();
        MultivaluedMap<String, Object> httpHeaders = new MultivaluedHashMap<>();
        Mockito.doThrow(new ServletException()).when(dispatcher).forward(Mockito.any(), Mockito.any());
        writer.writeTo(context, null, null, null, null, httpHeaders, entityStream);
    }
}
