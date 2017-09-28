package com.swiftelan.rafter.cache.boundary;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

@Provider
@HttpCache
public class HttpCacheContainerResponseFilter implements ContainerResponseFilter {

    @Context
    ResourceInfo resource;

    @Context
    Configuration configuration;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        CacheControl cache = new CacheControl();
        cache.setPrivate(true);

        Optional<HttpCache> entityAnnotation = Arrays.stream(responseContext.getEntityAnnotations())
                .filter(a -> HttpCache.class.equals(a.annotationType())).map(a -> (HttpCache) a).findFirst();
        Optional<HttpCache> cacheOption = Stream
                .of(entityAnnotation, ofElement(resource.getResourceMethod()), ofElement(resource.getResourceClass()))
                .filter(Optional::isPresent).map(Optional::get).findFirst();

        if (cacheOption.isPresent()) {
            HttpCache cacheAnnotation = cacheOption.get();
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

    static Optional<HttpCache> ofElement(AnnotatedElement element) {
        return Optional.ofNullable(element).map(e -> e.getAnnotation(HttpCache.class));
    }
}
