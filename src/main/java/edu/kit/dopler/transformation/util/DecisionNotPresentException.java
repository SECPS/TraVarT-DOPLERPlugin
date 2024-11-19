package edu.kit.dopler.transformation.util;

public class DecisionNotPresentException extends RuntimeException {

    private final String shouldBeFoundBy;

    public DecisionNotPresentException(String shouldBeFoundBy) {
        this.shouldBeFoundBy = shouldBeFoundBy;
    }

    @Override
    public String getMessage() {
        return "Decision could not be found. Searched by: " + shouldBeFoundBy;
    }
}
