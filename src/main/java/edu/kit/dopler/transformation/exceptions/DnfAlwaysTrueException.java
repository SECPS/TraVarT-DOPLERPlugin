package edu.kit.dopler.transformation.exceptions;

import de.vill.model.constraint.Constraint;

import java.util.ArrayList;
import java.util.List;

/**
 * This exception is thrown when a DNF is always true.
 */
public class DnfAlwaysTrueException extends Exception {

    private final List<List<Constraint>> dnf;

    /**
     * Constructor of {@link DnfAlwaysTrueException}
     *
     * @param conjunctionAlwaysTrueException Cause of this exception.
     * @param dnf                            DNF that is always true
     */
    public DnfAlwaysTrueException(ConjunctionAlwaysTrueException conjunctionAlwaysTrueException,
                                  List<List<Constraint>> dnf) {
        super(conjunctionAlwaysTrueException);
        this.dnf = new ArrayList<>();
        for (List<Constraint> constraints : dnf) {
            this.dnf.add(new ArrayList<>(constraints));
        }
    }

    @Override
    public String getMessage() {
        return "Constraint is always true. DNF: " + dnf;
    }
}

