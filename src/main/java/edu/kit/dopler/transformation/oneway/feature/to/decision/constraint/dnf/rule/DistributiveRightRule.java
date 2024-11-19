package edu.kit.dopler.transformation.oneway.feature.to.decision.constraint.dnf.rule;

import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.OrConstraint;

import java.util.Optional;

/** (A & (B | C)) ~> ((A & B) | (A & C)) */
public class DistributiveRightRule implements Rule {

    @Override
    public Optional<Constraint> replace(Constraint constraint) {
        if (constraint instanceof AndConstraint) {
            AndConstraint andConstraint = (AndConstraint) constraint;
            if (andConstraint.getRight() instanceof OrConstraint) {
                OrConstraint orConstraint = (OrConstraint) andConstraint.getRight();
                Constraint aConstraint = andConstraint.getLeft();
                Constraint bConstraint = orConstraint.getLeft();
                Constraint cConstraint = orConstraint.getRight();
                return Optional.of(new OrConstraint(new AndConstraint(aConstraint, bConstraint),
                        new AndConstraint(aConstraint, cConstraint)));
            }
        }
        return Optional.empty();
    }
}
