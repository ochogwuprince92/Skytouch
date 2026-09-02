package com.backend.Skytouch.common.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class CsvUtils {

    private CsvUtils() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static String row(String... values) {
        return Arrays.stream(values)
                .map(CsvUtils::escape)
                .collect(Collectors.joining(",")) + "\n";
    }
}
