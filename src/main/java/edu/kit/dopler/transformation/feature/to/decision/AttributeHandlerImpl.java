package edu.kit.dopler.transformation.feature.to.decision;

import de.vill.model.Attribute;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import de.vill.model.constraint.LiteralConstraint;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.DecisionNotPresentException;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;
import edu.kit.dopler.transformation.feature.to.decision.constraint.ConditionCreator;
import edu.kit.dopler.transformation.util.MyUtil;

import java.util.*;

/** Implementation of {@link AttributeHandler} */
class AttributeHandlerImpl implements AttributeHandler {

    private final ConditionCreator conditionCreator;

    AttributeHandlerImpl(ConditionCreator conditionCreator) {
        this.conditionCreator = conditionCreator;
    }

    public void handleAttributes(Dopler decisionModel, FeatureModel featureModel) {
        Feature root = featureModel.getRootFeature();
        checkFeatureRecursive(decisionModel, featureModel, root);
    }

    private void checkFeatureRecursive(Dopler decisionModel, FeatureModel featureModel, Feature feature) {
        if (!feature.getAttributes().isEmpty()) {
            handleFeatureWithAttribute(decisionModel, featureModel, feature);
        }

        for (Group child : feature.getChildren()) {
            checkGroupRecursive(decisionModel, featureModel, child);
        }
    }

    private void checkGroupRecursive(Dopler decisionModel, FeatureModel featureModel, Group group) {
        for (Feature feature : group.getFeatures()) {
            checkFeatureRecursive(decisionModel, featureModel, feature);
        }
    }

    @SuppressWarnings("rawtypes") //Attributes are returned with no type from the feature
    private void handleFeatureWithAttribute(Dopler decisionModel, FeatureModel featureModel, Feature feature) {

        //This map saves the rules that are added to the decisions with the attributes
        Map<IDecision<?>, List<IAction>> rulesMap = new HashMap<>();

        //Create new decisions and fill map
        Map<String, Attribute> attributes = feature.getAttributes();
        for (Attribute attribute : attributes.values()) {
            IDecision attributeDecision = createAttributeDecision(attribute, feature);
            decisionModel.addDecision(attributeDecision);

            IDecision<?> targetDecision = getTargetDecision(decisionModel, feature);
            rulesMap.putIfAbsent(targetDecision, new ArrayList<>());
            rulesMap.get(targetDecision).add(createAction(attribute, attributeDecision));
        }

        //Give decisions that contains the attributes new rules
        for (Map.Entry<IDecision<?>, List<IAction>> entry : rulesMap.entrySet()) {
            entry.getKey().addRule(new Rule(conditionCreator.createCondition(decisionModel, featureModel,
                    new LiteralConstraint(feature.getFeatureName())), new LinkedHashSet<>(entry.getValue())));
        }
    }

    private static IDecision<?> getTargetDecision(Dopler decisionModel, Feature feature) {
        //Decision, that gets the rule
        Optional<IDecision<?>> targetDecision = MyUtil.findDecisionByValue(decisionModel, feature.getFeatureName())
                .or(() -> MyUtil.findDecisionById(decisionModel, feature.getFeatureName()));
        if (targetDecision.isEmpty()) {
            //TODO Exception here will be thrown if a mandatory feature has a attribute
            throw new DecisionNotPresentException(feature.getFeatureName());
        }
        return targetDecision.get();
    }

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

    private IDecision<?> createAttributeDecision(Attribute<?> attribute, Feature feature) {
        //TODO: handle boolean attributes
        return switch (attribute.getType()) {
            case "number" -> new NumberDecision(feature.getFeatureName() + "#" + attribute.getName(),
                    String.format(FeatureAndGroupHandlerImpl.NUMBER_QUESTION, attribute.getName()), "",
                    new BooleanLiteralExpression(false), new LinkedHashSet<>(), new HashSet<>());
            case "string" -> new StringDecision(feature.getFeatureName() + "#" + attribute.getName(),
                    String.format(FeatureAndGroupHandlerImpl.STRING_QUESTION, attribute.getName()), "",
                    new BooleanLiteralExpression(false), new LinkedHashSet<>(), new HashSet<>());
            case "boolean" -> new BooleanDecision(feature.getFeatureName() + "#" + attribute.getName(),
                    String.format(FeatureAndGroupHandlerImpl.BOOLEAN_QUESTION, attribute.getName()), "",
                    new BooleanLiteralExpression(false), new LinkedHashSet<>());
            default -> throw new UnexpectedTypeException(attribute);
        };
    }
}
