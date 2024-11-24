package edu.kit.dopler.transformation.feature.to.decision;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.feature.to.decision.constraint.ConstraintHandler;
import edu.kit.dopler.transformation.feature.to.decision.constraint.ConstraintHandlerImpl;

public class FmToDmOneWayTransformer {

    private final FeatureAndGroupHandler featureAndGroupHandler;
    private final ConstraintHandler constraintHandler;

    public FmToDmOneWayTransformer() {
        constraintHandler = new ConstraintHandlerImpl();
        featureAndGroupHandler = new FeatureAndGroupHandler(new VisibilityHandler(), new IdHandler());
    }

    public Dopler transform(FeatureModel featureModel, String modelName) {
        Dopler decisionModel = new Dopler();
        decisionModel.setName(modelName);

        Feature rootFeature = featureModel.getRootFeature();
        featureAndGroupHandler.handleFeature(rootFeature, decisionModel, featureModel);

        constraintHandler.handleOwnConstraints(featureModel, decisionModel);

        return decisionModel;
    }
}
