package edu.kit.dopler.transformation.exceptions;

/** This exception is thrown when a construct could not be translated. */
public class CanNotBeTranslatedException extends RuntimeException {

    private final Object object;

    /**
     * Constructor of {@link CanNotBeTranslatedException}.
     *
     * @param object Construct that could not be translated.
     */
    public CanNotBeTranslatedException(Object object) {
        this.object = object;
    }

    @Override
    public String getMessage() {
        return "This object could not be translated: " + object.toString();
    }
}
