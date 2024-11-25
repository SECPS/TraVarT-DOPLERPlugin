package edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.rule;

import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.OrConstraint;

import java.util.Optional;

/** (A & (B | C)) ~> ((A & B) | (A & C)) */
public class DistributiveRightRule implements Rule {

    @Override
    public Optional<Constraint> replace(Constraint constraint) {
        if (constraint instanceof AndConstraint andConstraint) {
            if (andConstraint.getRight() instanceof OrConstraint orConstraint) {
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
