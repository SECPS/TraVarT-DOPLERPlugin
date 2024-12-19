package edu.kit.dopler.transformation.decision.to.feature;

import de.vill.model.Feature;
import de.vill.model.Group;

import java.util.*;

import static de.vill.model.Group.GroupType.ALTERNATIVE;
import static de.vill.model.Group.GroupType.MANDATORY;
import static de.vill.model.Group.GroupType.OPTIONAL;

/** Implementation of {@link TreeBeautifier} */
public class TreeBeautifierImpl implements TreeBeautifier {

    @Override
    public void beautify(Feature feature) {
        replaceSingleAlternativeWithMandatoryGroups(feature);
        groupFeaturesTogether(feature);

        //Recursively beautify children
        for (Group group : new ArrayList<>(feature.getChildren())) {
            for (Feature childFeature : new ArrayList<>(group.getFeatures())) {
                beautify(childFeature);
            }
        }

        //Sort features in groups
        feature.getChildren().forEach(group -> group.getFeatures().sort(Comparator.comparing(Feature::getFeatureName)));

        //Sort groups
        feature.getChildren().sort(Comparator.comparing(child -> child.toString(true, feature.getFeatureName())));
    }

    //Replace alternative groups with one child with mandatory groups
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
}
