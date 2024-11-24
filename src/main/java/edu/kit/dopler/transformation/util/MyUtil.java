package edu.kit.dopler.transformation.util;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import de.vill.model.constraint.LiteralConstraint;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.FeatureNotPresentException;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.Optional;
import java.util.Stack;
import java.util.function.Predicate;

/** Set of utility methods for editing decision models */
public final class MyUtil {

    private MyUtil() {
    }

    public static Optional<IDecision<?>> findDecisionById(Dopler decisionModel, String id) {
        return decisionModel.getDecisions().stream().filter(iDecision -> iDecision.getDisplayId().equals(id))
                .findFirst();
    }

    public static Optional<IDecision<?>> findDecisionByValue(Dopler decisionModel, String value) {
        //Decision should have the given value in its RangeValue
        Predicate<IDecision<?>> filter = decision -> {

            switch (decision) {
                case EnumerationDecision enumerationDecision -> {
                    Enumeration enumeration = enumerationDecision.getEnumeration();
                    for (EnumerationLiteral literal : enumeration.getEnumerationLiterals()) {
                        if (literal.getValue().equals(value)) {
                            return true;
                        }
                    }
                }
                case BooleanDecision booleanDecision -> {
                    return false;
                }
                case null, default -> throw new UnexpectedTypeException(decision);
            }

            return false;
        };

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
