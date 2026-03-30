package com.example.sealed;

/**
 * Chapter 7: Sealed Classes — a closed Shape hierarchy.
 * <p>
 * Every Shape variant is listed in the permits clause. The compiler can then
 * verify that switch expressions over Shape are exhaustive.
 */
public sealed interface Shape permits Circle, Rectangle, Triangle {
    double area();
}
