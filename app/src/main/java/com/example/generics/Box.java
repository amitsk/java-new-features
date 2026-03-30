package com.example.generics;

import java.util.function.Function;

/**
 * Chapter 4: Generics — a simple generic container.
 */
public class Box<T> {
    private final T value;

    public Box(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    /** Transform the contained value, returning a new Box of the result type. */
    public <R> Box<R> map(Function<T, R> transform) {
        return new Box<>(transform.apply(value));
    }

    @Override
    public String toString() {
        return "Box[" + value + "]";
    }
}
