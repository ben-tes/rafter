package com.swiftelan.rafter.cache.boundary;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class HttpCacheContainerResponseFilterTest {

    private HttpCacheContainerResponseFilter filter;
    private ContainerRequestContext requestContext;
    private ContainerResponseContext responseContext;

    @Before
    public void before() {
        filter = new HttpCacheContainerResponseFilter();
        filter.resource = Mockito.mock(ResourceInfo.class);
        Mockito.when(filter.resource.getResourceClass()).then(invocation -> ContainerRequestContext.class);
        Mockito.when(filter.resource.getResourceMethod())
                .then(invocation -> ContainerRequestContext.class.getMethod("getRequest"));

        requestContext = Mockito.mock(ContainerRequestContext.class);
        responseContext = Mockito.mock(ContainerResponseContext.class);
        Mockito.when(responseContext.getHeaders()).thenReturn(new MultivaluedHashMap<>());
        Mockito.when(responseContext.getEntityAnnotations()).thenReturn(new Annotation[] {});
    }

    @Test
    public void noAnnotation() throws IOException {
        filter.filter(requestContext, responseContext);
        Assert.assertFalse(responseContext.getHeaders().containsKey(HttpHeaders.CACHE_CONTROL));
    }

    @Test
    public void entityAnnotation() throws IOException {
        Mockito.when(responseContext.getEntityAnnotations())
                .thenReturn(new Annotation[] { new CacheLiteral(800, false, false, false, true) });
        filter.filter(requestContext, responseContext);
        Assert.assertTrue(responseContext.getHeaders().containsKey(HttpHeaders.CACHE_CONTROL));
        CacheControl cacheControl = (CacheControl) responseContext.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL);
        Assert.assertNotNull(cacheControl);
        Assert.assertEquals(800, cacheControl.getMaxAge());
    }

    @Test
    public void resourceClass() throws IOException {
        Mockito.when(filter.resource.getResourceClass()).then(i -> ResourceClass.class);
        filter.filter(requestContext, responseContext);
        Assert.assertTrue(responseContext.getHeaders().containsKey(HttpHeaders.CACHE_CONTROL));
        CacheControl cacheControl = (CacheControl) responseContext.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL);
        Assert.assertNotNull(cacheControl);
        Assert.assertEquals(900, cacheControl.getMaxAge());
    }

    @Test
    public void resourceMethod() throws IOException {
        Mockito.when(filter.resource.getResourceClass()).then(i -> ResourceClass.class);
        Mockito.when(filter.resource.getResourceMethod()).then(i -> ResourceClass.class.getMethod("test"));
        filter.filter(requestContext, responseContext);
        Assert.assertTrue(responseContext.getHeaders().containsKey(HttpHeaders.CACHE_CONTROL));
        CacheControl cacheControl = (CacheControl) responseContext.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL);
        Assert.assertNotNull(cacheControl);
        Assert.assertEquals(100, cacheControl.getMaxAge());
    }

    class CacheLiteral implements HttpCache {

        private final int maxAge;
        private final boolean privateFlag;
        private final boolean noCache;
        private final boolean noStore;
        private final boolean mustRevalidate;

        public CacheLiteral(int maxAge, boolean privateFlag, boolean noCache, boolean noStore, boolean mustRevalidate) {
            this.maxAge = maxAge;
            this.privateFlag = privateFlag;
            this.noCache = noCache;
            this.noStore = noStore;
            this.mustRevalidate = mustRevalidate;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return HttpCache.class;
        }

        @Override
        public int maxAge() {
            return maxAge;
        }

        @Override
        public boolean privateFlag() {
            return privateFlag;
        }

        @Override
        public boolean noCache() {
            return noCache;
        }

        @Override
        public boolean noStore() {
            return noStore;
        }

        @Override
        public boolean mustRevalidate() {
            return mustRevalidate;
        }

    }

    @HttpCache(maxAge = 900)
    class ResourceClass {

        @HttpCache(maxAge = 100)
        public void test() {
            // Test resource method
        }
    }
}
