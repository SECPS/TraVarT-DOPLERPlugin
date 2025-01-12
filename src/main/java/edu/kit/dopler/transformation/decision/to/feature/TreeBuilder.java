package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Feature;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IAction;
import edu.kit.dopler.model.IDecision;

import java.util.List;

/** This interface is responsible to build the tree structure of the feature model. */
public interface TreeBuilder {

    /**
     * Builds the tree of a {@link de.vill.model.FeatureModel} from the given {@link Dopler} model. Depending on the
     * given {@link IModelTransformer.STRATEGY} the tree will look differently.
     *
     * @param allDecisions All {@link IDecision}s of the {@link Dopler} model
     * @param allActions   All {@link IAction}s of the {@link Dopler} model
     * @param strategy     Strategy that is used to create the tree
     *
     * @return Root of the {@link de.vill.model.FeatureModel}
     */
    Feature buildTree(List<IDecision<?>> allDecisions, List<IAction> allActions, IModelTransformer.STRATEGY strategy);
}
