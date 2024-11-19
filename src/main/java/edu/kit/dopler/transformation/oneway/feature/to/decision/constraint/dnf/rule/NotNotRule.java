package edu.kit.dopler.transformation.oneway.feature.to.decision.constraint.dnf.rule;

import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.NotConstraint;

import java.util.Optional;

/** !!A ~> A */
public class NotNotRule implements Rule {

    @Override
    public Optional<Constraint> replace(Constraint constraint) {
        if (constraint instanceof NotConstraint) {
            NotConstraint notConstraint = (NotConstraint) constraint;
            if (notConstraint.getContent() instanceof NotConstraint) {
                NotConstraint content = (NotConstraint) notConstraint.getContent();
                return Optional.ofNullable(content.getContent());
            }
        }
        return Optional.empty();
    }
}
