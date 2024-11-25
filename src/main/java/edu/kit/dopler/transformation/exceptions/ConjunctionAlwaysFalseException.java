package edu.kit.dopler.transformation.exceptions;

import de.vill.model.constraint.Constraint;

import java.util.ArrayList;
import java.util.List;

/** Exception that should be thrown, when a conjunction of a DNF is always false. */
public class ConjunctionAlwaysFalseException extends Exception {

    private final List<Constraint> conjunction;

    /**
     * Constructor of {@link ConjunctionAlwaysFalseException}
     *
     * @param conjunction Conjunction that is always false, represented as a List of {@link Constraint}s
     */
    public ConjunctionAlwaysFalseException(List<Constraint> conjunction) {
        this.conjunction = new ArrayList<>(conjunction);
    }

    @Override
    public String getMessage() {
        return "Conjunction is always false: " + conjunction;
    }
}
