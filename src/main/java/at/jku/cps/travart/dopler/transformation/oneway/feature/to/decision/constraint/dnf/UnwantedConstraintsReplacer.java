package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import de.vill.model.constraint.Constraint;

/** Replaces unwanted constraints and replaces them with different ones. */
public interface UnwantedConstraintsReplacer {

    /**
     * Replaces unwanted constraints by semantically equal ones.
     *
     * @param constraint Constraint in which unwanted constraints should be replaced
     *
     * @return Copy of given constraint with the replaced constraints.
     */
    Constraint replaceUnwantedConstraints(Constraint constraint);
}
