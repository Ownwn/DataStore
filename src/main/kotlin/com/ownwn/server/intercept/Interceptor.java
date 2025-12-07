package com.ownwn.server.intercept;

import com.ownwn.server.HttpMethod;
import com.ownwn.server.Request;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public interface Interceptor { // todo jdoc for everything

    void handle(Request request, InterceptReciever interceptReciever);

    HttpMethod method();


    static Interceptor from(Method m, Intercept handle, Object instance) {
        validateExchangeMethod(m);
        return new Interceptor() {
            @Override
            public void handle(Request request, InterceptReciever interceptReciever) {
                try {
                    m.invoke(instance, request, interceptReciever);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public HttpMethod method() {
                return handle.method();
            }
        };

    }

    private static void validateExchangeMethod(Method m) {
        if (m.getParameterCount() != 2 || !Request.class.isAssignableFrom(m.getParameterTypes()[0]) || !InterceptReciever.class.isAssignableFrom(m.getParameterTypes()[1])) {
            throw new RuntimeException("Invalid handler parameters for method " + m + ". Should be " + Request.class + ", " + InterceptReciever.class);
        }

        if (!void.class.isAssignableFrom(m.getReturnType())) {
            throw new RuntimeException("Invalid handler return type for method " + m + ". Should be void");
        }
        if (Modifier.isStatic(m.getModifiers())) {
            throw new RuntimeException("Handler " + m + " must not be static");
        }
    }
}
