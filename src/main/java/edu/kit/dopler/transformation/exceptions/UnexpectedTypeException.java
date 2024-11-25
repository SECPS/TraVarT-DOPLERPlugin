package edu.kit.dopler.transformation.exceptions;

/**
 * This exception will be thrown, if an object has an unexpected type.
 */
public class UnexpectedTypeException extends RuntimeException {

    private final Object object;

    /**
     * Constructor of {@link UnexpectedTypeException}
     *
     * @param object Object that had an unexpected type
     */
    public UnexpectedTypeException(Object object) {
        this.object = object;
    }

    @Override
    public String getMessage() {
        return "Unexpected type: " + object.getClass().getSimpleName();
    }
}
