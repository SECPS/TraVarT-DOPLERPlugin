package edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.rule;

import de.vill.model.constraint.Constraint;

import java.util.Optional;

/**
 * Interface for a single rule for the DNF algorithm.
 */
public interface DnfRule {

    /**
     * Check if the {@link DnfRule} matches the given {@link Constraint}. If it's matching, then return a new
     * {@link Constraint} that replaces the old one. Both are semantically equal.
     *
     * @param constraint {@link Constraint} to replace (if {@link DnfRule} matches)
     *
     * @return New {@link Constraint} that replaces the old one
     */
    Optional<Constraint> replace(Constraint constraint);
}
