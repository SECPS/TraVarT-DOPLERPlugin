package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.IEnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import at.jku.cps.travart.dopler.transformation.DecisionModelUtil;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;

import java.util.List;

class FeatureAndGroupHandler {

    /**
     * Temporary variable to save current decision model
     */
    private IDecisionModel decisionModel;
    private FeatureModel featureModel;

    FeatureAndGroupHandler() {
        decisionModel = null;
        featureModel = null;
    }

    final void handleFeature(Feature feature, IDecisionModel decisionModel, FeatureModel featureModel) {
        this.decisionModel = decisionModel;
        this.featureModel = featureModel;

        //Handle groups of feature
        List<Group> groups = feature.getChildren();
        for (Group group : groups) {
            handleGroup(group);
        }
    }

    private void handleGroup(Group group) {

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

        //Handle features of group
        List<Feature> features = group.getFeatures();
        for (Feature feature : features) {
            handleFeature(feature, decisionModel, featureModel);
        }
    }

    private void handleOrGroup(Group group) {
        Feature parentFeature = group.getParentFeature();
        String id = parentFeature.getFeatureName();
        Range<String> range = new Range<>();
        group.getFeatures().stream().map(Feature::getFeatureName).map(StringValue::new).forEach(range::add);
        Cardinality cardinality = new Cardinality(1, group.getFeatures().size());

        EnumerationDecision decision = new EnumerationDecision(id);
        decision.setQuestion("Which " + id + "?");
        decision.setRange(range);
        decision.setCardinality(cardinality);

        DecisionModelUtil.addToDecisionModel(decision, decisionModel);
        DecisionModelUtil.resolveVisibilityForEnumDecision(decision, parentFeature, featureModel);
    }

    private void handleAlternativeGroup(Group group) {
        Feature parentFeature = group.getParentFeature();
        String id = parentFeature.getFeatureName();
        Range<String> range = new Range<>();
        group.getFeatures().stream().map(Feature::getFeatureName).map(StringValue::new).forEach(range::add);
        Cardinality cardinality = new Cardinality(1, 1);

        IEnumerationDecision<String> decision = new EnumerationDecision(id);
        decision.setQuestion("Which " + id + "?");
        decision.setRange(range);
        decision.setCardinality(cardinality);

        DecisionModelUtil.addToDecisionModel(decision, decisionModel);
        DecisionModelUtil.resolveVisibilityForEnumDecision(decision, parentFeature, featureModel);
    }

    private void handleOptionalGroup(Group group) {
        for (Feature feature : group.getFeatures()) {
            String id = feature.getFeatureName();
            BooleanDecision decision = new BooleanDecision(id);
            decision.setQuestion(id + "?");
            DecisionModelUtil.addToDecisionModel(decision, decisionModel);
            DecisionModelUtil.resolveVisibilityBooleanDecision(decision, feature, featureModel);
        }
    }

    private void handleMandatoryGroup(Group group) {
        /** Only important for round trip */
        if (true) {
            return;
        }

        for (Feature feature : group.getFeatures()) {
            String id = feature.getFeatureName();
            BooleanDecision decision = new BooleanDecision(id);
            decision.setQuestion(id + "?");
            decision.setVisibility(BooleanValue.getFalse());
            DecisionModelUtil.addToDecisionModel(decision, decisionModel);
            DecisionModelUtil.resolveVisibilityBooleanDecision(decision, feature, featureModel);
        }
    }
}
