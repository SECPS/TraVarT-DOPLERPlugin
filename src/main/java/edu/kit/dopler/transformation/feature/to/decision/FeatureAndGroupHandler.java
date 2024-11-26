package edu.kit.dopler.transformation.feature.to.decision;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.FeatureType;
import de.vill.model.Group;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.feature.to.decision.constraint.ConstraintHandler;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class is responsible for creating the {@link Decision}s. The rules are only later filled into the model (see
 * {@link ConstraintHandler}). How it works: for each group in the {@link FeatureModel} one or more {@link Decision}s
 * (depending on the type of the group) are created.
 */
class FeatureAndGroupHandler {

    private static final String ENUM_QUESTION = "Which %s?";
    private static final String BOOLEAN_QUESTION = "%s?";
    private static final String STRING_QUESTION = "What %s?";
    private static final String NUMBER_QUESTION = "How much %s?";

    private final VisibilityHandler visibilityHandler;
    private final IdHandler idHandler;

    /** Constructor of {@link FeatureAndGroupHandler} */
    FeatureAndGroupHandler(VisibilityHandler visibilityHandler, IdHandler idHandler) {
        this.visibilityHandler = visibilityHandler;
        this.idHandler = idHandler;
    }

    /** Checks all groups of the given feature. */
    void handleFeature(Feature feature, Dopler decisionModel, FeatureModel featureModel) {

        //Create decisions for types
        if (null != feature.getFeatureType()) {
            switch (feature.getFeatureType()) {
                case STRING -> createStringDecision(decisionModel, featureModel, feature);
                case INT, REAL -> createNumberDecision(decisionModel, featureModel, feature);
                case BOOL -> {
                    //Do nothing because BOOL is standard type
                }
                default -> throw new IllegalStateException("Unexpected value: " + feature.getFeatureType());
            }
        }

        //Go through all children
        for (Group group : feature.getChildren()) {
            handleGroup(featureModel, decisionModel, group);
        }
    }

    /** Create a decision from the given group. Also check all features inside the group for more groups. */
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
        for (Feature feature : group.getFeatures()) {
            handleFeature(feature, decisionModel, featureModel);
        }
    }

    /** Create a single decision for the or-group */
    private void handleOrGroup(FeatureModel featureModel, Dopler decisionModel, Group group) {
        createEnumDecision(decisionModel, group, featureModel, group.getFeatures().size());
    }

    /** Create a single decision for the alternative-group */
    private void handleAlternativeGroup(Group group, Dopler decisionModel, FeatureModel featureModel) {
        createEnumDecision(decisionModel, group, featureModel, 1);
    }

    /** Create a decision for each child in the optional-group */
    private void handleOptionalGroup(Group group, Dopler dopler, FeatureModel featureModel) {
        for (Feature feature : group.getFeatures()) {
            //Only create decision if it has not feature-type or the feature type is boolean
            if (null == feature.getFeatureType() || FeatureType.BOOL == feature.getFeatureType()) {
                createBooleanDecision(dopler, featureModel, feature);
            }
        }
    }

    /** Create a decision for each child in the mandatory-group */
    private void handleMandatoryGroup(Group group) {
        //Only important for round trip
    }

    private void createBooleanDecision(Dopler dopler, FeatureModel featureModel, Feature feature) {
        String description = "";
        IExpression visibility = visibilityHandler.resolveVisibility(featureModel, dopler, feature.getParentFeature());
        Set<Rule> rules = new LinkedHashSet<>();
        String id = idHandler.resolveId(dopler, feature);
        String question = String.format(BOOLEAN_QUESTION, id);
        dopler.addDecision(new BooleanDecision(id, question, description, visibility, rules));
    }

    private void createNumberDecision(Dopler dopler, FeatureModel featureModel, Feature feature) {
        String id = feature.getFeatureName() + "Number";
        String question = String.format(NUMBER_QUESTION, feature.getFeatureName());
        IExpression visibility = visibilityHandler.resolveVisibility(featureModel, dopler, feature.getParentFeature());
        Set<Rule> rules = new LinkedHashSet<>();
        Set<IExpression> validity = new HashSet<>();
        dopler.addDecision(new NumberDecision(id, question, "", visibility, rules, validity));
    }

    private void createStringDecision(Dopler dopler, FeatureModel featureModel, Feature feature) {
        String id = feature.getFeatureName() + "String";
        String question = String.format(STRING_QUESTION, feature.getFeatureName());
        IExpression visibility = visibilityHandler.resolveVisibility(featureModel, dopler, feature.getParentFeature());
        Set<Rule> rules = new LinkedHashSet<>();
        Set<IExpression> validity = new HashSet<>();
        dopler.addDecision(new StringDecision(id, question, "", visibility, rules, validity));
    }

    private void createEnumDecision(Dopler decisionModel, Group group, FeatureModel featureModel, int maxCardinality) {
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
        EnumerationDecision enumerationDecision =
                new EnumerationDecision(displayId, question, description, visibilityCondition, rules, enumeration,
                        minCardinality, maxCardinality);

        decisionModel.addDecision(enumerationDecision);
    }
}
