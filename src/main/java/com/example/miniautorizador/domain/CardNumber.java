package com.example.miniautorizador.domain;

import java.util.Objects;

public class CardNumber {
    private final String value;

    private CardNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Número do cartão não pode ser nulo ou vazio");
        }

        String normalized = value.replaceAll("[^0-9]", "");

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Número do cartão deve conter pelo menos um dígito");
        }

        if (normalized.length() < 13 || normalized.length() > 19) {
            throw new IllegalArgumentException("Número do cartão deve ter entre 13 e 19 dígitos");
        }

        this.value = normalized;
    }

    public static CardNumber of(String value) {
        return new CardNumber(value);
    }

    public String getValue() {
        return value;
    }

    public String getMasked() {
        if (value.length() <= 4) {
            return value;
        }

        String firstSix = value.substring(0, 6);
        String lastFour = value.substring(value.length() - 4);

        StringBuilder middle = new StringBuilder();
        for (int i = 0; i < value.length() - 10; i++) {
            middle.append("X");
        }

        return firstSix + middle.toString() + lastFour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardNumber that = (CardNumber) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return getMasked();
    }
}
