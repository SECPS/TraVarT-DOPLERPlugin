package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.IAction;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;

public interface ActionCreator {

    IAction createAction(IDecisionModel decisionModel, FeatureModel featureModel, Constraint right);
}
