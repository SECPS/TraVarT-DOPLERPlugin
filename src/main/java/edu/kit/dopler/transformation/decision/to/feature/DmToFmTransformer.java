package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;

/** This class is responsible for transforming a {@link Dopler} model into a {@link FeatureModel}. */
public interface DmToFmTransformer {

    /**
     * Transforms the given {@link FeatureModel} into a {@link Dopler} model.
     *
     * @param decisionModel {@link Dopler} model to transform
     * @param level         Strategy for the transformation: oneway or round-trip
     *
     * @return Newly created {@link FeatureModel} with the given name
     */
    FeatureModel transform(Dopler decisionModel, IModelTransformer.STRATEGY level);
}
