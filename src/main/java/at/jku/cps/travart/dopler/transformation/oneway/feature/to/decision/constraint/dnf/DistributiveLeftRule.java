package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.OrConstraint;

import java.util.Optional;

//((A | B) & C) ~> ((A & C) | (B & C))
public class DistributiveLeftRule implements Rule {

    @Override
    public Optional<Constraint> replace(Constraint constraint) {

        if (constraint instanceof AndConstraint) {
            AndConstraint andConstraint = (AndConstraint) constraint;
            if (andConstraint.getLeft() instanceof OrConstraint) {
                OrConstraint orConstraint = (OrConstraint) andConstraint.getLeft();
                Constraint a = orConstraint.getLeft();
                Constraint b = orConstraint.getRight();
                Constraint c = andConstraint.getRight();
                return Optional.of(new OrConstraint(new AndConstraint(a, c), new AndConstraint(b, c)));
            }
        }

        return Optional.empty();
    }
}
