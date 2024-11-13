package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.impl.DecisionModel;
import at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.ConstraintHandler;
import at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.ConstraintHandlerImpl;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;

public class FmToDmOneWayTransformer {

    private final FeatureAndGroupHandler featureAndGroupHandler;
    private final ConstraintHandler constraintHandler;

    public FmToDmOneWayTransformer() {
        constraintHandler = new ConstraintHandlerImpl();
        featureAndGroupHandler = new FeatureAndGroupHandler(new VisibilityHandler(), new IdHandler());
    }

    public IDecisionModel transform(FeatureModel featureModel, String modelName) {
        IDecisionModel decisionModel = new DecisionModel("", modelName);

        Feature rootFeature = featureModel.getRootFeature();
        featureAndGroupHandler.handleFeature(rootFeature, decisionModel, featureModel);

        constraintHandler.handleOwnConstraints(featureModel, decisionModel);

        return decisionModel;
    }
}
