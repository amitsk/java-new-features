package com.example.records;

import java.util.Objects;

/**
 * Chapter 6: Records — a simple immutable value object.
 */
public record Point(int x, int y) {

    /** Custom validation in compact constructor */
    // public Point { /* could validate here if needed */ }

    public double distanceTo(Point other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static Point origin() {
        return new Point(0, 0);
    }
}
