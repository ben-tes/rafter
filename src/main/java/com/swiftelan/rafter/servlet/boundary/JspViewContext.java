package com.swiftelan.rafter.servlet.boundary;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.UriInfo;

public class JspViewContext {
    private final String view;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final UriInfo uriInfo;
    private final ResourceInfo resourceInfo;
    private final Configuration configuration;
    private final Map<String, Object> model;

    public JspViewContext(String view, HttpServletRequest request, HttpServletResponse response, UriInfo uriInfo,
            ResourceInfo resourceInfo, Configuration configuration, Map<String, Object> model) {
        this.view = view;
        this.request = request;
        this.response = response;
        this.uriInfo = uriInfo;
        this.resourceInfo = resourceInfo;
        this.configuration = configuration;
        this.model = model;
    }

    /**
     * Returns the view.
     *
     * @return the view.
     */
    String getView() {
        return view;
    }

    /**
     * Returns the model instance needed to process the view.
     *
     * @return the model instance.
     */
    Map<String, Object> getModel() {
        return model;
    }

    /**
     * Returns HTTP request object from the Servlet container.
     *
     * @return HTTP request object.
     */
    HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Returns HTTP response object from the servlet container. The underlying
     * output stream should be used to write the result of processing a view.
     *
     * @return HTTP response object.
     */
    HttpServletResponse getResponse() {
        return response;
    }

    /**
     * Returns the {@link javax.ws.rs.core.UriInfo} instance containing information
     * about the current request URI.
     *
     * @return the URI info for the current request.
     * @see javax.ws.rs.core.UriInfo
     */
    UriInfo getUriInfo() {
        return uriInfo;
    }

    /**
     * Returns the {@link javax.ws.rs.container.ResourceInfo} instance containing
     * information about the controller method matched in the current request.
     *
     * @return the resource info for the current request.
     * @see javax.ws.rs.container.ResourceInfo
     */
    ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    /**
     * Returns the application's configuration. The configuration provides access to
     * properties such as {@link javax.mvc.engine.ViewEngine#VIEW_FOLDER}, which
     * view engines must use to locate views.
     *
     * @return application's configuration.
     * @see javax.ws.rs.core.Configuration
     */
    Configuration getConfiguration() {
        return configuration;
    }
}
