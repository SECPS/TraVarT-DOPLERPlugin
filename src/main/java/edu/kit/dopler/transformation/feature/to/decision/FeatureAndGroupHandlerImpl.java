package edu.kit.dopler.transformation.feature.to.decision;

import at.jku.cps.travart.core.common.IModelTransformer;
import com.google.inject.Inject;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.FeatureType;
import de.vill.model.Group;
import edu.kit.dopler.model.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/** Implementation of {@link FeatureAndGroupHandler} */
public class FeatureAndGroupHandlerImpl implements FeatureAndGroupHandler {

    static final String ENUM_QUESTION = "Which %s?";
    static final String BOOLEAN_QUESTION = "%s?";
    static final String STRING_QUESTION = "What %s?";
    static final String NUMBER_QUESTION = "How much %s?";

    private final VisibilityHandler visibilityHandler;
    private final IdHandler idHandler;

    /** Constructor of {@link FeatureAndGroupHandlerImpl} */
    @Inject
    FeatureAndGroupHandlerImpl(VisibilityHandler visibilityHandler, IdHandler idHandler) {
        this.visibilityHandler = visibilityHandler;
        this.idHandler = idHandler;
    }

    /** Checks all groups of the given feature. */
    @Override
    public void handleFeature(Feature feature, Dopler decisionModel, FeatureModel featureModel,
                              IModelTransformer.STRATEGY level) {

        //Create decisions for types
        if (null != feature.getFeatureType()) {
            switch (feature.getFeatureType()) {
                case STRING -> createStringDecision(decisionModel, featureModel, feature, level);
                case INT, REAL -> createNumberDecision(decisionModel, featureModel, feature, level);
                case BOOL -> {
                    //Do nothing because BOOL is standard type
                }
                default -> throw new IllegalStateException("Unexpected value: " + feature.getFeatureType());
            }
        }

        //Go through all children
        for (Group group : feature.getChildren()) {
            handleGroup(featureModel, decisionModel, group, level);
        }
    }

    /** Create a decision from the given group. Also check all features inside the group for more groups. */
    public void handleGroup(FeatureModel featureModel, Dopler decisionModel, Group group,
                             IModelTransformer.STRATEGY level) {
        switch (group.GROUPTYPE) {
            case OR -> handleOrGroup(featureModel, decisionModel, group, level);
            case ALTERNATIVE -> handleAlternativeGroup(group, decisionModel, featureModel, level);
            case MANDATORY -> handleMandatoryGroup(featureModel, group, decisionModel, level);
            case OPTIONAL -> handleOptionalGroup(group, decisionModel, featureModel, level);
            default -> throw new IllegalStateException("Unexpected value: " + group.GROUPTYPE);
        }

        //Handle features of group
        for (Feature feature : group.getFeatures()) {
            handleFeature(feature, decisionModel, featureModel, level);
        }
    }

    /** Create a single decision for the or-group */
    private void handleOrGroup(FeatureModel featureModel, Dopler decisionModel, Group group,
                               IModelTransformer.STRATEGY level) {
        createEnumDecision(decisionModel, group, featureModel, group.getFeatures().size(), level);
    }

    /** Create a single decision for the alternative-group */
    private void handleAlternativeGroup(Group group, Dopler decisionModel, FeatureModel featureModel,
                                        IModelTransformer.STRATEGY level) {
        createEnumDecision(decisionModel, group, featureModel, 1, level);
    }

    /** Create a decision for each child in the optional-group */
    private void handleOptionalGroup(Group group, Dopler dopler, FeatureModel featureModel,
                                     IModelTransformer.STRATEGY level) {
        for (Feature feature : group.getFeatures()) {
            //Only create decision if it has not feature-type or the feature type is boolean
            if (null == feature.getFeatureType() || FeatureType.BOOL == feature.getFeatureType()) {
                createBooleanDecision(dopler, featureModel, feature, level);
            }
        }
    }

    /** Create a decision for each child in the mandatory-group */
    private void handleMandatoryGroup(FeatureModel featureModel, Group group, Dopler dopler,
                                      IModelTransformer.STRATEGY level) {

        if (IModelTransformer.STRATEGY.ROUNDTRIP != level) {
            return;
        }

        for (Feature feature : group.getFeatures()) {
            //Only create decision if it has not feature-type or the feature type is boolean
            if (null == feature.getFeatureType() || FeatureType.BOOL == feature.getFeatureType()) {
                createMandatoryDecision(dopler, featureModel, feature, level);
            }
        }
    }

    private void createMandatoryDecision(Dopler dopler, FeatureModel featureModel, Feature feature,
                                         IModelTransformer.STRATEGY level) {
        String displayId = idHandler.resolveId(dopler, feature);
        String question = String.format(ENUM_QUESTION, displayId);
        IExpression visibilityCondition =
                visibilityHandler.resolveVisibility(featureModel, dopler, feature.getParentFeature(), level);
        Set<Rule> rules = new LinkedHashSet<>();
        Enumeration enumeration = new Enumeration(Set.of(new EnumerationLiteral(displayId)));
        EnumerationDecision enumerationDecision =
                new EnumerationDecision(displayId + "#", question, "", visibilityCondition, rules, enumeration, 1, 1);

        dopler.addDecision(enumerationDecision);
    }

    private void createBooleanDecision(Dopler dopler, FeatureModel featureModel, Feature feature,
                                       IModelTransformer.STRATEGY level) {
        String description = "";
        IExpression visibility =
                visibilityHandler.resolveVisibility(featureModel, dopler, feature.getParentFeature(), level);
        Set<Rule> rules = new LinkedHashSet<>();
        String id = idHandler.resolveId(dopler, feature);
        String question = String.format(BOOLEAN_QUESTION, id);
        dopler.addDecision(new BooleanDecision(id, question, description, visibility, rules));
    }

    private void createNumberDecision(Dopler dopler, FeatureModel featureModel, Feature feature,
                                      IModelTransformer.STRATEGY level) {
        String id = feature.getFeatureName() + "#Number";
        String question = String.format(NUMBER_QUESTION, feature.getFeatureName());
        IExpression visibility =
                visibilityHandler.resolveVisibilityForTypeDecisions(dopler, featureModel, feature, id, level);
        Set<Rule> rules = new LinkedHashSet<>();
        Set<IExpression> validity = new HashSet<>();
        dopler.addDecision(new NumberDecision(id, question, "", visibility, rules, validity));
    }

    private void createStringDecision(Dopler dopler, FeatureModel featureModel, Feature feature,
                                      IModelTransformer.STRATEGY level) {
        String id = feature.getFeatureName() + "#String";
        String question = String.format(STRING_QUESTION, feature.getFeatureName());
        IExpression visibility =
                visibilityHandler.resolveVisibilityForTypeDecisions(dopler, featureModel, feature, id, level);
        Set<Rule> rules = new LinkedHashSet<>();
        Set<IExpression> validity = new HashSet<>();
        dopler.addDecision(new StringDecision(id, question, "", visibility, rules, validity));
    }

    private void createEnumDecision(Dopler decisionModel, Group group, FeatureModel featureModel, int maxCardinality,
                                    IModelTransformer.STRATEGY level) {
        String displayId = idHandler.resolveId(decisionModel, group.getParentFeature());
        String question = String.format(ENUM_QUESTION, displayId);
        String description = "";
        IExpression visibilityCondition =
                visibilityHandler.resolveVisibility(featureModel, decisionModel, group.getParentFeature(), level);
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
