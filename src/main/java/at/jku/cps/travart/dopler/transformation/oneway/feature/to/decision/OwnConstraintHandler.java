package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import de.vill.model.Feature;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ImplicationConstraint;
import de.vill.model.constraint.LiteralConstraint;

import java.util.List;
import java.util.Optional;

/**
 * Adds the constraints from the feature model to the decision model.
 */
public class OwnConstraintHandler {

    /**
     * Temporary variable to save current decision model
     */
    private IDecisionModel decisionModel;

    public void handleOwnConstraints(List<Constraint> ownConstraints, IDecisionModel decisionModel) {
        this.decisionModel = decisionModel;

        for (Constraint ownConstraint : ownConstraints) {

            if (ownConstraint instanceof ImplicationConstraint) {
                //handleImplicationConstraint((ImplicationConstraint) ownConstraint);
            }
        }
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
