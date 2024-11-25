package edu.kit.dopler.transformation.exceptions;

/**
 * This exception will be thrown, when a feature that should exist, could not be found.
 */
public class FeatureNotPresentException extends RuntimeException {

    private final String shouldBeFoundBy;

    /**
     * Constructor of {@link FeatureNotPresentException}
     *
     * @param shouldBeFoundBy The string with which the feature was searched.
     */
    public FeatureNotPresentException(String shouldBeFoundBy) {
        this.shouldBeFoundBy = shouldBeFoundBy;
    }

    @Override
    public String getMessage() {
        return "Decision could not be found. Searched by: " + shouldBeFoundBy;
    }
}
