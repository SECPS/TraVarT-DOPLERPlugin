package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import de.vill.model.constraint.Constraint;

import java.util.List;

public interface DnfConverter {

    List<List<Constraint>> convertToDnf(Constraint constraint);
}
