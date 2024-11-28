package edu.kit.dopler.transformation.feature.to.decision;

import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;

public interface AttributeHandler {

    void handleAttributes(Dopler decisionModel, FeatureModel featureModel);
}
