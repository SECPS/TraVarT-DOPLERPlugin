package edu.kit.dopler.transformation;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.decision.to.feature.DmToFmTransformer;
import edu.kit.dopler.transformation.feature.to.decision.FmToDmTransformer;

/**
 * Transforms Dopler models into UVL models and vice versa.
 */
public class Transformer implements IModelTransformer<Dopler> {

    private final DmToFmTransformer dmToFmTransformer;
    private final FmToDmTransformer fmToDmTransformer;

    /** Constructor of {@link Transformer} */
    public Transformer() {
        dmToFmTransformer = new DmToFmTransformer();
        fmToDmTransformer = new FmToDmTransformer();
    }

    @Override
    public FeatureModel transform(Dopler model, String modelName, STRATEGY level) {
        return dmToFmTransformer.transform(model, modelName, level);
    }

    @Override
    public Dopler transform(FeatureModel model, String modelName, STRATEGY level) {
        return fmToDmTransformer.transform(model, modelName, level);
    }
}
