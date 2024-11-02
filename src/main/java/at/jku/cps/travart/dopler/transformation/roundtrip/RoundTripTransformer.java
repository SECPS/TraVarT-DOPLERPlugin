package at.jku.cps.travart.dopler.transformation.roundtrip;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import de.vill.model.FeatureModel;

public class RoundTripTransformer implements IModelTransformer<IDecisionModel> {

    @Override
    public FeatureModel transform(IDecisionModel model, String modelName, STRATEGY level) throws NotSupportedVariabilityTypeException {
        return null;
    }

    @Override
    public IDecisionModel transform(FeatureModel model, String modelName, STRATEGY level) throws NotSupportedVariabilityTypeException {
        return null;
    }
}
