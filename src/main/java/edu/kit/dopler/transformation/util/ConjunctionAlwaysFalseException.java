package edu.kit.dopler.transformation.util;

import de.vill.model.constraint.Constraint;

import java.util.List;

public class ConjunctionAlwaysFalseException extends Exception {

    private final List<Constraint> conjunction;

    public ConjunctionAlwaysFalseException(List<Constraint> conjunction) {
        this.conjunction = conjunction;
    }

    @Override
    public String getMessage() {
        return "Conjunction is always false: " + conjunction;
    }
}
