package com.backend.Skytouch.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationUtilsTest {

    @Test
    void pageable_clampsSizeToMax() {
        var pageable = PaginationUtils.pageable(0, 500, Sort.unsorted());
        assertThat(pageable.getPageSize()).isEqualTo(PaginationUtils.MAX_PAGE_SIZE);
    }

    @Test
    void pageable_clampsNegativePageToZero() {
        var pageable = PaginationUtils.pageable(-1, 10, Sort.unsorted());
        assertThat(pageable.getPageNumber()).isZero();
    }

    @Test
    void mapPage_mapsContentAndMetadata() {
        Page<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 5);
        var response = PaginationUtils.mapPage(page, String::toUpperCase);

        assertThat(response.getContent()).containsExactly("A", "B");
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(2);
        assertThat(response.getTotalElements()).isEqualTo(5);
        assertThat(response.getTotalPages()).isEqualTo(3);
    }
}
