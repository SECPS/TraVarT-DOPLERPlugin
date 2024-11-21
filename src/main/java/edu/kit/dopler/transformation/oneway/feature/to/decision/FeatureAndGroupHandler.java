package edu.kit.dopler.transformation.oneway.feature.to.decision;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import edu.kit.dopler.model.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class FeatureAndGroupHandler {

    private static final String ENUM_QUESTION = "Which %s?";
    private static final String BOOLEAN_QUESTION = "%s?";

    private final VisibilityHandler visibilityHandler;
    private final IdHandler idHandler;

    /**
     * Temporary variable to save current decision model
     */
    private Dopler decisionModel;

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

    void handleFeature(Feature feature, Dopler decisionModel, FeatureModel featureModel) {
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
            case OR -> handleOrGroup(group);
            case ALTERNATIVE -> handleAlternativeGroup(group);
            case MANDATORY -> handleMandatoryGroup(group);
            case OPTIONAL -> handleOptionalGroup(group);
            case GROUP_CARDINALITY -> throw new IllegalStateException("Unexpected value: " + group.GROUPTYPE);
            default -> throw new IllegalStateException("Unexpected value: " + group.GROUPTYPE);
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
        String displayId = idHandler.resolveId(decisionModel, parentFeature);
        String question = String.format(ENUM_QUESTION, displayId);
        String description = "";
        IExpression visibilityCondition =
                visibilityHandler.resolveVisibility(featureModel, decisionModel, parentFeature);
        Set<Rule> rules = new LinkedHashSet<>();

        Set<EnumerationLiteral> collect = new LinkedHashSet<>();
        group.getFeatures().stream().map(Feature::getFeatureName).map(EnumerationLiteral::new).forEach(collect::add);
        Enumeration enumeration = new Enumeration(collect);
        int minCardinality = 1;
        int maxCardinality = group.getFeatures().size();
        EnumerationDecision enumerationDecision =
                new EnumerationDecision(displayId, question, description, visibilityCondition, rules, enumeration,
                        minCardinality, maxCardinality);

        decisionModel.addDecision(enumerationDecision);
    }

    /** Create a single decision for the alternative-group */
    private void handleAlternativeGroup(Group group) {
        Feature parentFeature = group.getParentFeature();

        String displayId = idHandler.resolveId(decisionModel, parentFeature);
        String question = String.format(ENUM_QUESTION, displayId);
        String description = "";
        IExpression visibilityCondition =
                visibilityHandler.resolveVisibility(featureModel, decisionModel, parentFeature);
        Set<Rule> rules = new LinkedHashSet<>();
        Set<EnumerationLiteral> collect = new LinkedHashSet<>();
        group.getFeatures().stream().map(Feature::getFeatureName).map(EnumerationLiteral::new).forEach(collect::add);
        Enumeration enumeration = new Enumeration(collect);
        int minCardinality = 1;
        int maxCardinality = 1;
        EnumerationDecision enumerationDecision =
                new EnumerationDecision(displayId, question, description, visibilityCondition, rules, enumeration,
                        minCardinality, maxCardinality);

        decisionModel.addDecision(enumerationDecision);
    }

    /** Create a decision for each child in the optional-group */
    private void handleOptionalGroup(Group group) {
        Feature parentFeature = group.getParentFeature();
        for (Feature feature : group.getFeatures()) {

            String id = idHandler.resolveId(decisionModel, feature);
            String question = String.format(BOOLEAN_QUESTION, id);
            String description = "";
            IExpression visibility = visibilityHandler.resolveVisibility(featureModel, decisionModel, parentFeature);
            Set<Rule> rules = new LinkedHashSet<>();
            BooleanDecision decision = new BooleanDecision(id, question, description, visibility, rules);
            decisionModel.addDecision(decision);
        }
    }

    /** Create a decision for each child in the mandatory-group */
    private void handleMandatoryGroup(Group group) {
        //Only important for round trip
    }
}
