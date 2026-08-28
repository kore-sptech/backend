package kore.notification.utils;

import org.springframework.stereotype.Component;

@Component
public interface IValidator<T> {
    void validate(T value);
}
