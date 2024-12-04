package edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.rule;

import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.NotConstraint;
import de.vill.model.constraint.OrConstraint;

import java.util.Optional;

/** !(A | B) ~> (!A & !B) */
public class MorgenOrDnfRule implements DnfRule {

    @Override
    public Optional<Constraint> replace(Constraint constraint) {
        if (constraint instanceof NotConstraint notConstraint) {
            Constraint content = notConstraint.getContent();
            if (content instanceof OrConstraint orConstraint) {
                return Optional.of(new AndConstraint(new NotConstraint(orConstraint.getLeft()),
                        new NotConstraint(orConstraint.getRight())));
            }
        }
        return Optional.empty();
    }
}
