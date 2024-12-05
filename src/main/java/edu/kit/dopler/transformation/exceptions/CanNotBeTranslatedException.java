package edu.kit.dopler.transformation.exceptions;

public class CanNotBeTranslatedException extends Exception {

    private final Object object;

    public CanNotBeTranslatedException(Object object) {
        this.object = object;
    }

    @Override
    public String getMessage() {
        return "This object could not be translated: " + object.toString();
    }
}
