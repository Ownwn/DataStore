package com.ownwn.server.intercept;

import com.ownwn.server.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotating a class with this will make the server attempt to call it when any endpoint is called */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Intercept {
    HttpMethod method() default HttpMethod.GET;
}
