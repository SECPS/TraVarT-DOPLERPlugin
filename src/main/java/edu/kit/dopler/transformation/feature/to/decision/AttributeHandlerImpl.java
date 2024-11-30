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

        Map<String, Attribute> attributes = feature.getAttributes();

        for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
            String attributeName = entry.getKey();
            Attribute attribute = entry.getValue();

            //Booleans are skipped for now
            if ("boolean".equals(attribute.getType())) {
                continue;
            }

            if (!decisionExists(decisionModel, attributeName)) {
                createAttributeDecision(decisionModel, attribute, attributeName);
            }

            Optional<IDecision<?>> attributeDecision = MyUtil.findDecisionById(decisionModel, attributeName);
            if (attributeDecision.isPresent()) {
                createRule(decisionModel, feature, attribute, attributeDecision.get(), featureModel);
            } else {
                throw new DecisionNotPresentException(attributeName);
            }
        }
    }

    private void createRule(Dopler decisionModel, Feature feature, Attribute<?> attribute,
                            IDecision<?> attributeDecision, FeatureModel featureModel) {
        IExpression condition = conditionCreator.createCondition(decisionModel, featureModel,
                new LiteralConstraint(feature.getFeatureName()));

        IAction action;
        switch (attributeDecision) {
            case StringDecision stringDecision ->
                    action = new StringEnforce(stringDecision, new StringValue(attribute.getValue().toString()));
            case NumberDecision numberDecision -> action = new NumberEnforce(numberDecision,
                    new DoubleValue(Double.parseDouble(attribute.getValue().toString())));
            default -> throw new UnexpectedTypeException(attributeDecision);
        }

        //Decision, that gets the rule
        Optional<IDecision<?>> targetDecision = MyUtil.findDecisionByValue(decisionModel, feature.getFeatureName())
                .or(() -> MyUtil.findDecisionById(decisionModel, feature.getFeatureName()));
        if (targetDecision.isEmpty()) {
            throw new DecisionNotPresentException(feature.getFeatureName());
        }
        targetDecision.get().addRule(new Rule(condition, Set.of(action)));
    }

    private void createAttributeDecision(Dopler decisionModel, Attribute<?> attribute, String attributeName) {
        switch (attribute.getType()) {
            case "number" -> {
                NumberDecision decision = new NumberDecision(attributeName,
                        String.format(FeatureAndGroupHandlerImpl.NUMBER_QUESTION, attributeName), "",
                        new BooleanLiteralExpression(false), new LinkedHashSet<>(), new HashSet<>());
                decisionModel.addDecision(decision);
            }
            case "boolean" -> {
                //TODO: viele Modelle haben boolean Attribute. Was soll man damit machen?
                //String description = "";
                //IExpression visibility = new BooleanLiteralExpression(false);
                //Set<Rule> rules = new LinkedHashSet<>();
                //String id = attributeName + "Boolean";
                //String question = String.format(BOOLEAN_QUESTION, attributeName);
                //decisionModel.addDecision(new BooleanDecision(id, question, description, visibility, rules));
            }
            case "string" -> {
                StringDecision decision = new StringDecision(attributeName,
                        String.format(FeatureAndGroupHandlerImpl.STRING_QUESTION, attributeName), "",
                        new BooleanLiteralExpression(false), new LinkedHashSet<>(), new HashSet<>());
                decisionModel.addDecision(decision);
            }
            default -> throw new UnexpectedTypeException(attribute);
        }
    }

    private boolean decisionExists(Dopler decisionModel, String id) {
        return decisionModel.getDecisions().stream().anyMatch(iDecision -> iDecision.getDisplayId().equals(id));
    }
}
