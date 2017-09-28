package com.swiftelan.rafter.cache.boundary;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.NameBinding;
import javax.ws.rs.core.Request;

/**
 * Name binding annotations for REST resources that allow HTTP caching
 *
 * <p>
 * The framework supplies the <code>Cache-Control</code> HTTP header. It is the
 * responsibility of the resource implementation to provide support for
 * conditional requests with the <code>ETag</code> and
 * {@link Request#evaluatePreconditions(javax.ws.rs.core.EntityTag)} check.
 * </p>
 *
 * @author johna2
 * @since 1.0
 */
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@NameBinding
public @interface HttpCache {

    /**
     * Value for the <code>max-age</code> cache-request-directive.
     *
     * @return Time in seconds the client is allowed to cache the response.
     */
    int maxAge() default -1;

    /**
     * <p>
     * Controls the <code>[private | public]</code> directives of the
     * <code>Cache-Control</code> header.
     * </p>
     *
     * <p>
     * Essentially they let intermediary caches know that a given response is
     * specific to the end user and should not be cached. Do not make the mistake of
     * assuming that this in any way provides you with some kind of security or
     * privacy: Keep using SSL for that.
     * </p>
     *
     * @return <code>true</code> to apply '<code>Cache-Control: private</code>',
     *         <code>false</code> to apply ' <code>Cache-Control: public</code>'.
     *         Default is <code>true</code>
     */
    boolean privateFlag() default true;

    /**
     * Specify that caches should re-validate this resource every time, typically
     * using the <code>ETag</code> header.
     *
     * <p>
     * A cache MUST NOT use the response to satisfy a subsequent request without
     * successful revalidation with the origin server. This allows an origin server
     * to prevent caching even by caches that have been configured to return stale
     * responses to client requests.
     * </p>
     *
     * @return <code>true</code> to apply '<code>Cache-Control: no-cache</code> ',
     *         <code>false</code> to omit the <code>no-cache</code> directive.
     *         Default is <code>false</code>
     */
    boolean noCache() default false;

    /**
     * The response and the request that created it must not be stored on any cache,
     * whether shared or private. The storage inferred here is non-volatile storage,
     * such as tape backups. This is not an infallible security measure.
     *
     * @return <code>true</code> to apply '<code>Cache-Control: no-store</code> ',
     *         <code>false</code> to omit the <code>no-store</code> directive.
     *         Default is <code>false</code>
     */
    boolean noStore() default false;

    /**
     * When the <code>must-revalidate</code> directive is present in a response
     * received by a cache, that cache MUST NOT use the entry after it becomes stale
     * to respond to a subsequent request without first re-validating it with the
     * origin server.
     *
     * <p>
     * A cached response is state after the <code>max-age</code> elapses from the
     * original request time.
     * </p>
     *
     * @return <code>true</code> to apply
     *         '<code>Cache-Control: must-revalidate</code> ', <code>false</code> to
     *         omit the <code>must-revalidate</code> directive. Default is
     *         <code>false</code>
     */
    boolean mustRevalidate() default false;
}
