package at.jku.cps.travart.dopler.transformation.util;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import de.vill.model.constraint.LiteralConstraint;

import java.util.Optional;
import java.util.Stack;
import java.util.function.Predicate;

/** Set of utility methods for editing decision models */
public final class MyUtil {

    private MyUtil() {
    }

    public static Optional<IDecision<?>> findDecisionById(IDecisionModel decisionModel, String id) {
        return decisionModel.getDecisions().stream().filter(iDecision -> iDecision.getId().equals(id)).findFirst();
    }

    public static Optional<IDecision<?>> findDecisionByValue(IDecisionModel decisionModel, String value) {
        //Decision should have the given value in its RangeValue
        Predicate<IDecision<?>> filter = decision -> decision.getRange().stream()
                .anyMatch(abstractRangeValue -> abstractRangeValue.toString().equals(value));

        return decisionModel.getDecisions().stream().filter(filter).findFirst();
    }

    public static Optional<Feature> findFeatureWithName(FeatureModel featureModel, String literal) {
        Stack<Feature> stack = new Stack<>();
        stack.add(featureModel.getRootFeature());

        while (!stack.empty()) {
            Feature current = stack.pop();
            if (current.getFeatureName().equals(literal)) {
                return Optional.of(current);
            }

            current.getChildren().stream().flatMap(group -> group.getFeatures().stream()).forEach(stack::push);
        }

        return Optional.empty();
    }

    public static Optional<Feature> findFirstNonMandatoryParent(FeatureModel featureModel, Feature feature) {
        Feature parent = feature;
        while (null != parent.getParentGroup() && Group.GroupType.MANDATORY == parent.getParentGroup().GROUPTYPE) {
            parent = parent.getParentFeature();
        }

        if (featureModel.getRootFeature().equals(parent)) {
            return Optional.empty();
        }

        return Optional.of(parent);
    }

    public static Optional<LiteralConstraint> findFirstNonMandatoryParent(FeatureModel featureModel,
                                                                          LiteralConstraint literalConstraint) {
        String literal = literalConstraint.getLiteral();
        Optional<Feature> feature = findFeatureWithName(featureModel, literal);

        if (feature.isEmpty()) {
            throw new FeatureNotPresentException(literal);
        }

        Optional<Feature> firstNonMandatoryParent = findFirstNonMandatoryParent(featureModel, feature.get());
        return firstNonMandatoryParent.map(value -> new LiteralConstraint(value.getFeatureName()));
    }
}
