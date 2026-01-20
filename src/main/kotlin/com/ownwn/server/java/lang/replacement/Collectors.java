package com.ownwn.server.java.lang.replacement;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class Collectors {

    public static Collector<CharSequence, StringBuilder, String> joining(String s) {
        return new Collector<>() {
            private boolean added = false;
            @Override
            public Supplier<StringBuilder> supplier() {
                return StringBuilder::new;
            }

            @Override
            public BiConsumer<StringBuilder, CharSequence> accumulator() {
                return (sb, cs) -> {added = true; sb.append(s).append(cs);};
            }

            @Override
            public BinaryOperator<StringBuilder> combiner() {
                return (o1, o2) -> new StringBuilder(o1).append(s).append(o2);
            }

            @Override
            public Function<StringBuilder, String> finisher() {
                return sb -> {if (added) {sb.delete(sb.length()-1-s.length(), sb.length()); } return sb.toString();};
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        };
    }
}
