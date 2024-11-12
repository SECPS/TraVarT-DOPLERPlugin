package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf.rule.*;
import de.vill.model.constraint.*;

import java.util.List;
import java.util.Optional;

/** Implementation of DnfConverter */
public class DnfConverterImpl implements DnfConverter {

    private final UnwantedConstraintsReplacer replacer;
    private final DnfSimplifier dnfSimplifier;

    private static final List<Rule> RULES =
            List.of(new NotNotRule(), new MorgenOrRule(), new MorgenAndRule(), new DistributiveLeftRule(),
                    new DistributiveRightRule());

    public DnfConverterImpl() {
        replacer = new UnwantedConstraintsReplacerImpl();
        dnfSimplifier = new DnfSimplifierImpl();
    }

    @Override
    public List<List<Constraint>> convertToDnf(Constraint constraint) {
        Constraint clonedConstraint = constraint.clone();

        Constraint sanitisedConstraint = replacer.replaceUnwantedConstraints(clonedConstraint);
        ParenthesisConstraint parenthesisConstraint = new ParenthesisConstraint(sanitisedConstraint);

        //TODO replace 100
        for (int i = 0; i < 100; i++) {
            convertRecursive(parenthesisConstraint);
        }

        return dnfSimplifier.simplifyDnf(parenthesisConstraint.getContent());
    }

    /** Find first matching rule */
    private Constraint checkForRules(Constraint constraint) {
        for (Rule rule : RULES) {
            Optional<Constraint> optional = rule.replace(constraint);
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        return constraint;
    }

    private void convertRecursive(Constraint constraint) {
        if (constraint instanceof AndConstraint) {
            AndConstraint andConstraint = (AndConstraint) constraint;

            Constraint oldLeft = andConstraint.getLeft();
            Constraint oldRight = andConstraint.getRight();

            Constraint newLeft = checkForRules(oldLeft);
            Constraint newRight = checkForRules(oldRight);

            andConstraint.replaceConstraintSubPart(oldLeft, newLeft);
            andConstraint.replaceConstraintSubPart(oldRight, newRight);

            convertRecursive(newLeft);
            convertRecursive(newRight);
        } else if (constraint instanceof OrConstraint) {
            OrConstraint orConstraint = (OrConstraint) constraint;

            Constraint oldLeft = orConstraint.getLeft();
            Constraint oldRight = orConstraint.getRight();

            Constraint newLeft = checkForRules(oldLeft);
            Constraint newRight = checkForRules(oldRight);

            orConstraint.replaceConstraintSubPart(oldLeft, newLeft);
            orConstraint.replaceConstraintSubPart(oldRight, newRight);

            convertRecursive(newLeft);
            convertRecursive(newRight);
        } else if (constraint instanceof NotConstraint) {
            NotConstraint notConstraint = (NotConstraint) constraint;
            Constraint oldContent = notConstraint.getContent();
            Constraint newContent = checkForRules(oldContent);
            notConstraint.replaceConstraintSubPart(oldContent, newContent);
            convertRecursive(newContent);
        } else if (constraint instanceof ParenthesisConstraint) {
            ParenthesisConstraint parenthesisConstraint = (ParenthesisConstraint) constraint;
            Constraint oldContent = parenthesisConstraint.getContent();
            Constraint newContent = checkForRules(oldContent);
            parenthesisConstraint.replaceConstraintSubPart(oldContent, newContent);
            convertRecursive(newContent);
        } else if (constraint instanceof LiteralConstraint) {
            //Do nothing
        } else if (constraint instanceof ExpressionConstraint) {
            //Do nothing
        } else {
            throw new RuntimeException("Unexpected constraint: " + constraint.getClass());
        }
    }
}
