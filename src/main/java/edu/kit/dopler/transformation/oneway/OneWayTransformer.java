package edu.kit.dopler.transformation.oneway;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.sampler.DefaultCoreModelSampler;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.oneway.decision.to.feature.DmToFmOneWayTransformer;
import edu.kit.dopler.transformation.oneway.feature.to.decision.FmToDmOneWayTransformer;

public class OneWayTransformer implements IModelTransformer<Dopler> {

    private final DmToFmOneWayTransformer dmToFmOneWayTransformer;
    private final FmToDmOneWayTransformer fmToDmOneWayTransformer;

    public OneWayTransformer() {
        dmToFmOneWayTransformer = new DmToFmOneWayTransformer();
        fmToDmOneWayTransformer = new FmToDmOneWayTransformer();
    }

    @Override
    public FeatureModel transform(Dopler model, String modelName, STRATEGY level) {
        return dmToFmOneWayTransformer.transform(model, modelName, level);
    }

    @Override
    public Dopler transform(FeatureModel model, String modelName, STRATEGY level) {

        DefaultCoreModelSampler defaultCoreModelSampler = new DefaultCoreModelSampler();

        //try {
        //    Set<Map<IConfigurable, Boolean>> maps = defaultCoreModelSampler.sampleValidConfigurations(model);
        //
        //    System.out.println(maps.size());
        //
        //} catch (NotSupportedVariabilityTypeException e) {
        //    throw new RuntimeException(e);
        //}

        return fmToDmOneWayTransformer.transform(model, modelName);
    }
}
