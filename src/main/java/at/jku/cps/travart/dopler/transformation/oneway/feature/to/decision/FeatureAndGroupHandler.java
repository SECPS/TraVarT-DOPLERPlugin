package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.IEnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;

import java.util.List;

class FeatureAndGroupHandler {

    private static final String ENUM_QUESTION = "Which %s?";
    private static final String BOOLEAN_QUESTION = "%s?";

    private final VisibilityHandler visibilityHandler;
    private final IdHandler idHandler;

    /**
     * Temporary variable to save current decision model
     */
    private IDecisionModel decisionModel;

    /**
     * Temporary variable to save feature decision model
     */
    private FeatureModel featureModel;

    FeatureAndGroupHandler(VisibilityHandler visibilityHandler, IdHandler idHandler) {
        this.visibilityHandler = visibilityHandler;
        this.idHandler = idHandler;
        decisionModel = null;
        featureModel = null;
    }

    void handleFeature(Feature feature, IDecisionModel decisionModel, FeatureModel featureModel) {
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

    /** Create a single decision for the or-group */
    private void handleOrGroup(Group group) {
        Feature parentFeature = group.getParentFeature();
        Range<String> range = new Range<>();
        group.getFeatures().stream().map(Feature::getFeatureName).map(StringValue::new).forEach(range::add);
        Cardinality cardinality = new Cardinality(1, group.getFeatures().size());

        EnumerationDecision decision = new EnumerationDecision("");
        decision.setVisibility(visibilityHandler.resolveVisibility(featureModel, parentFeature, decisionModel));
        decision.setId(idHandler.resolveId(decisionModel, parentFeature));
        decision.setQuestion(String.format(ENUM_QUESTION, decision.getId()));
        decision.setRange(range);
        decision.setCardinality(cardinality);

        decisionModel.add(decision);
    }

    /** Create a single decision for the alternative-group */
    private void handleAlternativeGroup(Group group) {
        Feature parentFeature = group.getParentFeature();
        Range<String> range = new Range<>();
        group.getFeatures().stream().map(Feature::getFeatureName).map(StringValue::new).forEach(range::add);
        Cardinality cardinality = new Cardinality(1, 1);

        IEnumerationDecision<String> decision = new EnumerationDecision("");
        decision.setVisibility(visibilityHandler.resolveVisibility(featureModel, parentFeature, decisionModel));
        decision.setId(idHandler.resolveId(decisionModel, parentFeature));
        decision.setQuestion(String.format(ENUM_QUESTION, decision.getId()));
        decision.setRange(range);
        decision.setCardinality(cardinality);

        decisionModel.add(decision);
    }

    /** Create a decision for each child in the optional-group */
    private void handleOptionalGroup(Group group) {
        Feature parentFeature = group.getParentFeature();
        for (Feature feature : group.getFeatures()) {
            BooleanDecision decision = new BooleanDecision("");
            decision.setVisibility(visibilityHandler.resolveVisibility(featureModel, parentFeature, decisionModel));
            decision.setId(idHandler.resolveId(decisionModel, feature));
            decision.setQuestion(String.format(BOOLEAN_QUESTION, decision.getId()));
            decisionModel.add(decision);
        }
    }

    /** Create a decision for each child in the mandatory-group */
    private void handleMandatoryGroup(Group group) {
        //Only important for round trip
    }
}
