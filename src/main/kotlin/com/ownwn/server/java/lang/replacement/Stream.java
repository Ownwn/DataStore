package com.ownwn.server.java.lang.replacement;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class Stream<T> implements Cloneable, java.util.stream.Stream<T> {
    private List<T> underlying;
    private List<Function<T, T>> operations;
    static <T> Stream<T> empty() {
        return null; // todo

    }


    Stream(List<T> col) {
        underlying = col; // todo mutate by someone else bad
    }

    Stream() {}

    @Override
    public Stream<T> filter(Predicate<? super T> predicate) {
        return new Stream<>();
    }

    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return null;// todo
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return IntStream.empty();
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return LongStream.empty();
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return DoubleStream.empty();
    }

    @Override
    public <R> java.util.stream.Stream<R> flatMap(Function<? super T, ? extends java.util.stream.Stream<? extends R>> mapper) {
        return java.util.stream.Stream.empty();
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return IntStream.empty();
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return LongStream.empty();
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return DoubleStream.empty();
    }

    @Override
    public java.util.stream.Stream<T> distinct() {
        return java.util.stream.Stream.empty();
    }

    @Override
    public java.util.stream.Stream<T> sorted() {
        return java.util.stream.Stream.empty();
    }

    @Override
    public java.util.stream.Stream<T> sorted(Comparator<? super T> comparator) {
        return java.util.stream.Stream.empty();
    }

    @Override
    public java.util.stream.Stream<T> peek(Consumer<? super T> action) {
        return java.util.stream.Stream.empty();
    }

    @Override
    public java.util.stream.Stream<T> limit(long maxSize) {
        return java.util.stream.Stream.empty();
    }

    @Override
    public java.util.stream.Stream<T> skip(long n) {
        return java.util.stream.Stream.empty();
    }

    @Override
    public void forEach(Consumer<? super T> action) {

    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {

    }

    @NotNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NotNull
    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return null;
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return null;
    }

    @NotNull
    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return Optional.empty();
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return null;
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return null;
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return null;
    }

    public List<T> toList() {
        return underlying; // todo
    }

    @NotNull
    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return Optional.empty();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return false;
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return false;
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return false;
    }

    @NotNull
    @Override
    public Optional<T> findFirst() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<T> findAny() {
        return Optional.empty();
    }

    @Override
    protected Stream<T> clone() {
        return new Stream<>() {
            {
                operations = new ArrayList<>(operations);
            }
        };
    }

    public String collect(Object o) {
        throw new Error("NYI");
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @NotNull
    @Override
    public Spliterator<T> spliterator() {
        return null;
    }

    @Override
    public boolean isParallel() {
        return false;
    }

    @NotNull
    @Override
    public java.util.stream.Stream<T> sequential() {
        return java.util.stream.Stream.empty();
    }

    @NotNull
    @Override
    public java.util.stream.Stream<T> parallel() {
        return java.util.stream.Stream.empty();
    }

    @NotNull
    @Override
    public java.util.stream.Stream<T> unordered() {
        return java.util.stream.Stream.empty();
    }

    @NotNull
    @Override
    public java.util.stream.Stream<T> onClose(@NotNull Runnable closeHandler) {
        return java.util.stream.Stream.empty();
    }

    @Override
    public void close() {

    }
}
