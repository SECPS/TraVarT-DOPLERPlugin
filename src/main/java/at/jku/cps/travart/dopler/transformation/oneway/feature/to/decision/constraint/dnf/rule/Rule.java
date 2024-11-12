package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf.rule;

import de.vill.model.constraint.Constraint;

import java.util.Optional;

/**
 * Interface for a single rule for the DNF algorithm.
 */
@FunctionalInterface
public interface Rule {

    Optional<Constraint> replace(Constraint constraint);
}
