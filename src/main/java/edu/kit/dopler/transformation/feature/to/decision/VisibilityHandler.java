package edu.kit.dopler.transformation.feature.to.decision;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.util.MyUtil;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.Optional;

class VisibilityHandler {

    /**
     * Resolves the visibility of the given decision. The visibility depends on the parents of the group from which the
     * decision originates.
     */
    IExpression resolveVisibility(FeatureModel featureModel, Dopler decisionModel, Feature feature) {

        //Search for first non-mandatory parent
        Optional<Feature> parent = MyUtil.findFirstNonMandatoryParent(featureModel, feature);

        //Parent is root. Decision for root feature is always taken. Visibility must therefore be true.
        if (parent.isEmpty()) {
            return new BooleanLiteralExpression(true);
        }

        //Parent is not root. Look at parent group of parent
        return switch (parent.get().getParentGroup().GROUPTYPE) {
            case OPTIONAL -> handleBooleanDecision(parent.get());
            case ALTERNATIVE -> handleAlternative(parent.get(), decisionModel);
            case OR -> handleOr(parent.get(), decisionModel);
            case GROUP_CARDINALITY, MANDATORY -> throw new UnexpectedTypeException(parent.get().getParentGroup());
        };
    }

    /** Returns {@code decision.value}, because you can choose several enums from the decision */
    private IExpression handleOr(Feature parent, Dopler decisionModel) {
        Feature parentOfParent = parent.getParentFeature();

        Optional<IDecision<?>> parentOfParentDecision =
                MyUtil.findDecisionById(decisionModel, parentOfParent.getFeatureName());
        if (parentOfParentDecision.isEmpty()) {
            return new BooleanLiteralExpression(true);
        }

        IDecision<?> decision = parentOfParentDecision.get();
        return new StringLiteralExpression(decision.getDisplayId() + "." + parent.getFeatureName());
    }

    /** Returns {@code getValue(decision) = value}, because you can only choose one enum from the decision */
    private IExpression handleAlternative(Feature parent, Dopler decisionModel) {
        Feature parentOfParent = parent.getParentFeature();

        Optional<IDecision<?>> parentOfParentDecision =
                MyUtil.findDecisionById(decisionModel, parentOfParent.getFeatureName());
        if (parentOfParentDecision.isEmpty()) {
            return new BooleanLiteralExpression(true);
        }

        //IExpression left = new GetValueFunction(new StringDecision(parentOfParent.getFeatureName()));
        //IExpression right = new DecisionValueCondition(parentOfParentDecision.get(), new StringValue(parent
        // .getFeatureName()));

        return new StringLiteralExpression(parentOfParent.getFeatureName() + "." + parent.getFeatureName());
    }

    /** Returns {@code decision}, because you can only choose one enum from the decision */
    private static IExpression handleBooleanDecision(Feature parent) {
        return new StringLiteralExpression(parent.getFeatureName());
    }
}
