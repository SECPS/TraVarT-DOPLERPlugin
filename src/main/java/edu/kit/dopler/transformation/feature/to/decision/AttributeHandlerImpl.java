package edu.kit.dopler.transformation.feature.to.decision;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Attribute;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import de.vill.model.constraint.LiteralConstraint;
import edu.kit.dopler.model.BooleanDecision;
import edu.kit.dopler.model.BooleanEnforce;
import edu.kit.dopler.model.BooleanLiteralExpression;
import edu.kit.dopler.model.BooleanValue;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.DoubleValue;
import edu.kit.dopler.model.Enforce;
import edu.kit.dopler.model.IAction;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.model.IExpression;
import edu.kit.dopler.model.NumberDecision;
import edu.kit.dopler.model.NumberEnforce;
import edu.kit.dopler.model.Rule;
import edu.kit.dopler.model.StringDecision;
import edu.kit.dopler.model.StringEnforce;
import edu.kit.dopler.model.StringValue;
import edu.kit.dopler.transformation.exceptions.DecisionNotPresentException;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;
import edu.kit.dopler.transformation.feature.to.decision.constraint.ConditionCreator;
import edu.kit.dopler.transformation.util.DecisionFinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implementation of {@link AttributeHandler} */
public class AttributeHandlerImpl implements AttributeHandler {

    private static final String ATTRIBUTE_DECISION_IDENTIFIER = "%s#%s#Attribute";

    private final ConditionCreator conditionCreator;
    private final DecisionFinder decisionFinder;

    /**
     * Constructor of {@link AttributeHandlerImpl}.
     *
     * @param conditionCreator {@link ConditionCreator}
     * @param decisionFinder   {@link DecisionFinder}
     */
    public AttributeHandlerImpl(ConditionCreator conditionCreator, DecisionFinder decisionFinder) {
        this.conditionCreator = conditionCreator;
        this.decisionFinder = decisionFinder;
    }

    @Override
    public void handleAttributes(Dopler decisionModel, FeatureModel featureModel, IModelTransformer.STRATEGY level) {
        //Attributes are only important for the round trip
        if (IModelTransformer.STRATEGY.ROUNDTRIP == level) {
            Feature root = featureModel.getRootFeature();
            checkFeatureRecursive(decisionModel, featureModel, root);
        }
    }

    /** Check the given {@link Feature} for attributes. */
    private void checkFeatureRecursive(Dopler decisionModel, FeatureModel featureModel, Feature feature) {
        if (!feature.getAttributes().isEmpty()) {
            handleFeatureWithAttribute(decisionModel, featureModel, feature);
        }

        for (Group child : feature.getChildren()) {
            checkGroupRecursive(decisionModel, featureModel, child);
        }
    }

    /** Check the given {@link Group} for features. */
    private void checkGroupRecursive(Dopler decisionModel, FeatureModel featureModel, Group group) {
        for (Feature feature : group.getFeatures()) {
            checkFeatureRecursive(decisionModel, featureModel, feature);
        }
    }

    /**
     * If the given {@link Feature} has {@link Attribute}s, this method created for each {@link Attribute} a new
     * {@link IDecision} and a new {@link Rule} and adds them to the {@link Dopler} model.
     */
    private void handleFeatureWithAttribute(Dopler decisionModel, FeatureModel featureModel, Feature feature) {

        //This map saves the rules (as values) that are added to the decisions with the attributes (as keys)
        Map<IDecision<?>, List<IAction>> rulesMap = new HashMap<>();

        //Create new decisions and fill map
        Map<String, Attribute> attributes = feature.getAttributes();
        for (Attribute<?> attribute : attributes.values()) {
            IDecision<?> attributeDecision = createAttributeDecision(feature, attribute);
            decisionModel.addDecision(attributeDecision);

            IDecision<?> targetDecision = getTargetDecision(decisionModel, feature);
            rulesMap.putIfAbsent(targetDecision, new ArrayList<>());
            rulesMap.get(targetDecision).add(createAction(attribute, attributeDecision));
        }

        //Give decisions that contains the attributes new rules
        for (Map.Entry<IDecision<?>, List<IAction>> entry : rulesMap.entrySet()) {
            IExpression condition = conditionCreator.createCondition(decisionModel, featureModel,
                    new LiteralConstraint(feature.getFeatureName()));
            LinkedHashSet<IAction> actions = new LinkedHashSet<>(entry.getValue());
            entry.getKey().addRule(new Rule(condition, actions));
        }
    }

    /** This method finds in the {@link Dopler} model the decision, that gets the newly created {@link Rule}. */
    private IDecision<?> getTargetDecision(Dopler decisionModel, Feature feature) {
        Optional<IDecision<?>> targetDecision =
                decisionFinder.findDecisionByValue(decisionModel, feature.getFeatureName())
                        .or(() -> decisionFinder.findDecisionById(decisionModel, feature.getFeatureName()));
        if (targetDecision.isEmpty()) {
            //TODO Exception here will be thrown if a mandatory feature has a attribute
            throw new DecisionNotPresentException(feature.getFeatureName());
        }
        return targetDecision.get();
    }

    /** Creates a new {@link Enforce} rule for the given {@link Attribute} and target {@link IDecision}. */
    private IAction createAction(Attribute<?> attribute, IDecision<?> attributeDecision) {
        return switch (attributeDecision) {
            case StringDecision stringDecision ->
                    new StringEnforce(stringDecision, new StringValue(attribute.getValue().toString()));
            case NumberDecision numberDecision -> new NumberEnforce(numberDecision,
                    new DoubleValue(Double.parseDouble(attribute.getValue().toString())));
            case BooleanDecision booleanDecision -> new BooleanEnforce(booleanDecision,
                    new BooleanValue(Boolean.valueOf(attribute.getValue().toString())));
            default -> throw new UnexpectedTypeException(attributeDecision);
        };
    }

    /** Creates a new {@link IDecision} fot the given {@link Attribute}. */
    private IDecision<?> createAttributeDecision(Feature feature, Attribute<?> attribute) {
        //TODO: add handling of lists, arrays and constraints
        return switch (attribute.getType()) {
            case "number" -> new NumberDecision(
                    ATTRIBUTE_DECISION_IDENTIFIER.formatted(feature.getFeatureName(), attribute.getName()),
                    String.format(FeatureAndGroupHandlerImpl.NUMBER_QUESTION, attribute.getName()), "",
                    new BooleanLiteralExpression(false), new LinkedHashSet<>(), new HashSet<>());
            case "string" -> new StringDecision(
                    ATTRIBUTE_DECISION_IDENTIFIER.formatted(feature.getFeatureName(), attribute.getName()),
                    String.format(FeatureAndGroupHandlerImpl.STRING_QUESTION, attribute.getName()), "",
                    new BooleanLiteralExpression(false), new LinkedHashSet<>(), new HashSet<>());
            case "boolean" -> new BooleanDecision(
                    ATTRIBUTE_DECISION_IDENTIFIER.formatted(feature.getFeatureName(), attribute.getName()),
                    String.format(FeatureAndGroupHandlerImpl.BOOLEAN_QUESTION, attribute.getName()), "",
                    new BooleanLiteralExpression(false), new LinkedHashSet<>());
            default -> throw new UnexpectedTypeException(attribute);
        };
    }
}
