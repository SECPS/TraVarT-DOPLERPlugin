package edu.kit.dopler.transformation.exceptions;

import java.util.Objects;

/**
 * This exception is thrown when an object has an unexpected type.
 */
public class UnexpectedTypeException extends RuntimeException {

    private final Object object;

    /**
     * Constructor of {@link UnexpectedTypeException}
     *
     * @param object Object that had an unexpected type
     */
    public UnexpectedTypeException(Object object) {
        Objects.requireNonNull(object);
        this.object = object;
    }

    @Override
    public String getMessage() {
        return "Unexpected type. Class: '%s', Value: '%s'".formatted(object.getClass().getSimpleName(),
                object.toString());
    }
}
