package br.com.forjacode.taskmanager.adapters.input.rest.resolver;

import br.com.forjacode.taskmanager.adapters.input.rest.annotation.CurrentUserId;
import br.com.forjacode.taskmanager.domain.exception.MissingCurrentUserException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentUserIdArgumentResolverTest {

    private final CurrentUserIdArgumentResolver resolver = new CurrentUserIdArgumentResolver();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @SuppressWarnings("unused")
    private void dummyMethod(@CurrentUserId UUID annotatedUuidParam, UUID plainUuidParam,
            @CurrentUserId String annotatedStringParam) {
    }

    private MethodParameter parameterAt(int index) throws NoSuchMethodException {
        Method method = getClass().getDeclaredMethod("dummyMethod", UUID.class, UUID.class, String.class);
        return new MethodParameter(method, index);
    }

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should support parameter annotated with @CurrentUserId of type UUID")
        void shouldSupportParameterAnnotatedWithCurrentUserIdOfTypeUuid() throws NoSuchMethodException {
            assertThat(resolver.supportsParameter(parameterAt(0))).isTrue();
        }

        @Test
        @DisplayName("should resolve argument to the authenticated user id")
        void shouldResolveArgumentToTheAuthenticatedUserId() {
            UUID userId = UUID.randomUUID();
            var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Object result = resolver.resolveArgument(null, null, null, null);

            assertThat(result).isEqualTo(userId);
        }
    }

    @Nested
    @DisplayName("WithError")
    class WithError {

        @Test
        @DisplayName("should not support parameter without @CurrentUserId annotation")
        void shouldNotSupportParameterWithoutCurrentUserIdAnnotation() throws NoSuchMethodException {
            assertThat(resolver.supportsParameter(parameterAt(1))).isFalse();
        }

        @Test
        @DisplayName("should not support parameter annotated with @CurrentUserId but not of type UUID")
        void shouldNotSupportParameterAnnotatedWithCurrentUserIdButNotOfTypeUuid() throws NoSuchMethodException {
            assertThat(resolver.supportsParameter(parameterAt(2))).isFalse();
        }

        @Test
        @DisplayName("should throw MissingCurrentUserException when there is no authentication")
        void shouldThrowMissingCurrentUserExceptionWhenThereIsNoAuthentication() {
            assertThatThrownBy(() -> resolver.resolveArgument(null, null, null, null))
                    .isInstanceOf(MissingCurrentUserException.class)
                    .hasMessage("No authenticated user found in security context");
        }

        @Test
        @DisplayName("should throw MissingCurrentUserException when principal is not a UUID")
        void shouldThrowMissingCurrentUserExceptionWhenPrincipalIsNotAUuid() {
            var authentication = new UsernamePasswordAuthenticationToken("not-a-uuid", null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            assertThatThrownBy(() -> resolver.resolveArgument(null, null, null, null))
                    .isInstanceOf(MissingCurrentUserException.class)
                    .hasMessage("No authenticated user found in security context");
        }
    }
}