package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;

public interface DmToFmTransformer {

    FeatureModel transform(Dopler decisionModel, IModelTransformer.STRATEGY level);
}
