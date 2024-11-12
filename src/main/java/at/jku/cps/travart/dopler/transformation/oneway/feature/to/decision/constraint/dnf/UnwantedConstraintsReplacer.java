package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import de.vill.model.constraint.*;

public class UnwantedConstraintsReplacer {

    public Constraint replaceUnwantedConstraints(Constraint constraint) {
        ParenthesisConstraint parenthesisConstraint = new ParenthesisConstraint(constraint);
        replace(parenthesisConstraint);
        return parenthesisConstraint.getContent();
    }

    private void replace(Constraint constraint) {
        if (constraint instanceof ImplicationConstraint) {
            ImplicationConstraint implicationConstraint = (ImplicationConstraint) constraint;
            Constraint oldLeft = implicationConstraint.getLeft();
            Constraint newLeft = replaceChild(oldLeft);
            Constraint oldRight = implicationConstraint.getRight();
            Constraint newRight = replaceChild(oldRight);
            implicationConstraint.replaceConstraintSubPart(oldLeft, newLeft);
            implicationConstraint.replaceConstraintSubPart(oldRight, newRight);
            replace(newLeft);
            replace(newRight);
        } else if (constraint instanceof EquivalenceConstraint) {
            EquivalenceConstraint equivalenceConstraint = (EquivalenceConstraint) constraint;
            Constraint oldLeft = equivalenceConstraint.getLeft();
            Constraint newLeft = replaceChild(oldLeft);
            Constraint oldRight = equivalenceConstraint.getRight();
            Constraint newRight = replaceChild(oldRight);
            equivalenceConstraint.replaceConstraintSubPart(oldLeft, newLeft);
            equivalenceConstraint.replaceConstraintSubPart(oldRight, newRight);
            replace(newLeft);
            replace(newRight);
        } else if (constraint instanceof ParenthesisConstraint) {
            ParenthesisConstraint parenthesisConstraint = (ParenthesisConstraint) constraint;
            Constraint oldChild = parenthesisConstraint.getContent();
            Constraint newChild = replaceChild(oldChild);
            parenthesisConstraint.replaceConstraintSubPart(oldChild, newChild);
            replace(newChild);
        } else if (constraint instanceof AndConstraint) {
            AndConstraint andConstraint = (AndConstraint) constraint;
            Constraint oldLeft = andConstraint.getLeft();
            Constraint newLeft = replaceChild(oldLeft);
            Constraint oldRight = andConstraint.getRight();
            Constraint newRight = replaceChild(oldRight);
            andConstraint.replaceConstraintSubPart(oldLeft, newLeft);
            andConstraint.replaceConstraintSubPart(oldRight, newRight);
            replace(newLeft);
            replace(newRight);
        } else if (constraint instanceof OrConstraint) {
            OrConstraint orConstraint = (OrConstraint) constraint;
            Constraint oldLeft = orConstraint.getLeft();
            Constraint newLeft = replaceChild(oldLeft);
            Constraint oldRight = orConstraint.getRight();
            Constraint newRight = replaceChild(oldRight);
            orConstraint.replaceConstraintSubPart(oldLeft, newLeft);
            orConstraint.replaceConstraintSubPart(oldRight, newRight);
            replace(newLeft);
            replace(newRight);
        } else if (constraint instanceof NotConstraint) {
            NotConstraint notConstraint = (NotConstraint) constraint;
            Constraint oldChild = notConstraint.getContent();
            Constraint newChild = replaceChild(oldChild);
            notConstraint.replaceConstraintSubPart(oldChild, newChild);
            replace(newChild);
        } else if (constraint instanceof LiteralConstraint) {
            //Do nothing
        } else if (constraint instanceof ExpressionConstraint) {
            //Do nothing
        } else {
            throw new RuntimeException("Unexpected constraint");
        }
    }

    private Constraint replaceChild(Constraint constraint) {
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
            return replaceChild(((ParenthesisConstraint) constraint).getContent());
        } else if (constraint instanceof AndConstraint) {
            //Do nothing
            return constraint;
        } else if (constraint instanceof OrConstraint) {
            //Do nothing
            return constraint;
        } else if (constraint instanceof NotConstraint) {
            //Do nothing
            return constraint;
        } else if (constraint instanceof LiteralConstraint) {
            //Do nothing
            return constraint;
        } else if (constraint instanceof ExpressionConstraint) {
            //Do nothing
            return constraint;
        } else {
            throw new RuntimeException("Unexpected constraint");
        }
    }
}
