package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.NotConstraint;
import de.vill.model.constraint.OrConstraint;

import java.util.Optional;

public class MorgenAndRule implements Rule {

    @Override
    public Optional<Constraint> replace(Constraint constraint) {
        if (constraint instanceof NotConstraint) {
            NotConstraint notConstraint = (NotConstraint) constraint;
            Constraint content = notConstraint.getContent();
            if (content instanceof AndConstraint) {
                AndConstraint andConstraint = (AndConstraint) content;
                return Optional.of(new OrConstraint(new NotConstraint(andConstraint.getLeft()),
                        new NotConstraint(andConstraint.getRight())));
            }
        }

        return Optional.empty();
    }
}
