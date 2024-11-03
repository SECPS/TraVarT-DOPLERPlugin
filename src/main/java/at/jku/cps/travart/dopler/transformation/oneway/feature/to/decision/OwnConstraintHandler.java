package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import de.vill.model.Feature;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ImplicationConstraint;
import de.vill.model.constraint.LiteralConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adds the constraints from the feature model to the decision model.
 */
class OwnConstraintHandler {

    /**
     * Temporary variable to save current decision model
     */
    private IDecisionModel decisionModel = null;

    final void handleOwnConstraints(List<Constraint> ownConstraints, IDecisionModel decisionModel) {
        this.decisionModel = decisionModel;

        List<Constraint> simplifiedConstraints = new ArrayList<>();
        for (Constraint constraint : ownConstraints) {
            simplifiedConstraints.addAll(simplifyConstraint(constraint));
        }

        for (Constraint constraint : simplifiedConstraints) {

            if (constraint instanceof ImplicationConstraint) {
                //handleImplicationConstraint((ImplicationConstraint) constraint);
            }
        }
    }

    /** Decomposes the given (sometimes complex) constraint into several simpler ones. */
    private List<Constraint> simplifyConstraint(Constraint constraint) {
        throw new RuntimeException("Not implemented");
    }

    private void handleImplicationConstraint(ImplicationConstraint ownConstraint) {
        LiteralConstraint left = (LiteralConstraint) ownConstraint.getLeft();
        LiteralConstraint right = (LiteralConstraint) ownConstraint.getRight();

        Feature leftFeature = left.getFeature();
        Feature rightFeature = right.getFeature();

        //Find decision depending on left feature
        Optional<IDecision<?>> first = decisionModel.getDecisions().stream()
                .filter(iDecision -> iDecision.getId().equals(leftFeature.getParentFeature().getFeatureName()))
                .findFirst();

        System.out.println();
    }
}
