package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.impl.DecisionModel;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;

public class FmToDmOneWayTransformer {

    private final FeatureAndGroupHandler featureAndGroupHandler;
    private final OwnConstraintHandler ownConstraintHandler;

    public FmToDmOneWayTransformer() {
        ownConstraintHandler = new OwnConstraintHandler();
        featureAndGroupHandler = new FeatureAndGroupHandler();
    }

    public final IDecisionModel transform(FeatureModel featureModel, String modelName, IModelTransformer.STRATEGY level)
            throws NotSupportedVariabilityTypeException {
        IDecisionModel decisionModel = new DecisionModel("", modelName);

        Feature rootFeature = featureModel.getRootFeature();
        featureAndGroupHandler.handleFeature(rootFeature, decisionModel, featureModel);

        ownConstraintHandler.handleOwnConstraints(featureModel.getOwnConstraints(), decisionModel);

        return decisionModel;
    }
}
