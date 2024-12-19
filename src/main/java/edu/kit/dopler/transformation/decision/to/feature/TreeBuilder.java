package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Feature;
import edu.kit.dopler.model.Dopler;

/** This interface is responsible to build the tree structure of the feature model. */
public interface TreeBuilder {

    Feature buildTree(Dopler decisionModel, String modelName, IModelTransformer.STRATEGY level);
}
