package edu.kit.travart.dopler.sampling;

/**
 * This exception is thrown when there is a problem with the sat solver.
 */
class SatException extends RuntimeException {

    /**
     * Constructor of {@link SatException}.
     *
     * @param exception Inner {@link Exception} that caused this {@link SatException}
     */
    SatException(Exception exception) {
        super(exception);
    }

    /**
     * Constructor of {@link SatException}.
     */
    SatException() {
    }
}
