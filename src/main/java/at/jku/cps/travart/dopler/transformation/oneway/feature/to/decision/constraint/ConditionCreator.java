package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;

public interface ConditionCreator {

    ICondition createCondition(IDecisionModel decisionModel, FeatureModel featureModel, Constraint left);
}
