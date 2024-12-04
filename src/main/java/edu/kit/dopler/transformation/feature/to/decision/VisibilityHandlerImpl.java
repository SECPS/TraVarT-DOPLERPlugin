package edu.kit.dopler.transformation.feature.to.decision;

import com.google.inject.Inject;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;
import edu.kit.dopler.transformation.util.DecisionFinder;
import edu.kit.dopler.transformation.util.FeatureFinder;

import java.util.Optional;

public class VisibilityHandlerImpl implements VisibilityHandler {

    private final FeatureFinder featureFinder;
    private final DecisionFinder decisionFinder;

    @Inject
    public VisibilityHandlerImpl(FeatureFinder featureFinder, DecisionFinder decisionFinder) {
        this.featureFinder = featureFinder;
        this.decisionFinder = decisionFinder;
    }

    @Override
    public IExpression resolveVisibility(FeatureModel featureModel, Dopler decisionModel, Feature feature) {
        Optional<Feature> firstNonMandatoryParent = featureFinder.findFirstNonMandatoryParent(featureModel, feature);

        // Two possible cases depending on the value of firstNonMandatoryParent:
        // 1. If parent is empty, then there is no non-mandatory parent.
        // -> Decision is always taken. Visibility must therefore be true.
        // 2. If parent is present, then there is a non-mandatory parent.
        // -> Look at parent group of this non-mandatory parent

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
                decisionFinder.findDecisionById(decisionModel, parentOfParent.getFeatureName());
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
