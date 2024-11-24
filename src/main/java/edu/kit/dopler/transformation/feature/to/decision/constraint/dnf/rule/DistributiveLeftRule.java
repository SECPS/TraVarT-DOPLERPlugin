package edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.rule;

import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.OrConstraint;

import java.util.Optional;

/** ((A | B) & C) ~> ((A & C) | (B & C)) */
public class DistributiveLeftRule implements Rule {

    @Override
    public Optional<Constraint> replace(Constraint constraint) {
        if (constraint instanceof AndConstraint) {
            AndConstraint andConstraint = (AndConstraint) constraint;
            if (andConstraint.getLeft() instanceof OrConstraint) {
                OrConstraint orConstraint = (OrConstraint) andConstraint.getLeft();
                Constraint aConstraint = orConstraint.getLeft();
                Constraint bConstraint = orConstraint.getRight();
                Constraint cConstraint = andConstraint.getRight();
                return Optional.of(new OrConstraint(new AndConstraint(aConstraint, cConstraint),
                        new AndConstraint(bConstraint, cConstraint)));
            }
        }
        return Optional.empty();
    }
}
