package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Feature;
import edu.kit.dopler.model.Dopler;

/** This interface is responsible to build the tree structure of the feature model. */
public interface TreeBuilder {

    /**
     * Builds the tree of a {@link de.vill.model.FeatureModel} from the given {@link Dopler} model. Depending on the
     * given {@link IModelTransformer.STRATEGY} the tree will look differently.
     *
     * @param decisionModel {@link Dopler} model from which the tree will be created
     * @param strategy      Strategy that is used to create the tree
     *
     * @return Root of the {@link de.vill.model.FeatureModel}
     */
    Feature buildTree(Dopler decisionModel, IModelTransformer.STRATEGY strategy);
}
