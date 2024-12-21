package edu.kit.dopler.transformation.feature.to.decision;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;

/** This class is responsible for transforming a {@link FeatureModel} into a {@link Dopler} model. */
public interface FmToDmTransformer {

    /**
     * Transforms the given {@link FeatureModel} into a {@link Dopler} model.
     *
     * @param featureModel {@link FeatureModel} to transform
     * @param level        Strategy for the transformation: oneway or round-trip
     *
     * @return Newly created {@link Dopler} model with the given name
     */
    Dopler transform(FeatureModel featureModel, IModelTransformer.STRATEGY level);
}
