package edu.kit.dopler.transformation.exceptions;

import de.vill.model.constraint.Constraint;

import java.util.ArrayList;
import java.util.List;

/** This exception is thrown when a conjunction of a DNF is always true. */
public class ConjunctionAlwaysTrueException extends Exception {

    private final List<Constraint> conjunction;

    /**
     * Constructor of {@link ConjunctionAlwaysTrueException}
     *
     * @param conjunction Conjunction that is always true, represented as a List of {@link Constraint}s
     */
    public ConjunctionAlwaysTrueException(List<Constraint> conjunction) {
        this.conjunction = new ArrayList<>(conjunction);
    }

    @Override
    public String getMessage() {
        return "Conjunction is always true: " + conjunction;
    }
}
