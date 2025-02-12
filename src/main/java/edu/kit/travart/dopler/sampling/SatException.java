package edu.kit.travart.dopler.sampling;

import java.io.IOException;

public class SatException extends RuntimeException {

    public SatException(IOException ioException) {
        super(ioException);
    }

    public SatException() {
    }
}
