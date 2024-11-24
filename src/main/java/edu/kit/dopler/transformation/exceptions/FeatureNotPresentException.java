package edu.kit.dopler.transformation.exceptions;

public class FeatureNotPresentException extends RuntimeException {

    private final String shouldBeFoundBy;

    public FeatureNotPresentException(String shouldBeFoundBy) {
        this.shouldBeFoundBy = shouldBeFoundBy;
    }

    @Override
    public String getMessage() {
        return "Decision could not be found. Searched by: " + shouldBeFoundBy;
    }
}
