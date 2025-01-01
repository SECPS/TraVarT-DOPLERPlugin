package edu.kit.dopler.transformation.exceptions;

import de.vill.model.constraint.Constraint;

import java.util.ArrayList;
import java.util.List;

/**
 * This exception is thrown when a DNF is always false.
 */
public class DnfAlwaysFalseException extends Exception {

    private final List<List<Constraint>> dnf;

    /**
     * Constructor of {@link DnfAlwaysFalseException}
     *
     * @param dnf DNF that is always false
     */
    public DnfAlwaysFalseException(List<List<Constraint>> dnf) {
        this.dnf = new ArrayList<>();
        for (List<Constraint> constraints : dnf) {
            this.dnf.add(new ArrayList<>(constraints));
        }
    }

    @Override
    public String getMessage() {
        return "Constraint is always false. DNF: " + dnf;
    }
}
