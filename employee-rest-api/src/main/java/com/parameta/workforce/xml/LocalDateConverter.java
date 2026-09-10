package com.parameta.workforce.xml;

import java.time.LocalDate;

public final class LocalDateConverter {

    private LocalDateConverter() {
    }

    public static LocalDate parse(String value) {
        return (value == null || value.isBlank()) ? null : LocalDate.parse(value);
    }

    public static String print(LocalDate value) {
        return (value == null) ? null : value.toString();
    }
}
