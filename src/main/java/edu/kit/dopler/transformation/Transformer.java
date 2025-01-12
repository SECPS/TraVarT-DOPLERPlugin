package edu.kit.dopler.transformation;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.decision.to.feature.DmToFmTransformer;
import edu.kit.dopler.transformation.feature.to.decision.FmToDmTransformer;

/**
 * Transforms {@link Dopler} models into {@link FeatureModel}s and vice versa.
 */
public class Transformer implements IModelTransformer<Dopler> {

    /**
     * This is the standard name for the root feature in the {@link FeatureModel}.
     */
    public static final String STANDARD_MODEL_NAME = "STANDARD_MODEL_NAME";

    private final DmToFmTransformer dmToFmTransformer;
    private final FmToDmTransformer fmToDmTransformer;

    /** Constructor of {@link Transformer} */
    public Transformer(DmToFmTransformer dmToFmTransformer, FmToDmTransformer fmToDmTransformer) {
        this.dmToFmTransformer = dmToFmTransformer;
        this.fmToDmTransformer = fmToDmTransformer;
    }

    @Override
    public FeatureModel transform(Dopler model, String s, STRATEGY strategy) {
        return dmToFmTransformer.transform(model, strategy);
    }

    @Override
    public Dopler transform(FeatureModel featureModel, String s, STRATEGY strategy) {
        return fmToDmTransformer.transform(featureModel, strategy);
    }
}
