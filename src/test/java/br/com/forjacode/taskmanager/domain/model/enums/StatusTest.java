package br.com.forjacode.taskmanager.domain.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusTest {

    @Nested
    @DisplayName("Can transition to")
    class CanTransitionTo {

        @Nested
        @DisplayName("From TODO")
        class FromTodo {

            @Test
            @DisplayName("should allow transition to IN_PROGRESS")
            void shouldAllowTransitionToInProgress() {
                boolean canTransition = Status.TODO.canTransitionTo(Status.IN_PROGRESS);

                assertThat(canTransition).isTrue();
            }

            @Test
            @DisplayName("should allow transition to CANCELLED")
            void shouldAllowTransitionToCancelled() {
                boolean canTransition = Status.TODO.canTransitionTo(Status.CANCELLED);

                assertThat(canTransition).isTrue();
            }

            @Test
            @DisplayName("should not allow transition to DONE")
            void shouldNotAllowTransitionToDone() {
                boolean canTransition = Status.TODO.canTransitionTo(Status.DONE);

                assertThat(canTransition).isFalse();
            }

            @Test
            @DisplayName("should not allow transition to TODO")
            void shouldNotAllowTransitionToTodo() {
                boolean canTransition = Status.TODO.canTransitionTo(Status.TODO);

                assertThat(canTransition).isFalse();
            }
        }

        @Nested
        @DisplayName("From IN_PROGRESS")
        class FromInProgress {

            @Test
            @DisplayName("should allow transition to DONE")
            void shouldAllowTransitionToDone() {
                boolean canTransition = Status.IN_PROGRESS.canTransitionTo(Status.DONE);

                assertThat(canTransition).isTrue();
            }

            @Test
            @DisplayName("should allow transition to CANCELLED")
            void shouldAllowTransitionToCancelled() {
                boolean canTransition = Status.IN_PROGRESS.canTransitionTo(Status.CANCELLED);

                assertThat(canTransition).isTrue();
            }

            @Test
            @DisplayName("should not allow transition to TODO")
            void shouldNotAllowTransitionToTodo() {
                boolean canTransition = Status.IN_PROGRESS.canTransitionTo(Status.TODO);

                assertThat(canTransition).isFalse();
            }

            @Test
            @DisplayName("should not allow transition to IN_PROGRESS")
            void shouldNotAllowTransitionToInProgress() {
                boolean canTransition = Status.IN_PROGRESS.canTransitionTo(Status.IN_PROGRESS);

                assertThat(canTransition).isFalse();
            }
        }

        @Nested
        @DisplayName("From DONE")
        class FromDone {

            @Test
            @DisplayName("should not allow transition to TODO")
            void shouldNotAllowTransitionToTodo() {
                boolean canTransition = Status.DONE.canTransitionTo(Status.TODO);

                assertThat(canTransition).isFalse();
            }

            @Test
            @DisplayName("should not allow transition to IN_PROGRESS")
            void shouldNotAllowTransitionToInProgress() {
                boolean canTransition = Status.DONE.canTransitionTo(Status.IN_PROGRESS);

                assertThat(canTransition).isFalse();
            }

            @Test
            @DisplayName("should not allow transition to CANCELLED")
            void shouldNotAllowTransitionToCancelled() {
                boolean canTransition = Status.DONE.canTransitionTo(Status.CANCELLED);

                assertThat(canTransition).isFalse();
            }

            @Test
            @DisplayName("should not allow transition to DONE")
            void shouldNotAllowTransitionToDone() {
                boolean canTransition = Status.DONE.canTransitionTo(Status.DONE);

                assertThat(canTransition).isFalse();
            }
        }

        @Nested
        @DisplayName("From CANCELLED")
        class FromCancelled {

            @Test
            @DisplayName("should not allow transition to TODO")
            void shouldNotAllowTransitionToTodo() {
                boolean canTransition = Status.CANCELLED.canTransitionTo(Status.TODO);

                assertThat(canTransition).isFalse();
            }

            @Test
            @DisplayName("should not allow transition to IN_PROGRESS")
            void shouldNotAllowTransitionToInProgress() {
                boolean canTransition = Status.CANCELLED.canTransitionTo(Status.IN_PROGRESS);

                assertThat(canTransition).isFalse();
            }

            @Test
            @DisplayName("should not allow transition to DONE")
            void shouldNotAllowTransitionToDone() {
                boolean canTransition = Status.CANCELLED.canTransitionTo(Status.DONE);

                assertThat(canTransition).isFalse();
            }

            @Test
            @DisplayName("should not allow transition to CANCELLED")
            void shouldNotAllowTransitionToCancelled() {
                boolean canTransition = Status.CANCELLED.canTransitionTo(Status.CANCELLED);

                assertThat(canTransition).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("Is terminal")
    class IsTerminal {

        @Test
        @DisplayName("should return true for DONE")
        void shouldReturnTrueForDone() {
            boolean isTerminal = Status.DONE.isTerminal();

            assertThat(isTerminal).isTrue();
        }

        @Test
        @DisplayName("should return true for CANCELLED")
        void shouldReturnTrueForCancelled() {
            boolean isTerminal = Status.CANCELLED.isTerminal();

            assertThat(isTerminal).isTrue();
        }

        @Test
        @DisplayName("should return false for TODO")
        void shouldReturnFalseForTodo() {
            boolean isTerminal = Status.TODO.isTerminal();

            assertThat(isTerminal).isFalse();
        }

        @Test
        @DisplayName("should return false for IN_PROGRESS")
        void shouldReturnFalseForInProgress() {
            boolean isTerminal = Status.IN_PROGRESS.isTerminal();

            assertThat(isTerminal).isFalse();
        }
    }
}

