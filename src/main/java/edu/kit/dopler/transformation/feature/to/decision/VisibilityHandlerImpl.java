package edu.kit.dopler.transformation.feature.to.decision;

import com.google.inject.Inject;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.DecisionNotPresentException;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;
import edu.kit.dopler.transformation.util.DecisionFinder;
import edu.kit.dopler.transformation.util.FeatureFinder;

import java.util.Collections;
import java.util.Optional;

import static edu.kit.dopler.transformation.feature.to.decision.FeatureAndGroupHandlerImpl.BOOLEAN_QUESTION;

/** Implementation of {@link VisibilityHandler} */
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

    @Override
    public IExpression resolveVisibilityForTypeDecisions(Dopler dopler, FeatureModel featureModel, Feature feature,
                                                         String id) {
        IExpression visibility;
        Group parentGroup = feature.getParentGroup();
        Feature parentFeature = feature.getParentFeature();
        switch (parentGroup.GROUPTYPE) {
            case MANDATORY -> {
                //visibility depends on first non-mandatory parent
                visibility = resolveVisibility(featureModel, dopler, parentFeature);
            }
            case OPTIONAL -> {
                //Insert a check decision. Visibility is this newly created check decision.
                BooleanDecision checkDecision =
                        new BooleanDecision(id + "Check", String.format(BOOLEAN_QUESTION, id), "",
                                resolveVisibility(featureModel, dopler, parentFeature), Collections.emptySet());
                dopler.addDecision(checkDecision);
                visibility = new StringLiteralExpression(checkDecision.getDisplayId());
            }
            case OR, ALTERNATIVE -> {
                //visibility is `decision.value`, where the `decision` is the decision of the parent group and
                // `value` is
                // one of the enum values of this decision
                String value = feature.getFeatureName();
                IDecision<?> decisionByValue = decisionFinder.findDecisionByValue(dopler, value)
                        .orElseThrow(() -> new DecisionNotPresentException(value));
                visibility = new StringLiteralExpression(decisionByValue.getDisplayId() + "." + value);
            }
            case null, default -> throw new IllegalStateException("Unexpected value: " + parentGroup.GROUPTYPE);
        }

        return visibility;
    }

    /**
     * Returns a {@link StringLiteralExpression} containing {@code decision.value}, where {@code value} is the name of
     * the given feature and {@code decision} is the name of the parent of the given feature. If the parent is the root
     * of the model, then a {@link BooleanLiteralExpression} with the value {@code true} is returned.
     */
    private IExpression handleAlternativeAndOrFeature(Feature parent, Dopler decisionModel) {
        Optional<IDecision<?>> parentOfParentDecision =
                decisionFinder.findDecisionByValue(decisionModel, parent.getFeatureName());
        if (parentOfParentDecision.isEmpty()) {
            return new BooleanLiteralExpression(true);
        }

        return new StringLiteralExpression(parentOfParentDecision.get().getDisplayId() + "." + parent.getFeatureName());
    }

    /** Returns a {@link StringLiteralExpression} only containing the name of the given feature. */
    private static IExpression handleOptionalFeature(Feature parent) {
        return new StringLiteralExpression(parent.getFeatureName());
    }
}
