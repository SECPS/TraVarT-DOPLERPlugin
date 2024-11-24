package edu.kit.dopler.transformation.feature.to.decision;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import edu.kit.dopler.model.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class FeatureAndGroupHandler {

    private static final String ENUM_QUESTION = "Which %s?";
    private static final String BOOLEAN_QUESTION = "%s?";

    private final VisibilityHandler visibilityHandler;
    private final IdHandler idHandler;

    FeatureAndGroupHandler(VisibilityHandler visibilityHandler, IdHandler idHandler) {
        this.visibilityHandler = visibilityHandler;
        this.idHandler = idHandler;
    }

    void handleFeature(Feature feature, Dopler decisionModel, FeatureModel featureModel) {

        //Handle groups of feature
        List<Group> groups = feature.getChildren();
        for (Group group : groups) {
            handleGroup(featureModel, decisionModel, group);
        }
    }

    private void handleGroup(FeatureModel featureModel, Dopler decisionModel, Group group) {
        switch (group.GROUPTYPE) {
            case OR -> handleOrGroup(featureModel, decisionModel, group);
            case ALTERNATIVE -> handleAlternativeGroup(group, decisionModel, featureModel);
            case MANDATORY -> handleMandatoryGroup(group);
            case OPTIONAL -> handleOptionalGroup(group, decisionModel, featureModel);
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
    private void handleOrGroup(FeatureModel featureModel, Dopler decisionModel, Group group) {
        String displayId = idHandler.resolveId(decisionModel, group.getParentFeature());
        String question = String.format(ENUM_QUESTION, displayId);
        String description = "";
        IExpression visibilityCondition =
                visibilityHandler.resolveVisibility(featureModel, decisionModel, group.getParentFeature());
        Set<Rule> rules = new LinkedHashSet<>();
        Set<EnumerationLiteral> literals = new LinkedHashSet<>();
        group.getFeatures().stream().map(Feature::getFeatureName).map(EnumerationLiteral::new).forEach(literals::add);
        Enumeration enumeration = new Enumeration(literals);
        int minCardinality = 1;
        int maxCardinality = group.getFeatures().size();
        EnumerationDecision enumerationDecision =
                new EnumerationDecision(displayId, question, description, visibilityCondition, rules, enumeration,
                        minCardinality, maxCardinality);

        decisionModel.addDecision(enumerationDecision);
    }

    /** Create a single decision for the alternative-group */
    private void handleAlternativeGroup(Group group, Dopler decisionModel, FeatureModel featureModel) {
        String displayId = idHandler.resolveId(decisionModel, group.getParentFeature());
        String question = String.format(ENUM_QUESTION, displayId);
        String description = "";
        IExpression visibilityCondition =
                visibilityHandler.resolveVisibility(featureModel, decisionModel, group.getParentFeature());
        Set<Rule> rules = new LinkedHashSet<>();
        Set<EnumerationLiteral> literals = new LinkedHashSet<>();
        group.getFeatures().stream().map(Feature::getFeatureName).map(EnumerationLiteral::new).forEach(literals::add);
        Enumeration enumeration = new Enumeration(literals);
        int minCardinality = 1;
        int maxCardinality = 1;
        EnumerationDecision enumerationDecision =
                new EnumerationDecision(displayId, question, description, visibilityCondition, rules, enumeration,
                        minCardinality, maxCardinality);

        decisionModel.addDecision(enumerationDecision);
    }

    /** Create a decision for each child in the optional-group */
    private void handleOptionalGroup(Group group, Dopler decisionModel, FeatureModel featureModel) {
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
