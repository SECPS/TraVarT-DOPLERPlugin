package edu.kit.dopler.transformation.util;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import de.vill.model.constraint.LiteralConstraint;
import edu.kit.dopler.transformation.exceptions.FeatureNotPresentException;

import java.util.Collection;
import java.util.Optional;
import java.util.Stack;

/** Implementation of {@link FeatureFinder}. */
public final class FeatureFinderImpl implements FeatureFinder {

    @Override
    public Optional<Feature> findFirstNonMandatoryParent(FeatureModel featureModel, Feature feature) {
        Feature parent = feature;
        while (null != parent.getParentFeature() && null != parent.getParentGroup() &&
                Group.GroupType.MANDATORY == parent.getParentGroup().GROUPTYPE) {
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

        return findFirstNonMandatoryParent(featureModel, feature.get()).map(
                value -> new LiteralConstraint(value.getFeatureName()));
    }

    @Override
    public Optional<Feature> findFeatureByName(Collection<Feature> features, String featureName) {
        Stack<Feature> stack = new Stack<>();
        features.forEach(stack::push);
        while (!stack.empty()) {
            Feature current = stack.pop();

            if (current.getFeatureName().equals(featureName)) {
                return Optional.of(current);
            }

            for (Group child : current.getChildren()) {
                for (Feature feature : child.getFeatures()) {
                    stack.push(feature);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Feature> findFeatureByName(Feature root, String featureName) {

        if (root.getFeatureName().equals(featureName)) {
            return Optional.of(root);
        }

        for (Group group : root.getChildren()) {
            for (Feature feature : group.getFeatures()) {
                Optional<Feature> featureByName = findFeatureByName(feature, featureName);
                if (featureByName.isPresent()) {
                    return featureByName;
                }
            }
        }

        return Optional.empty();
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
