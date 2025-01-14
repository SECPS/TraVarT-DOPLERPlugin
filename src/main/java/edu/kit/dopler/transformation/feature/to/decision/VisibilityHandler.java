package edu.kit.dopler.transformation.feature.to.decision;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.BooleanLiteralExpression;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.model.IExpression;
import edu.kit.dopler.model.ValueDecision;

/** This interface is responsible for creating the visibility expressions for the decisions. */
public interface VisibilityHandler {

    /**
     * Resolves the visibility of a {@link IDecision}. The visibility depends on the parent feature of the group from
     * which the decision originates. If the parent feature is the root of the {@link FeatureModel}, a
     * {@link BooleanLiteralExpression} with the value {@code true} is returned.
     *
     * @param featureModel  {@link FeatureModel} that contains the given {@link Feature}
     * @param level         {@link IModelTransformer.STRATEGY} to use
     * @param decisionModel {@link Dopler} model that contains will contain the resolved visibility
     * @param feature       {@link Feature} that was used to generate the {@link IDecision} for which the visibility
     *                      should be resolved.
     *
     * @return Visibility as an {@link IExpression}
     */
    IExpression resolveVisibility(FeatureModel featureModel, Dopler decisionModel, Feature feature,
                                  IModelTransformer.STRATEGY level);

    /**
     * Resolves the visibility of a {@link ValueDecision}.
     *
     * @param dopler       {@link Dopler} model that contains will contain the resolved visibility
     * @param featureModel {@link FeatureModel} that contains the given {@link Feature}
     * @param feature      {@link Feature} that was used to generate the {@link IDecision} for which the visibility *
     *                     should be resolved.
     * @param id           Identifier of the {@link IDecision} that will have the resolved visibility
     * @param level        {@link IModelTransformer.STRATEGY} to use
     *
     * @return Visibility as an {@link IExpression}
     */
    IExpression resolveVisibilityForTypeDecisions(Dopler dopler, FeatureModel featureModel, Feature feature, String id,
                                                  IModelTransformer.STRATEGY level);
}
