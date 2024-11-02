package at.jku.cps.travart.dopler.transformation.oneway.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import de.vill.model.FeatureModel;

public class DmToFmOneWayTransformer {

    public FeatureModel transform(IDecisionModel model, String modelName, IModelTransformer.STRATEGY level)
            throws NotSupportedVariabilityTypeException {
        throw new RuntimeException("Not implemented");
    }
}
