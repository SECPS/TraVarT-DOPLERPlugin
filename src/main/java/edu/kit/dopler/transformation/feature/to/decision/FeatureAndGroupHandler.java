package edu.kit.dopler.transformation.feature.to.decision;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import edu.kit.dopler.model.Decision;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.feature.to.decision.constraint.ConstraintHandler;

/**
 * This interface is responsible for creating the {@link Decision}s. The rules are only later filled into the model (see
 * {@link ConstraintHandler}). How it works: for each group in the {@link FeatureModel} one or more {@link Decision}s
 * (depending on the type of the group) are created.
 */
public interface FeatureAndGroupHandler {

    void handleFeature(Feature feature, Dopler decisionModel, FeatureModel featureModel,
                       IModelTransformer.STRATEGY level);

    void handleGroup(FeatureModel featureModel, Dopler decisionModel, Group group, IModelTransformer.STRATEGY level);
}
