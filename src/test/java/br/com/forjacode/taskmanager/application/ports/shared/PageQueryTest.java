package br.com.forjacode.taskmanager.application.ports.shared;

import br.com.forjacode.taskmanager.application.ports.shared.exception.InvalidPageQueryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageQueryTest {

    @Nested
    @DisplayName("Successful creation")
    class Success {

        @Test
        @DisplayName("should create page query with valid values")
        void shouldCreatePageQueryWithValidValues() {
            PageQuery query = new PageQuery(1, 10, TaskSortField.TITLE, SortDirection.ASC);

            assertThat(query.page()).isEqualTo(1);
            assertThat(query.size()).isEqualTo(10);
            assertThat(query.sortBy()).isEqualTo(TaskSortField.TITLE);
            assertThat(query.sortDirection()).isEqualTo(SortDirection.ASC);
        }

        @Test
        @DisplayName("should create page query with minimum valid page and size")
        void shouldCreatePageQueryWithMinimumValidPageAndSize() {
            PageQuery query = new PageQuery(0, 1, TaskSortField.DEFAULT, SortDirection.DESC);

            assertThat(query.page()).isZero();
            assertThat(query.size()).isEqualTo(1);
            assertThat(query.sortBy()).isEqualTo(TaskSortField.DEFAULT);
            assertThat(query.sortDirection()).isEqualTo(SortDirection.DESC);
        }
    }

    @Nested
    @DisplayName("Creation with error")
    class WithError {

        @Test
        @DisplayName("should throw exception when page is negative")
        void shouldThrowExceptionWhenPageIsNegative() {
            assertThatThrownBy(() -> new PageQuery(-1, 10, TaskSortField.TITLE, SortDirection.ASC))
                    .isInstanceOf(InvalidPageQueryException.class)
                    .hasMessage("Page index must be >= 0");
        }

        @Test
        @DisplayName("should throw exception when size is zero")
        void shouldThrowExceptionWhenSizeIsZero() {
            assertThatThrownBy(() -> new PageQuery(0, 0, TaskSortField.TITLE, SortDirection.ASC))
                    .isInstanceOf(InvalidPageQueryException.class)
                    .hasMessage("Page size must be > 0");
        }

        @Test
        @DisplayName("should throw exception when size is negative")
        void shouldThrowExceptionWhenSizeIsNegative() {
            assertThatThrownBy(() -> new PageQuery(0, -1, TaskSortField.TITLE, SortDirection.ASC))
                    .isInstanceOf(InvalidPageQueryException.class)
                    .hasMessage("Page size must be > 0");
        }

        @Test
        @DisplayName("should throw exception when sort field is null")
        void shouldThrowExceptionWhenSortFieldIsNull() {
            assertThatThrownBy(() -> new PageQuery(0, 10, null, SortDirection.ASC))
                    .isInstanceOf(InvalidPageQueryException.class)
                    .hasMessage("SortBy must not be null");
        }

        @Test
        @DisplayName("should throw exception when sort direction is null")
        void shouldThrowExceptionWhenSortDirectionIsNull() {
            assertThatThrownBy(() -> new PageQuery(0, 10, TaskSortField.TITLE, null))
                    .isInstanceOf(InvalidPageQueryException.class)
                    .hasMessage("SortDirection must not be null");
        }
    }
}

