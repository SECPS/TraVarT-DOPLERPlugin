package edu.kit.dopler.transformation.decision.to.feature.rules;

import com.google.inject.Inject;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ImplicationConstraint;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.model.Rule;

import java.util.Comparator;
import java.util.Optional;

/** Implementation of {@link RuleHandler}. */
public class RuleHandlerImpl implements RuleHandler {

    private final LeftCreator leftCreator;
    private final RightCreator rightCreator;

    @Inject
    public RuleHandlerImpl(LeftCreator leftCreator, RightCreator rightCreator) {
        this.leftCreator = leftCreator;
        this.rightCreator = rightCreator;
    }

    @Override
    public void handleRules(Dopler decisionModel, FeatureModel featureModel) {
        for (IDecision<?> decision : decisionModel.getDecisions()) {
            for (Rule rule : decision.getRules()) {
                handleRule(featureModel, rule);
            }
        }

        //Sort constraints
        featureModel.getOwnConstraints().sort(Comparator.comparing(constraint -> constraint.toString(true, "")));
    }

    private void handleRule(FeatureModel featureModel, Rule rule) {
        Optional<Constraint> left = leftCreator.handleCondition(rule.getCondition());
        Constraint right = rightCreator.createRight(featureModel.getRootFeature(), rule.getActions());

        if (left.isPresent()) {
            // constraint: 'left -> right'
            featureModel.getOwnConstraints().add(new ImplicationConstraint(left.get(), right));
        } else {
            // constraint: 'right'
            featureModel.getOwnConstraints().add(right);
        }
    }
}
