package edu.kit.dopler.transformation.feature.to.decision;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;
import edu.kit.dopler.transformation.util.MyUtil;

import java.util.Optional;

/** This class is responsible for creating the visibility expressions for the decisions. */
class VisibilityHandler {

    /**
     * Resolves the visibility of a decision. The visibility depends on the parent feature of the group from which the
     * decision originates. If the parent feature is the root of the {@link FeatureModel}, a
     * {@link BooleanLiteralExpression} with the value {@code true} is returned.
     */
    IExpression resolveVisibility(FeatureModel featureModel, Dopler decisionModel, Feature feature) {
        Optional<Feature> firstNonMandatoryParent = MyUtil.findFirstNonMandatoryParent(featureModel, feature);

        // If parent is empty, then parent is root.
        // -> Decision for root feature is always taken. Visibility must therefore be true.
        // If parent is present, then parent is not root.
        // -> Look at parent group of parent
        return firstNonMandatoryParent.map(parent -> switch (parent.getParentGroup().GROUPTYPE) {
            case OPTIONAL -> handleOptionalFeature(parent);
            case ALTERNATIVE, OR -> handleAlternativeAndOrFeature(parent, decisionModel);
            case GROUP_CARDINALITY, MANDATORY -> throw new UnexpectedTypeException(parent.getParentGroup());
        }).orElseGet(() -> new BooleanLiteralExpression(true));
    }

    /**
     * Returns a {@link StringLiteralExpression} containing {@code decision.value}, where {@code value} is the name of
     * the given feature and {@code decision} is the name of the parent of the given feature. If the parent is the root
     * of the model, then a {@link BooleanLiteralExpression} with the value {@code true} is returned.
     */
    private IExpression handleAlternativeAndOrFeature(Feature parent, Dopler decisionModel) {
        Feature parentOfParent = parent.getParentFeature();

        Optional<IDecision<?>> parentOfParentDecision =
                MyUtil.findDecisionById(decisionModel, parentOfParent.getFeatureName());
        if (parentOfParentDecision.isEmpty()) {
            return new BooleanLiteralExpression(true);
        }

        return new StringLiteralExpression(parentOfParent.getFeatureName() + "." + parent.getFeatureName());
    }

    /** Returns a {@link StringLiteralExpression} only containing the name of the given feature. */
    private static IExpression handleOptionalFeature(Feature parent) {
        return new StringLiteralExpression(parent.getFeatureName());
    }
}
