package edu.kit.dopler.transformation.util;

import de.vill.model.constraint.Constraint;

import java.util.List;

public class DnfAlwaysFalseException extends Exception {

    private final List<List<Constraint>> dnf;

    public DnfAlwaysFalseException(List<List<Constraint>> dnf) {
        this.dnf = dnf;
    }

    @Override
    public String getMessage() {
        return "Constraint is always false. DNF: " + dnf;
    }
}
