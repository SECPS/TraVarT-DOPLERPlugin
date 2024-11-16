package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import at.jku.cps.travart.dopler.transformation.util.MyUtil;
import at.jku.cps.travart.dopler.transformation.util.UnexpectedTypeException;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;

import java.util.ArrayList;
import java.util.List;

public class DnfAlwaysTrueAndFalseRemover {

    /**
     * Removes conjunctions from the DNF that are always false and removes Literals from conkunctions that are always
     * true.
     */
    public List<List<Constraint>> removeAlwaysTruOrFalseConstraints(FeatureModel featureModel,
                                                                    List<List<Constraint>> dnf) {
        List<List<Constraint>> newDnf = new ArrayList<>();

        for (List<Constraint> conjunction : dnf) {
            List<Constraint> newConjunction = new ArrayList<>();
            newDnf.add(newConjunction);

            for (Constraint constraint : conjunction) {

                if (constraint instanceof NotConstraint notConstraint &&
                        notConstraint.getContent() instanceof LiteralConstraint literalConstraint) {
                    //If parent was not found, then conjunction is always false and can be removed
                    MyUtil.findFirstNonMandatoryParent(featureModel, literalConstraint)
                            .ifPresentOrElse(newLiteral -> newConjunction.add(new NotConstraint(newLiteral)),
                                    () -> newDnf.remove(newConjunction));
                } else if (constraint instanceof LiteralConstraint literalConstraint) {
                    //If parent was not found, constraint is always true and does not need to be added
                    MyUtil.findFirstNonMandatoryParent(featureModel, literalConstraint).ifPresent(newConjunction::add);
                } else {
                    throw new UnexpectedTypeException(constraint);
                }
            }

            if (newConjunction.isEmpty()) {
                newDnf.remove(newConjunction);
            }
        }

        return newDnf;
    }
}
