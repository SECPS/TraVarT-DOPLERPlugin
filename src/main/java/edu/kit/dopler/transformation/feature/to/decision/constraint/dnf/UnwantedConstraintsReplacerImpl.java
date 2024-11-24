package edu.kit.dopler.transformation.feature.to.decision.constraint.dnf;

import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;
import de.vill.model.constraint.*;

/**
 * Implementation of {@link UnwantedConstraintsReplacer} Replaces unwanted constraints like "=>", "<=>" and "()" and
 * replaces them with different ones.
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
        //A => B -> !A | B
        //A <=> B -> A => B & B => A
        //(A) -> A
        return switch (constraint) {
            case ImplicationConstraint implicationConstraint ->
                    new OrConstraint(new NotConstraint(implicationConstraint.getLeft()),
                            implicationConstraint.getRight());
            case EquivalenceConstraint equivalenceConstraint -> new AndConstraint(
                    new ImplicationConstraint(equivalenceConstraint.getLeft(), equivalenceConstraint.getRight()),
                    new ImplicationConstraint(equivalenceConstraint.getRight(), equivalenceConstraint.getLeft()));
            case ParenthesisConstraint parenthesisConstraint -> convertIfNeeded(parenthesisConstraint.getContent());
            case AndConstraint andConstraint -> constraint;
            case OrConstraint orConstraint -> constraint;
            case NotConstraint notConstraint -> constraint;
            case LiteralConstraint literalConstraint -> constraint;
            case ExpressionConstraint expressionConstraint -> constraint;
            case null, default -> throw new UnexpectedTypeException(constraint);
        };
    }
}
