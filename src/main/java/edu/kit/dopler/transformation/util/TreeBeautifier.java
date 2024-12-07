package edu.kit.dopler.transformation.util;

import de.vill.model.Feature;
import de.vill.model.Group;

import java.util.*;

import static de.vill.model.Group.GroupType.MANDATORY;
import static de.vill.model.Group.GroupType.OPTIONAL;

public class TreeBeautifier {

    public void beautify(Feature feature) {
        groupFeaturesTogether(feature);

        //Sort groups and features in groups
        feature.getChildren().sort(Comparator.comparing(o -> o.GROUPTYPE.name()));
        feature.getChildren().forEach(group -> group.getFeatures().sort(Comparator.comparing(Feature::getFeatureName)));

        //Recursively beautify children
        for (Group group : feature.getChildren()) {
            List<Feature> childFeatures = group.getFeatures();
            for (Feature childFeature : childFeatures) {
                beautify(childFeature);
            }
        }
    }

    private static void groupFeaturesTogether(Feature feature) {
        //Only mandatory and optional features can be grouped together
        List<Group> groups = feature.getChildren().stream()
                .filter(child -> MANDATORY == child.GROUPTYPE || OPTIONAL == child.GROUPTYPE).toList();

        //Remove existing mandatory and optional groups
        feature.getChildren().removeAll(groups);

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
            }
        }
    }
}
