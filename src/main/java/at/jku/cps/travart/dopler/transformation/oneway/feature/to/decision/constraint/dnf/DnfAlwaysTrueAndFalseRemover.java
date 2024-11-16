package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import at.jku.cps.travart.dopler.transformation.util.MyUtil;
import at.jku.cps.travart.dopler.transformation.util.UnexpectedTypeException;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DnfAlwaysTrueAndFalseRemover {

    /**
     * Removes conjunctions from the DNF that are always false and removes Literals from conkunctions that are always
     * true.
     */
    public List<List<Constraint>> removeAlwaysTruOrFalseConstraints(FeatureModel featureModel,
                                                                    List<List<Constraint>> dnf) {
        List<List<Constraint>> newDnf = new ArrayList<>();

        //Create copy of DNF but remove unnecessary literals and conjunctions
        for (List<Constraint> conjunction : dnf) {
            List<Constraint> newConjunction = createNewConjunction(featureModel, conjunction);
            if (!newConjunction.isEmpty()) {
                newDnf.add(newConjunction);
            }
        }

        return newDnf;
    }

    private List<Constraint> createNewConjunction(FeatureModel featureModel, List<Constraint> conjunction) {
        List<Constraint> newConjunction = new ArrayList<>();

        for (Constraint constraint : conjunction) {
            Optional<LiteralConstraint> parent = getNonMandatoryParent(featureModel, constraint);

            if (constraint instanceof NotConstraint) {
                //If parent was not found, then conjunction is always false and can be removed from DNF
                if (parent.isEmpty()) {
                    return new ArrayList<>();
                } else {
                    newConjunction.add(new NotConstraint(parent.get()));
                }
            } else if (constraint instanceof LiteralConstraint) {
                //If parent was not found, constraint is always true and does not need to be added
                parent.ifPresent(newConjunction::add);
            } else {
                throw new UnexpectedTypeException(constraint);
            }
        }

        return newConjunction;
    }

    private Optional<LiteralConstraint> getNonMandatoryParent(FeatureModel featureModel, Constraint constraint) {
        return switch (constraint) {
            case NotConstraint notConstraint when notConstraint.getContent() instanceof LiteralConstraint literalConstraint ->
                    MyUtil.findFirstNonMandatoryParent(featureModel, literalConstraint);
            case LiteralConstraint literalConstraint ->
                    MyUtil.findFirstNonMandatoryParent(featureModel, literalConstraint);
            case null, default -> throw new UnexpectedTypeException(constraint);
        };
    }
}
