package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Feature;
import de.vill.model.Group;
import edu.kit.dopler.transformation.Transformer;

import java.util.*;

import static de.vill.model.Group.GroupType.ALTERNATIVE;
import static de.vill.model.Group.GroupType.MANDATORY;
import static de.vill.model.Group.GroupType.OPTIONAL;

/** Implementation of {@link TreeBeautifier} */
public class TreeBeautifierImpl implements TreeBeautifier {

    @Override
    public Feature beautify(Feature root, IModelTransformer.STRATEGY strategy) {
        beautifyRecursively(root, strategy);
        return removeStandardRoot(root);
    }

    private void beautifyRecursively(Feature feature, IModelTransformer.STRATEGY strategy) {
        if (IModelTransformer.STRATEGY.ONE_WAY == strategy) {
            replaceSingleAlternativeWithMandatoryGroups(feature);
            replaceMandatoryFeaturesWithTypeChildren(feature, strategy);
        }
        groupFeaturesTogether(feature);

        //Recursively beautify children
        for (Group group : new ArrayList<>(feature.getChildren())) {
            for (Feature childFeature : new ArrayList<>(group.getFeatures())) {
                beautifyRecursively(childFeature, strategy);
            }
        }

        //Sort features in groups
        feature.getChildren().forEach(group -> group.getFeatures().sort(Comparator.comparing(Feature::getFeatureName)));

        //Sort groups
        feature.getChildren().sort(Comparator.comparing(child -> child.toString(true, feature.getFeatureName())));
    }

    private void replaceMandatoryFeaturesWithTypeChildren(Feature feature, IModelTransformer.STRATEGY strategy) {

        List<Group> groups = feature.getChildren();

        for (Group group : new ArrayList<>(groups)) {

            //We need a mandatory group with a single child
            if (MANDATORY != group.GROUPTYPE || 1 != group.getFeatures().size()) {
                continue;
            }

            Feature first = group.getFeatures().getFirst();
            Group parentGroup = feature.getParentGroup();

            //child feature should have a type and the 'feature' should have a parent
            if (null == first.getFeatureType() || null == parentGroup) {
                continue;
            }

            //Put 'first' at the position of 'feature'. 'feature' is deleted.
            parentGroup.getFeatures().remove(feature);
            parentGroup.getFeatures().add(first);
            first.setParentGroup(parentGroup);
            groups.remove(group);
            first.getChildren().addAll(groups);

            //Beautify here because the feature is put up in the tree
            beautifyRecursively(first, strategy);
        }
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
}
