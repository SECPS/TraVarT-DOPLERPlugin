package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import at.jku.cps.travart.dopler.transformation.util.UnexpectedTypeException;
import de.vill.model.constraint.*;

/**
 * Implementation of {@link UnwantedConstraintsReplacer}
 */
public class UnwantedConstraintsReplacerImpl implements UnwantedConstraintsReplacer {

    @Override
    public Constraint replaceUnwantedConstraints(Constraint constraint) {
        ParenthesisConstraint parenthesisConstraint = new ParenthesisConstraint(constraint.clone());
        convertRecursive(parenthesisConstraint);
        return parenthesisConstraint.getContent();
    }

    private void convertRecursive(Constraint constraint) {
        for (Constraint oldChild : constraint.getConstraintSubParts()) {
            Constraint newChild = convertIfNeeded(oldChild);
            constraint.replaceConstraintSubPart(oldChild, newChild);
            convertRecursive(newChild);
        }
    }

    private Constraint convertIfNeeded(Constraint constraint) {
        if (constraint instanceof ImplicationConstraint) {
            //A => B -> !A | B
            ImplicationConstraint implicationConstraint = (ImplicationConstraint) constraint;
            return new OrConstraint(new NotConstraint(implicationConstraint.getLeft()),
                    implicationConstraint.getRight());
        } else if (constraint instanceof EquivalenceConstraint) {
            //A <=> B -> A => B & B => A
            EquivalenceConstraint equivalenceConstraint = (EquivalenceConstraint) constraint;
            return new AndConstraint(
                    new ImplicationConstraint(equivalenceConstraint.getLeft(), equivalenceConstraint.getRight()),
                    new ImplicationConstraint(equivalenceConstraint.getRight(), equivalenceConstraint.getLeft()));
        } else if (constraint instanceof ParenthesisConstraint) {
            //(A) -> A
            return convertIfNeeded(((ParenthesisConstraint) constraint).getContent());
        } else if ((((constraint instanceof AndConstraint || constraint instanceof OrConstraint) ||
                constraint instanceof NotConstraint) || constraint instanceof LiteralConstraint) ||
                constraint instanceof ExpressionConstraint) {
            //Do nothing
            return constraint;
        } else {
            throw new UnexpectedTypeException(constraint);
        }
    }
}
