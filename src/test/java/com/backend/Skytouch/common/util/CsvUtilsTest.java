package com.backend.Skytouch.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CsvUtilsTest {

    @Test
    void escape_returnsPlainValueUnchanged() {
        assertThat(CsvUtils.escape("hello")).isEqualTo("hello");
    }

    @Test
    void escape_wrapsValueWithCommas() {
        assertThat(CsvUtils.escape("hello, world")).isEqualTo("\"hello, world\"");
    }

    @Test
    void escape_doublesInternalQuotes() {
        assertThat(CsvUtils.escape("say \"hi\"")).isEqualTo("\"say \"\"hi\"\"\"");
    }

    @Test
    void row_joinsEscapedValues() {
        assertThat(CsvUtils.row("a", "b,c", null)).isEqualTo("a,\"b,c\",\n");
    }
}
