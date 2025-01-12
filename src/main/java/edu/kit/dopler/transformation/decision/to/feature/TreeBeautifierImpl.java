package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import com.google.inject.Inject;
import de.vill.model.Feature;
import de.vill.model.Group;
import edu.kit.dopler.transformation.Transformer;
import edu.kit.dopler.transformation.util.FeatureFinder;

import java.util.*;

import static de.vill.model.Group.GroupType.ALTERNATIVE;
import static de.vill.model.Group.GroupType.MANDATORY;
import static de.vill.model.Group.GroupType.OPTIONAL;

/** Implementation of {@link TreeBeautifier} */
public class TreeBeautifierImpl implements TreeBeautifier {

    private final FeatureFinder featureFinder;

    @Inject
    TreeBeautifierImpl(FeatureFinder featureFinder) {
        this.featureFinder = featureFinder;
    }

    /** Replace alternative groups with one child with mandatory groups */
    private static void replaceSingleAlternativeWithMandatoryGroups(Feature feature) {
        for (Group group : new ArrayList<>(feature.getChildren())) {

            if (ALTERNATIVE == group.GROUPTYPE && 1 == group.getFeatures().size()) {

                feature.getChildren().remove(group);

                Group mandatoryGroup = new Group(MANDATORY);

                group.getFeatures().forEach(child -> {
                    mandatoryGroup.getFeatures().add(child);
                    child.setParentGroup(mandatoryGroup);
                });

                feature.getChildren().add(mandatoryGroup);
                mandatoryGroup.setParentFeature(feature);
            }
        }
    }

    private static void groupFeaturesTogether(Feature feature) {
        //Only mandatory and optional features can be grouped together
        List<Group> groups = feature.getChildren().stream()
                .filter(child -> MANDATORY == child.GROUPTYPE || OPTIONAL == child.GROUPTYPE).toList();

        //Remove existing mandatory and optional groups
        feature.getChildren().removeAll(groups);
        groups.forEach(group -> group.setParentFeature(null));

        //Reconstruct groups
        Map<Group.GroupType, List<Feature>> groupTypesWithFeatures = new LinkedHashMap<>();
        groupTypesWithFeatures.put(MANDATORY, new ArrayList<>());
        groupTypesWithFeatures.put(OPTIONAL, new ArrayList<>());
        for (Group group : groups) {
            groupTypesWithFeatures.get(group.GROUPTYPE).addAll(group.getFeatures());
        }

        //Add groups back to feature
        for (Map.Entry<Group.GroupType, List<Feature>> entry : groupTypesWithFeatures.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                Group newGroup = new Group(entry.getKey());
                entry.getValue().forEach(childFeature -> newGroup.getFeatures().add(childFeature));
                feature.getChildren().add(newGroup);
                newGroup.setParentFeature(feature);
            }
        }
    }

    /** Remove root if it is the standard root */
    private static Feature removeStandardRoot(Feature rootFeature) {
        Feature feature = rootFeature;
        boolean hasNoParent = null == feature.getParentGroup();
        boolean hasStandardName = feature.getFeatureName().equals(Transformer.STANDARD_MODEL_NAME);
        boolean hasOneChild = 1 == feature.getChildren().size();
        boolean hasOneGrandChild = 1 == feature.getChildren().getFirst().getFeatures().size();
        if (hasNoParent && hasStandardName && hasOneChild && hasOneGrandChild) {
            feature = feature.getChildren().getFirst().getFeatures().getFirst();
            feature.setParentGroup(null);
        }
        return feature;
    }

    @Override
    public Feature beautify(Feature root, IModelTransformer.STRATEGY strategy) {
        beautifyRecursively(root, root, strategy);
        return removeStandardRoot(root);
    }

    private void beautifyRecursively(Feature root, Feature feature, IModelTransformer.STRATEGY strategy) {
        if (IModelTransformer.STRATEGY.ONE_WAY == strategy) {
            replaceSingleAlternativeWithMandatoryGroups(feature);
            simplifyTypeFeatures(root, feature);
            simplifyName(root, feature);
        }
        groupFeaturesTogether(feature);

        //Recursively beautify children
        for (Group group : new ArrayList<>(feature.getChildren())) {
            for (Feature childFeature : new ArrayList<>(group.getFeatures())) {
                beautifyRecursively(root, childFeature, strategy);
            }
        }

        //Sort features in groups
        feature.getChildren().forEach(group -> group.getFeatures().sort(Comparator.comparing(Feature::getFeatureName)));

        //Sort groups
        feature.getChildren().sort(Comparator.comparing(child -> child.toString(true, feature.getFeatureName())));
    }

    /** Remove not needed '*' at the end of the feature name. */
    private void simplifyName(Feature root, Feature feature) {
        String id = feature.getFeatureName();
        while (id.endsWith("*")) {
            id = id.substring(0, id.length() - 1);
            if (featureFinder.findFeatureByName(root, id).isEmpty()) {
                feature.setFeatureName(id);
            } else {
                return;
            }
        }
    }

    private void simplifyTypeFeatures(Feature root, Feature feature) {
        List<Group> groups = feature.getChildren();
        for (Group group : new ArrayList<>(groups)) {

            //We need a mandatory group with a single child
            if (MANDATORY != group.GROUPTYPE || 1 != group.getFeatures().size()) {
                continue;
            }

            Feature first = group.getFeatures().getFirst();
            Group parentGroup = feature.getParentGroup();

            //child feature should have a type and the 'feature' should have a parent
            if (null == first.getFeatureType() || null == parentGroup || MANDATORY == parentGroup.GROUPTYPE) {
                continue;
            }

            //Put 'first' at the position of 'feature'. 'feature' is deleted.
            parentGroup.getFeatures().remove(feature);
            parentGroup.getFeatures().add(first);
            first.setParentGroup(parentGroup);
            groups.remove(group);
            first.getChildren().addAll(groups);

            //Beautify here because the feature is put up in the tree
            beautifyRecursively(root, first, IModelTransformer.STRATEGY.ONE_WAY);
        }
    }
}
