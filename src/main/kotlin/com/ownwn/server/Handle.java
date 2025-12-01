package com.ownwn.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Handle {
    String value();

    HttpMethod method() default HttpMethod.GET;

    boolean requiresAuth() default true;
}
