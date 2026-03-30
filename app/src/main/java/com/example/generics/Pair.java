package com.example.generics;

/**
 * Chapter 4: Generics — a generic pair.
 */
public record Pair<A, B>(A first, B second) {

    public Pair<B, A> swap() {
        return new Pair<>(second, first);
    }
}
