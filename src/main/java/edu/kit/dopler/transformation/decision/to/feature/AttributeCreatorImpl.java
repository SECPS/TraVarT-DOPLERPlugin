package edu.kit.dopler.transformation.decision.to.feature;

import com.google.inject.Inject;
import de.vill.model.Attribute;
import de.vill.model.Feature;
import edu.kit.dopler.model.Enforce;
import edu.kit.dopler.model.IAction;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.transformation.util.FeatureFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Implementation of AttributeCreator. */
public class AttributeCreatorImpl implements AttributeCreator {

    private final FeatureFinder featureFinder;

    @Inject
    public AttributeCreatorImpl(FeatureFinder featureFinder) {
        this.featureFinder = featureFinder;
    }

    @Override
    public void handleAttributeDecisions(List<IDecision<?>> attributeDecisions, Feature rootFeature,
                                         List<IAction> allActions) {

        //Create one attribute for each attribute decision.
        for (IDecision<?> attributeDecision : attributeDecisions) {
            String[] split = attributeDecision.getDisplayId().split("#");
            String featureName = split[0];
            String attributeName = split[1];
            Object attributeValue = findAttributeValue(allActions, featureName, attributeName);

            Feature feature = featureFinder.findFeatureByName(rootFeature, featureName).orElseThrow();
            feature.getAttributes().put(attributeName, new Attribute(attributeName, attributeValue));
        }
    }

    private Object findAttributeValue(List<IAction> allActions, String featureName, String attributeName) {
        for (IAction action : new ArrayList<>(allActions)) {
            if (action instanceof Enforce enforce) {
                IDecision<?> enforceDecision = enforce.getDecision();
                if (TreeBuilderImpl.ATTRIBUTE_PATTERN.matcher(enforceDecision.getDisplayId()).matches()) {
                    String[] split = enforceDecision.getDisplayId().split("#");
                    if (Objects.equals(split[0], featureName) && Objects.equals(split[1], attributeName)) {
                        allActions.remove(action);
                        return enforce.getValue().getValue();
                    }
                }
            }
        }

        throw new RuntimeException(
                "Could not find value of attribute: '%s' of feature '%s'".formatted(attributeName, featureName));
    }
}
