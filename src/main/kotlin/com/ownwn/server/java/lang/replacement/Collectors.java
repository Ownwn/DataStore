package com.ownwn.server.java.lang.replacement;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class Collectors {

    public static Collector<CharSequence, ?, String> joining(String s) {
        return new Collector<>() {
            private final StringBuilder builder = new StringBuilder();

            @Override
            public Supplier<Object> supplier() {
                return StringBuilder::new;
            }

            @Override
            public BiConsumer<Object, CharSequence> accumulator() {
                return StringBuilder::append;
            }

            @Override
            public BinaryOperator<Object> combiner() {
                return (o1, o2) -> (Object) o1 + o2;
            }

            @Override
            public Function<Object, String> finisher() {
                return Object::toString;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        }
    }
}
