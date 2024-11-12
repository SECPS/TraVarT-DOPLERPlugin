package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import de.vill.model.constraint.Constraint;

import java.util.Optional;

public interface Rule {

    Optional<Constraint> replace(Constraint constraint);
}
