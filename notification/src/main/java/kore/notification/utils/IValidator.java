package kore.notification.utils;

import org.springframework.stereotype.Component;

public interface IValidator<T> {
    void validate(T value);
}
