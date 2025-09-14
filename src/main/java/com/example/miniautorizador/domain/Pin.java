package com.example.miniautorizador.domain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

public class Pin {
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final String hashedValue;
    private final String rawValue;

    private Pin(String value, boolean isHashed) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("PIN não pode ser nulo ou vazio");
        }

        if (isHashed) {
            this.hashedValue = value;
            this.rawValue = null;
        } else {
            if (!value.matches("^\\d{4,6}$")) {
                throw new IllegalArgumentException("PIN deve conter de 4 a 6 dígitos");
            }

            this.rawValue = value;
            this.hashedValue = PASSWORD_ENCODER.encode(value);
        }
    }

    public static Pin of(String rawValue) {
        return new Pin(rawValue, false);
    }

    public static Pin fromHashed(String hashedValue) {
        return new Pin(hashedValue, true);
    }

    public boolean matches(String rawPin) {
        if (rawPin == null) {
            return false;
        }

        if (rawValue != null) {
            return rawValue.equals(rawPin);
        }

        return PASSWORD_ENCODER.matches(rawPin, hashedValue);
    }

    public String getHashedValue() {
        return hashedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pin pin = (Pin) o;
        return hashedValue.equals(pin.hashedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashedValue);
    }

    @Override
    public String toString() {
        return "****";
    }
}
