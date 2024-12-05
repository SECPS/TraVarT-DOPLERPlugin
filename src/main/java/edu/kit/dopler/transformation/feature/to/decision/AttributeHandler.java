package edu.kit.dopler.transformation.feature.to.decision;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.model.Rule;

/** This interface is responsible for handling all attributes inside a {@link FeatureModel}. */
public interface AttributeHandler {

    /**
     * Depending on the given {@link FeatureModel}, this method creates new {@link IDecision}s and new {@link Rule}s and
     * adds them to the given {@link Dopler} model.
     *
     * @param decisionModel Decision model to add the newly created decisions and rules
     * @param featureModel  Feature model containing features with their attributes
     * @param level         Strategy that is used to handle the attributes
     */
    void handleAttributes(Dopler decisionModel, FeatureModel featureModel, IModelTransformer.STRATEGY level);
}
