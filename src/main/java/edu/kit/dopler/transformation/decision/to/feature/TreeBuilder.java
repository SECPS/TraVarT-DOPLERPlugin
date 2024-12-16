package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Feature;
import edu.kit.dopler.model.Dopler;

public interface TreeBuilder {

    Feature buildTree(Dopler decisionModel, String modelName, IModelTransformer.STRATEGY level);
}
