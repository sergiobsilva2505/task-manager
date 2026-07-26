package br.com.forjacode.taskmanager.domain.model;

import br.com.forjacode.taskmanager.domain.exception.InvalidInputException;
import br.com.forjacode.taskmanager.domain.exception.MissingRequiredFieldException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Nested
    @DisplayName("Successful creation")
    class Success {

        @Test
        @DisplayName("should create user with valid name and email")
        void shouldCreateUserWithValidNameAndEmail() {
            User user = User.create("John Doe", "john.doe@example.com");

            assertThat(user.getId()).isNotNull();
            assertThat(user.getName()).isEqualTo("John Doe");
            assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(user.getCreatedAt()).isNotNull();
            assertThat(user.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
        }

        @Test
        @DisplayName("should create user with name containing hyphens")
        void shouldCreateUserWithNameContainingHyphens() {
            User user = User.create("Mary-Jane", "mary@example.com");

            assertThat(user.getName()).isEqualTo("Mary-Jane");
        }

        @Test
        @DisplayName("should create user with name containing apostrophes")
        void shouldCreateUserWithNameContainingApostrophes() {
            User user = User.create("O'Connor", "oconnor@example.com");

            assertThat(user.getName()).isEqualTo("O'Connor");
        }

        @Test
        @DisplayName("should create user with minimum valid name length")
        void shouldCreateUserWithMinimumValidNameLength() {
            User user = User.create("Ana", "ana@example.com");

            assertThat(user.getName()).isEqualTo("Ana");
            assertThat(user.getName()).hasSize(3);
        }

        @Test
        @DisplayName("should create user with maximum valid name length")
        void shouldCreateUserWithMaximumValidNameLength() {
            String longName = "A".repeat(160);
            User user = User.create(longName, "user@example.com");

            assertThat(user.getName()).isEqualTo(longName);
            assertThat(user.getName()).hasSize(160);
        }

        @Test
        @DisplayName("should create user with name containing accented characters")
        void shouldCreateUserWithNameContainingAccentedCharacters() {
            User user = User.create("José García", "jose@example.com");

            assertThat(user.getName()).isEqualTo("José García");
        }
    }

    @Nested
    @DisplayName("Creation with error")
    class WithError {

        @Test
        @DisplayName("should throw exception when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            assertThatThrownBy(() -> User.create(null, "email@example.com"))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("Name cannot be null or blank");
        }

        @Test
        @DisplayName("should throw exception when name is blank")
        void shouldThrowExceptionWhenNameIsBlank() {
            assertThatThrownBy(() -> User.create("   ", "email@example.com"))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("Name cannot be null or blank");
        }

        @Test
        @DisplayName("should throw exception when name is empty")
        void shouldThrowExceptionWhenNameIsEmpty() {
            assertThatThrownBy(() -> User.create("", "email@example.com"))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("Name cannot be null or blank");
        }

        @Test
        @DisplayName("should throw exception when name is too short")
        void shouldThrowExceptionWhenNameIsTooShort() {
            assertThatThrownBy(() -> User.create("Jo", "email@example.com"))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Name must be between 3 and 160 characters");
        }

        @Test
        @DisplayName("should throw exception when name is too long")
        void shouldThrowExceptionWhenNameIsTooLong() {
            String longName = "A".repeat(161);
            assertThatThrownBy(() -> User.create(longName, "email@example.com"))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Name must be between 3 and 160 characters");
        }

        @Test
        @DisplayName("should throw exception when name contains numbers")
        void shouldThrowExceptionWhenNameContainsNumbers() {
            assertThatThrownBy(() -> User.create("John123", "email@example.com"))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Name must contain only letters, spaces, hyphens or apostrophes");
        }

        @Test
        @DisplayName("should throw exception when name contains special characters")
        void shouldThrowExceptionWhenNameContainsSpecialCharacters() {
            assertThatThrownBy(() -> User.create("John@Doe", "email@example.com"))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Name must contain only letters, spaces, hyphens or apostrophes");
        }

        @Test
        @DisplayName("should throw exception when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            assertThatThrownBy(() -> User.create("John Doe", null))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("Email cannot be null or blank");
        }

        @Test
        @DisplayName("should throw exception when email is blank")
        void shouldThrowExceptionWhenEmailIsBlank() {
            assertThatThrownBy(() -> User.create("John Doe", "   "))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("Email cannot be null or blank");
        }

        @Test
        @DisplayName("should throw exception when email is empty")
        void shouldThrowExceptionWhenEmailIsEmpty() {
            assertThatThrownBy(() -> User.create("John Doe", ""))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("Email cannot be null or blank");
        }
    }

    @Nested
    @DisplayName("Reconstruction")
    class Reconstruction {

        @Nested
        @DisplayName("Successful reconstruction")
        class Success {

            @Test
            @DisplayName("should reconstruct user with valid data")
            void shouldReconstructUserWithValidData() {
                UUID id = UUID.randomUUID();
                Instant createdAt = Instant.parse("2026-07-26T10:00:00Z");

                User user = User.reconstruct(id, "John Doe", "john@example.com", createdAt);

                assertThat(user.getId()).isEqualTo(id);
                assertThat(user.getName()).isEqualTo("John Doe");
                assertThat(user.getEmail()).isEqualTo("john@example.com");
                assertThat(user.getCreatedAt()).isEqualTo(createdAt);
            }
        }

        @Nested
        @DisplayName("Reconstruction with error")
        class WithError {

            private UUID id;

            @BeforeEach
            void setUp() {
                id = UUID.randomUUID();
            }

            @Test
            @DisplayName("should throw exception when createdAt is null")
            void shouldThrowExceptionWhenCreatedAtIsNull() {
                assertThatThrownBy(() -> User.reconstruct(id, "John Doe", "email@example.com", null))
                        .isInstanceOf(MissingRequiredFieldException.class)
                        .hasMessage("CreatedAt cannot be null");
            }
        }
    }
}