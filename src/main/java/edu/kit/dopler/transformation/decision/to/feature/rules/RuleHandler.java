package edu.kit.dopler.transformation.decision.to.feature.rules;

import com.google.inject.Inject;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ImplicationConstraint;
import edu.kit.dopler.model.*;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

public class RuleHandler {

    private final LeftCreator leftCreator;
    private final RightCreator rightCreator;

    @Inject
    public RuleHandler(LeftCreator leftCreator, RightCreator rightCreator) {
        this.leftCreator = leftCreator;
        this.rightCreator = rightCreator;
    }

    public void handleRules(Dopler decisionModel, FeatureModel featureModel) {
        for (IDecision<?> decision : decisionModel.getDecisions()) {

            //TODO Delete this check
            if (decision.getRules().size() > 1) {
                throw new RuntimeException("Decisions with more then one rule is not supported");
            }

            for (Rule rule : decision.getRules()) {
                handleRule(featureModel, rule);
            }
        }

        //Sort constraints
        featureModel.getOwnConstraints().sort(Comparator.comparing(constraint -> constraint.toString(true, "")));
    }

    private void handleRule(FeatureModel featureModel, Rule rule) {
        IExpression condition = rule.getCondition();
        Set<IAction> actions = rule.getActions();

        //TODO: Delete this. Actions should never be empty.
        if (actions.isEmpty()) {
            throw new RuntimeException("actions are empty");
        }

        Optional<Constraint> left = leftCreator.handleCondition(condition);
        Constraint right = rightCreator.createRight(actions);

        if (left.isPresent()) {
            // constraint: left -> right
            featureModel.getOwnConstraints().add(new ImplicationConstraint(left.get(), right));
        } else {
            // constraint: right
            featureModel.getOwnConstraints().add(right);
        }
    }
}
