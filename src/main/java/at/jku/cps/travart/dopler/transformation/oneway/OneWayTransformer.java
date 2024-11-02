package at.jku.cps.travart.dopler.transformation.oneway;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.impl.DecisionModel;
import de.vill.model.FeatureModel;

public class OneWayTransformer implements IModelTransformer<IDecisionModel> {

    @Override
    public FeatureModel transform(IDecisionModel model, String modelName, STRATEGY level) throws NotSupportedVariabilityTypeException {
        return new FeatureModel();
    }

    @Override
    public IDecisionModel transform(FeatureModel model, String modelName, STRATEGY level) throws NotSupportedVariabilityTypeException {
        DecisionModel decisionModel = new DecisionModel(modelName);


        return decisionModel;
    }
}
