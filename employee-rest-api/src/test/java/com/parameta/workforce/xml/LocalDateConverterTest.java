package com.parameta.workforce.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;

class LocalDateConverterTest {

    private static final LocalDate DATE = LocalDate.of(1995, Month.MARCH, 15);

    @Test
    void parsesAnIsoDate() {
        assertEquals(DATE, LocalDateConverter.parse("1995-03-15"));
    }

    @Test
    void parsesNullAsNull() {
        assertNull(LocalDateConverter.parse(null));
    }

    @Test
    void parsesBlankAsNull() {
        assertNull(LocalDateConverter.parse("   "));
    }

    @Test
    void printsAnIsoDate() {
        assertEquals("1995-03-15", LocalDateConverter.print(DATE));
    }

    @Test
    void printsNullAsNull() {
        assertNull(LocalDateConverter.print(null));
    }
}
