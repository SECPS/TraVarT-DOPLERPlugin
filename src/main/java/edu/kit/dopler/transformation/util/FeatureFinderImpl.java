package edu.kit.dopler.transformation.util;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import de.vill.model.constraint.LiteralConstraint;
import edu.kit.dopler.transformation.exceptions.FeatureNotPresentException;

import java.util.Optional;
import java.util.Stack;

/** Implementation of {@link FeatureFinder}. */
public final class FeatureFinderImpl implements FeatureFinder {

    @Override
    public Optional<Feature> findFirstNonMandatoryParent(FeatureModel featureModel, Feature feature) {
        Feature parent = feature;
        while (null != parent.getParentGroup() && Group.GroupType.MANDATORY == parent.getParentGroup().GROUPTYPE) {
            parent = parent.getParentFeature();
        }

        if (featureModel.getRootFeature().equals(parent)) {
            return Optional.empty();
        }

        return Optional.of(parent);
    }

    @Override
    public Optional<LiteralConstraint> findFirstNonMandatoryParent(FeatureModel featureModel,
                                                                   LiteralConstraint literalConstraint) {
        String literal = literalConstraint.getLiteral();
        Optional<Feature> feature = findFeatureWithName(featureModel, literal);

        if (feature.isEmpty()) {
            throw new FeatureNotPresentException(literal);
        }

        Optional<Feature> firstNonMandatoryParent = findFirstNonMandatoryParent(featureModel, feature.get());
        return firstNonMandatoryParent.map(value -> new LiteralConstraint(value.getFeatureName()));
    }

    private Optional<Feature> findFeatureWithName(FeatureModel featureModel, String literal) {
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
}
