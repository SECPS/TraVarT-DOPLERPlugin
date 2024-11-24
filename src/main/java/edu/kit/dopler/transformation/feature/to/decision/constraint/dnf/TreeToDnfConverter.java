package edu.kit.dopler.transformation.feature.to.decision.constraint.dnf;

import de.vill.model.constraint.Constraint;

import java.util.List;

/** Converts a {@link Constraint} into DNF */
public interface TreeToDnfConverter {

    /**
     * Converts the given {@link Constraint} into DNF.
     *
     * @param constraint {@link Constraint} to convert
     *
     * @return DNF. The inner lists are conjunctions. The outer list is the disjunction.
     */
    List<List<Constraint>> convertToDnf(Constraint constraint);
}
