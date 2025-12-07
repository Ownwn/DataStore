package com.ownwn.server;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

interface RequestHandler {
    Response handle(Request request);

    HttpMethod method();


    static RequestHandler from(Method m, Handle handle, Object instance) {
        validateExchangeMethod(m);
        return new RequestHandler() {
            @Override
            public Response handle(Request request) {
                try {
                    Object res = m.invoke(instance, request);
                    return (Response) res;
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
        if (m.getParameterCount() != 1 || !Request.class.isAssignableFrom(m.getParameterTypes()[0])) {
            throw new RuntimeException("Invalid handler parameters for method " + m + ". Should be " + Request.class);
        }

        if (!Response.class.isAssignableFrom(m.getReturnType())) {
            throw new RuntimeException("Invalid handler return type for method " + m + ". Should be " + Response.class);
        }
        if (Modifier.isStatic(m.getModifiers())) {
            throw new RuntimeException("Handler " + m + " must not be static");
        }
    }
}
