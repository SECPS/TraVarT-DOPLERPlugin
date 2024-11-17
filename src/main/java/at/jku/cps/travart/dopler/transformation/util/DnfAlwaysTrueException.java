package at.jku.cps.travart.dopler.transformation.util;

import de.vill.model.constraint.Constraint;

import java.util.List;

public class DnfAlwaysTrueException extends Exception {

    private final List<List<Constraint>> dnf;

    public DnfAlwaysTrueException(List<List<Constraint>> dnf) {
        this.dnf = dnf;
    }

    @Override
    public String getMessage() {
        return "Constraint is always true. DNF: " + dnf;
    }
}

