package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Feature;

/** This interface is responsible for beautifying the tree structure of the feature model. */
public interface TreeBeautifier {

    /**
     * Make the given Root {@link Feature} look good. What is considered good, depends on the implementation and the
     * given {@link IModelTransformer.STRATEGY}.
     *
     * @param root     Root {@link Feature} to beautify
     * @param strategy Currently used {@link IModelTransformer.STRATEGY}
     *
     * @return Beautified root {@link Feature}
     */
    Feature beautify(Feature root, IModelTransformer.STRATEGY strategy);
}
