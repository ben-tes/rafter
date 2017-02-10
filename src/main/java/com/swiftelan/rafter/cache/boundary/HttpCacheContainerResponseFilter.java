package com.swiftelan.rafter.cache.boundary;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

@Provider
@HttpCache
public class HttpCacheContainerResponseFilter implements ContainerResponseFilter {

    @Context
    ResourceInfo resource;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        CacheControl cache = new CacheControl();
        cache.setPrivate(true);

        Optional<Annotation> entityAnnotation = Arrays.stream(responseContext.getEntityAnnotations())
                .filter(a -> HttpCache.class.equals(a.annotationType())).findFirst();
        HttpCache cacheAnnotation = null;
        if (entityAnnotation.isPresent()) {
            cacheAnnotation = (HttpCache) entityAnnotation.get();
        } else {
            cacheAnnotation = resource.getResourceMethod().getAnnotation(HttpCache.class);
            if (cacheAnnotation == null) {
                cacheAnnotation = resource.getResourceClass().getAnnotation(HttpCache.class);
            }
        }
        if (cacheAnnotation != null) {
            cache.setMaxAge(cacheAnnotation.maxAge());
            cache.setMustRevalidate(cacheAnnotation.mustRevalidate());
            cache.setNoCache(cacheAnnotation.noCache());
            cache.setNoStore(cacheAnnotation.noStore());
            cache.setPrivate(cacheAnnotation.privateFlag());
            if (!responseContext.getHeaders().containsKey(HttpHeaders.CACHE_CONTROL)) {
                responseContext.getHeaders().add(HttpHeaders.CACHE_CONTROL, cache);
                responseContext.getHeaders().remove(HttpHeaders.EXPIRES);
                responseContext.getHeaders().putSingle("Pragma", "cache-control");
            }
        }
    }
}
