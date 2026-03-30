package com.example.sealed;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Sealed Shape hierarchy")
class ShapeTest {

    @Test
    @DisplayName("Circle area should be π * r²")
    void circleArea() {
        Shape shape = new Circle(5);
        assertThat(shape.area()).isCloseTo(Math.PI * 25, within(0.0001));
    }

    @Test
    @DisplayName("Rectangle area should be width * height")
    void rectangleArea() {
        Shape shape = new Rectangle(4, 6);
        assertThat(shape.area()).isEqualTo(24.0);
    }

    @Test
    @DisplayName("Triangle area should be 0.5 * base * height")
    void triangleArea() {
        Shape shape = new Triangle(3, 8);
        assertThat(shape.area()).isEqualTo(12.0);
    }

    @Test
    @DisplayName("switch on sealed Shape is exhaustive")
    void switchOnSealedShape() {
        Shape shape = new Circle(1);
        String label = switch (shape) {
            case Circle c    -> "circle";
            case Rectangle r -> "rectangle";
            case Triangle t  -> "triangle";
        };
        assertThat(label).isEqualTo("circle");
    }
}
