package edu.kit.dopler.transformation.exceptions;

/**
 * This exception is thrown when a model is invalid.
 */
public class ModelInvalidException extends RuntimeException {

    /**
     * Constructor of {@link ModelInvalidException}.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param e       the cause (which is saved for later retrieval by the getCause() method). (A null value is
     *                permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public ModelInvalidException(String message, DnfAlwaysFalseException e) {
        super(message, e);
    }
}
