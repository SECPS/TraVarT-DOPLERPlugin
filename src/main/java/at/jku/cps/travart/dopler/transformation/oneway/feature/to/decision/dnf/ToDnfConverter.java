package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.dnf;

import de.vill.model.constraint.*;

import java.util.List;
import java.util.Optional;

public class ToDnfConverter {

    private final UnwantedConstraintsReplacer replacer;

    private final List<Rule> rules =
            List.of(new NotNotRule(), new MorgenOrRule(), new MorgenAndRule(), new DistributiveLeftRule(),
                    new DistributiveRightRule());

    ToDnfConverter() {
        replacer = new UnwantedConstraintsReplacer();
    }

    public Constraint convertToDnf(Constraint constraint) {
        Constraint sanitisedConstraint = replacer.replaceUnwantedConstraints(constraint);
        ParenthesisConstraint parenthesisConstraint = new ParenthesisConstraint(sanitisedConstraint);

        doSomething(parenthesisConstraint);

        return parenthesisConstraint.getContent();
    }

    /** Find first matching rule */
    private Constraint checkForRules(Constraint constraint) {
        for (Rule rule : rules) {
            Optional<Constraint> optional = rule.replace(constraint);
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        return constraint;
    }

    private void doSomething(Constraint constraint) {
        if (constraint instanceof AndConstraint) {
            AndConstraint andConstraint = (AndConstraint) constraint;

            Constraint oldLeft = andConstraint.getLeft();
            Constraint oldRight = andConstraint.getRight();

            Constraint newLeft = checkForRules(oldLeft);
            Constraint newRight = checkForRules(oldRight);

            andConstraint.replaceConstraintSubPart(oldLeft, newLeft);
            andConstraint.replaceConstraintSubPart(oldRight, newRight);

            doSomething(newLeft);
            doSomething(newRight);
        } else if (constraint instanceof OrConstraint) {
            OrConstraint orConstraint = (OrConstraint) constraint;

            Constraint oldLeft = orConstraint.getLeft();
            Constraint oldRight = orConstraint.getRight();

            Constraint newLeft = checkForRules(oldLeft);
            Constraint newRight = checkForRules(oldRight);

            orConstraint.replaceConstraintSubPart(oldLeft, newLeft);
            orConstraint.replaceConstraintSubPart(oldRight, newRight);

            doSomething(newLeft);
            doSomething(newRight);
        } else if (constraint instanceof NotConstraint) {
            NotConstraint notConstraint = (NotConstraint) constraint;
            Constraint oldContent = notConstraint.getContent();
            Constraint newContent = checkForRules(oldContent);
            notConstraint.replaceConstraintSubPart(oldContent, newContent);
            doSomething(newContent);
        } else if (constraint instanceof ParenthesisConstraint) {
            ParenthesisConstraint parenthesisConstraint = (ParenthesisConstraint) constraint;
            Constraint oldContent = parenthesisConstraint.getContent();
            Constraint newContent = checkForRules(oldContent);
            parenthesisConstraint.replaceConstraintSubPart(oldContent, newContent);
            doSomething(newContent);
        } else if (constraint instanceof LiteralConstraint) {
            //Do nothing
        } else if (constraint instanceof ExpressionConstraint) {
            //Do nothing
        } else {
            throw new RuntimeException("Unexpected constraint");
        }
    }
}
