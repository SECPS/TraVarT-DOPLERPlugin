package edu.kit.dopler.transformation.exceptions;

/** Exception that should be thrown, when a decision that should exist in the model, could not be found by its name. */
public class DecisionNotPresentException extends RuntimeException {

    private final String shouldBeFoundBy;

    /**
     * Constructor of {@link DecisionNotPresentException}.
     *
     * @param shouldBeFoundBy The string with which the decision was searched.
     */
    public DecisionNotPresentException(String shouldBeFoundBy) {
        this.shouldBeFoundBy = shouldBeFoundBy;
    }

    @Override
    public String getMessage() {
        return "Decision could not be found. Searched by: " + shouldBeFoundBy;
    }
}
