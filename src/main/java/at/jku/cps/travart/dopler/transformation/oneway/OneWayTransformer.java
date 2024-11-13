package at.jku.cps.travart.dopler.transformation.oneway;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.transformation.oneway.decision.to.feature.DmToFmOneWayTransformer;
import at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.FmToDmOneWayTransformer;
import de.vill.model.FeatureModel;

public class OneWayTransformer implements IModelTransformer<IDecisionModel> {

    private final DmToFmOneWayTransformer dmToFmOneWayTransformer;
    private final FmToDmOneWayTransformer fmToDmOneWayTransformer;

    public OneWayTransformer() {
        dmToFmOneWayTransformer = new DmToFmOneWayTransformer();
        fmToDmOneWayTransformer = new FmToDmOneWayTransformer();
    }

    @Override
    public FeatureModel transform(IDecisionModel model, String modelName, STRATEGY level) {
        return dmToFmOneWayTransformer.transform(model, modelName, level);
    }

    @Override
    public IDecisionModel transform(FeatureModel model, String modelName, STRATEGY level) {
        return fmToDmOneWayTransformer.transform(model, modelName);
    }
}
