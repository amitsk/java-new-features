package com.example;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Calculator")
class CalculatorTest {

    private Calculator calc;

    @BeforeEach
    void setUp() {
        calc = new Calculator();
    }

    @Test
    @DisplayName("should add two positive numbers")
    void addPositiveNumbers() {
        assertThat(calc.add(2, 3)).isEqualTo(5);
    }

    @Test
    @DisplayName("should return negative result when subtracting larger number")
    void subtractLargerFromSmaller() {
        assertThat(calc.subtract(3, 10)).isEqualTo(-7);
    }

    @Test
    @DisplayName("should throw ArithmeticException when dividing by zero")
    void divideByZeroThrows() {
        assertThatThrownBy(() -> calc.divide(10, 0))
            .isInstanceOf(ArithmeticException.class)
            .hasMessage("Cannot divide by zero");
    }

    @ParameterizedTest(name = "{index}: {0} + {1} = {2}")
    @CsvSource({
        "1,  2,  3",
        "0,  0,  0",
        "-1, 1,  0",
        "10, 20, 30"
    })
    @DisplayName("parameterized addition")
    void addParameterized(int a, int b, int expected) {
        assertThat(calc.add(a, b)).isEqualTo(expected);
    }
}
