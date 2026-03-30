package com.example;

import com.example.records.Point;
import com.example.sealed.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Tutorial demo app — demonstrates features from the tutorial chapters.
 * Run with: ./gradlew run
 */
public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        log.info("=== Modern Java Features Tutorial ===");

        // Chapter 6: Records
        Point p1 = new Point(3, 4);
        Point origin = Point.origin();
        log.info("Distance from {} to origin: {}", p1, p1.distanceTo(origin));

        // Chapter 7: Sealed classes + switch pattern matching
        List<Shape> shapes = List.of(
            new Circle(5),
            new Rectangle(4, 6),
            new Triangle(3, 8)
        );

        shapes.forEach(shape -> {
            String description = switch (shape) {
                case Circle c    -> "Circle with radius %.1f, area=%.2f".formatted(c.radius(), c.area());
                case Rectangle r -> "Rectangle %.1fx%.1f, area=%.2f".formatted(r.width(), r.height(), r.area());
                case Triangle t  -> "Triangle base=%.1f height=%.1f, area=%.2f".formatted(t.base(), t.height(), t.area());
            };
            log.info(description);
        });

        // Chapter 5: Streams
        List<String> names = List.of("Alice", "Bob", "Charlie", "Anna", "Brian");
        String result = names.stream()
            .filter(n -> n.length() > 3)
            .sorted()
            .collect(Collectors.joining(", "));
        log.info("Long names: {}", result);

        // Chapter 8: Text block + var
        var json = """
                {
                    "tutorial": "Modern Java",
                    "chapters": 12
                }
                """;
        log.info("Sample JSON: {}", json.strip());

        log.info("=== Tutorial demo complete ===");
    }
}
