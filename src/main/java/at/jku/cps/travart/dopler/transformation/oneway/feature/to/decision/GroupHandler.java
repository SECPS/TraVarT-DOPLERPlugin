package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import de.vill.model.Feature;
import de.vill.model.Group;

import java.util.List;

public class GroupHandler {

    private FeatureHandler featureHandler = null;

    /**
     * Temporary variable to save current decision model
     */
    private IDecisionModel decisionModel;

    GroupHandler() {
        this.decisionModel = null;
    }

    final void handleGroup(Group group, IDecisionModel dm) {
        List<Feature> features = group.getFeatures();
        for (Feature feature : features) {
            featureHandler.handleFeature(feature, dm);
        }

        this.decisionModel = dm;

        switch (group.GROUPTYPE) {
            case OR:
                handleOrGroup(group);
                break;
            case ALTERNATIVE:
                handleAlternativeGroup(group);
                break;
            case MANDATORY:
                handleMandatoryGroup(group);
                break;
            case OPTIONAL:
                handleOptionalGroup(group);
                break;
            case GROUP_CARDINALITY:
                throw new IllegalStateException("Unexpected value: " + group.GROUPTYPE);
            default:
                throw new IllegalStateException("Unexpected value: " + group.GROUPTYPE);
        }
    }

    private void handleOrGroup(Group group) {
        String id = group.getParentFeature().getFeatureName();
        Range<String> range = new Range<>();
        group.getFeatures().stream().map(Feature::getFeatureName).map(StringValue::new).forEach(range::add);
        Cardinality cardinality = new Cardinality(1, group.getFeatures().size());


        EnumerationDecision decision = new EnumerationDecision(id);
        decision.setQuestion("Or " + id);
        decision.setRange(range);
        decision.setCardinality(cardinality);

        decisionModel.add(decision);
    }

    private void handleAlternativeGroup(Group group) {
        String id = group.getParentFeature().getFeatureName();
        Range<String> range = new Range<>();
        group.getFeatures().stream().map(Feature::getFeatureName).map(StringValue::new).forEach(range::add);
        Cardinality cardinality = new Cardinality(1, 1);

        EnumerationDecision decision = new EnumerationDecision(id);
        decision.setQuestion("Alternative " + id);
        decision.setRange(range);
        decision.setCardinality(cardinality);

        decisionModel.add(decision);
    }

    private void handleOptionalGroup(Group group) {
        for (Feature feature : group.getFeatures()) {
            String id = feature.getFeatureName();
            BooleanDecision decision = new BooleanDecision(id);
            decision.setQuestion("Optional " + id);
            decisionModel.add(decision);
        }
    }

    private void handleMandatoryGroup(Group group) {
        // If something is mandatory, no question is asked
    }

    final void setFeatureHandler(FeatureHandler fh) {
        this.featureHandler = fh;
    }
}
