package edu.kit.travart.dopler.sampling;

/**
 * This exception is thrown when there is a problem with the sat solver.
 */
class SatException extends RuntimeException {

    SatException(Exception exception) {
        super(exception);
    }

    SatException(String message) {
        super(message);
    }
}
