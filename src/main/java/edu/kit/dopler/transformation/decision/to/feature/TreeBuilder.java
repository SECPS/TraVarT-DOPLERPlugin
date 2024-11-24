package edu.kit.dopler.transformation.decision.to.feature;

import de.vill.model.Feature;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IDecision;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class TreeBuilder {

    Feature buildTree(Dopler decisionModel, List<Feature> enumFeatures, List<Feature> optionalFeatures) {
        Feature rootFeature = new Feature("ROOT");
        List<Feature> allFeatures = new ArrayList<>();
        allFeatures.addAll(enumFeatures);
        allFeatures.addAll(optionalFeatures);


        return rootFeature;
    }

    private static Optional<Feature> getFeatureByDecision(List<Feature> allFeatures, IDecision<?> decision) {
        return allFeatures.stream().filter(feature -> feature.getFeatureName().equals(decision.getDisplayId()))
                .findFirst();
    }
}
