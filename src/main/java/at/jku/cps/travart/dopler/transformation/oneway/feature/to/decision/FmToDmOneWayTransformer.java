package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;


import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.impl.DecisionModel;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;

public class FmToDmOneWayTransformer {

    private final GroupHandler groupHandler;
    private final FeatureHandler featureHandler;
    private final OwnConstraintHandler ownConstraintHandler;


    public FmToDmOneWayTransformer() {
        groupHandler = new GroupHandler();
        featureHandler = new FeatureHandler(groupHandler);
        groupHandler.setFeatureHandler(featureHandler);
        ownConstraintHandler = new OwnConstraintHandler();
    }

    public IDecisionModel transform(FeatureModel featureModel, String modelName, IModelTransformer.STRATEGY level)
            throws NotSupportedVariabilityTypeException {
        IDecisionModel decisionModel = new DecisionModel("", modelName);

        Feature rootFeature = featureModel.getRootFeature();
        featureHandler.handleFeature(rootFeature, decisionModel);

        ownConstraintHandler.handleOwnConstraints(featureModel.getOwnConstraints(), decisionModel);

        return decisionModel;
    }


}
