package com.ownwn.server.intercept;

import com.ownwn.server.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Intercept {
    HttpMethod method() default HttpMethod.GET;
}
