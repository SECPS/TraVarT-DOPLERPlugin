package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import at.jku.cps.travart.dopler.transformation.util.UnexpectedTypeException;
import de.vill.model.constraint.*;

import java.util.function.Supplier;

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
        if (constraint instanceof ImplicationConstraint) {
            ImplicationConstraint implicationConstraint = (ImplicationConstraint) constraint;
            convertBiConstraint(implicationConstraint, implicationConstraint::getLeft, implicationConstraint::getRight);
        } else if (constraint instanceof EquivalenceConstraint) {
            EquivalenceConstraint equivalenceConstraint = (EquivalenceConstraint) constraint;
            convertBiConstraint(equivalenceConstraint, equivalenceConstraint::getLeft, equivalenceConstraint::getRight);
        } else if (constraint instanceof AndConstraint) {
            AndConstraint andConstraint = (AndConstraint) constraint;
            convertBiConstraint(andConstraint, andConstraint::getLeft, andConstraint::getRight);
        } else if (constraint instanceof OrConstraint) {
            OrConstraint orConstraint = (OrConstraint) constraint;
            convertBiConstraint(orConstraint, orConstraint::getLeft, orConstraint::getRight);
        } else if (constraint instanceof ParenthesisConstraint) {
            ParenthesisConstraint parenthesisConstraint = (ParenthesisConstraint) constraint;
            convertMonoConstraint(parenthesisConstraint, parenthesisConstraint::getContent);
        } else if (constraint instanceof NotConstraint) {
            NotConstraint notConstraint = (NotConstraint) constraint;
            convertMonoConstraint(notConstraint, notConstraint::getContent);
        } else if (constraint instanceof LiteralConstraint || constraint instanceof ExpressionConstraint) {
            //Do nothing
        } else {
            throw new UnexpectedTypeException(constraint);
        }
    }

    //Convert single child of given constraint
    private void convertMonoConstraint(Constraint monoConstraint, Supplier<Constraint> contentSupplier) {
        Constraint oldChild = contentSupplier.get();
        Constraint newChild = convertIfNeeded(oldChild);
        monoConstraint.replaceConstraintSubPart(oldChild, newChild);
        convertRecursive(newChild);
    }

    //Convert both children of given constraint
    private void convertBiConstraint(Constraint biConstraint, Supplier<Constraint> leftSupplier,
                                     Supplier<Constraint> rightSupplier) {
        Constraint oldLeft = leftSupplier.get();
        Constraint newLeft = convertIfNeeded(oldLeft);
        Constraint oldRight = rightSupplier.get();
        Constraint newRight = convertIfNeeded(oldRight);
        biConstraint.replaceConstraintSubPart(oldLeft, newLeft);
        biConstraint.replaceConstraintSubPart(oldRight, newRight);
        convertRecursive(newLeft);
        convertRecursive(newRight);
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
