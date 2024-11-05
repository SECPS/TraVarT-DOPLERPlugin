package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.impl.DecisionModel;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;

public class FmToDmOneWayTransformer {

    private final FeatureAndGroupHandler featureAndGroupHandler;
    private final ConstraintHandler constraintHandler;

    public FmToDmOneWayTransformer() {
        constraintHandler = new ConstraintHandler();
        featureAndGroupHandler = new FeatureAndGroupHandler(new VisibilityHandler(), new IdHandler());
    }

    public final IDecisionModel transform(FeatureModel featureModel, String modelName, IModelTransformer.STRATEGY level)
            throws NotSupportedVariabilityTypeException {
        IDecisionModel decisionModel = new DecisionModel("", modelName);

        Feature rootFeature = featureModel.getRootFeature();
        featureAndGroupHandler.handleFeature(rootFeature, decisionModel, featureModel);

        constraintHandler.handleOwnConstraints(featureModel, decisionModel);

        return decisionModel;
    }
}
