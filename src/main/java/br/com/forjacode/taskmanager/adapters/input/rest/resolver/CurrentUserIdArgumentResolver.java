package br.com.forjacode.taskmanager.adapters.input.rest.resolver;

import br.com.forjacode.taskmanager.adapters.input.rest.annotation.CurrentUserId;
import br.com.forjacode.taskmanager.domain.exception.MissingCurrentUserException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

@NullMarked
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class) &&
                parameter.getParameterType().equals(UUID.class);
    }

    @Override
    public @Nullable Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

        String header = webRequest.getHeader("X-User-Id");

        if (header == null || header.isBlank()) {
            throw new MissingCurrentUserException("Missing or blank X-User-Id header");
        }

        try {
            return UUID.fromString(header);
        } catch (IllegalArgumentException e) {
            throw new MethodArgumentTypeMismatchException(
                    header, UUID.class, "X-User-Id", parameter, e);
        }

    }
}
