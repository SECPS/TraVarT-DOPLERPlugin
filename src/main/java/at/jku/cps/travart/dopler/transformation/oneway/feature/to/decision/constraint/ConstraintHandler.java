package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;
import at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf.DnfConverter;
import at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf.DnfConverterImpl;
import at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf.DnfSimplifier;
import at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf.DnfSimplifierImpl;
import com.google.common.collect.Lists;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ImplicationConstraint;
import de.vill.model.constraint.NotConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Adds the constraints from the feature model to the decision model.
 */
public class ConstraintHandler {

    private final DnfConverter dnfConverter;
    private final DnfSimplifier dnfSimplifier;
    private final ActionCreator actionCreator;
    private final ConditionCreator conditionCreator;

    public ConstraintHandler() {
        dnfConverter = new DnfConverterImpl();
        dnfSimplifier = new DnfSimplifierImpl();
        actionCreator = new ActionCreator();
        conditionCreator = new ConditionCreator();
    }

    public void handleOwnConstraints(FeatureModel featureModel, IDecisionModel decisionModel) {
        //Bring constraints into normalised form
        List<ImplicationConstraint> implicationConstraints = normaliseConstraints(featureModel);

        //Create rules from the implications
        List<Rule> rules = createRules(decisionModel, implicationConstraints);

        //Ann the created rules to the decisions
        distributeRules(rules);
    }

    /** Converts the constraints in a normalised form. The form follows this pattern: {@code !(A & ... & Z) => ALPHA} */
    private List<ImplicationConstraint> normaliseConstraints(FeatureModel featureModel) {
        List<ImplicationConstraint> implicationConstraints = new ArrayList<>();

        //Remove root ands from the constraints
        List<Constraint> sanitisedConstrains = removeAnds(featureModel);
        for (Constraint constraint : sanitisedConstrains) {
            List<List<Constraint>> dnf = dnfConverter.convertToDnf(constraint);

            if (1 == dnf.size()) { // Contains no OR
                //TODO
                throw new RuntimeException("DNF to short");
            } else { //Contains at least one OR
                List<Constraint> rightSide = dnf.get(dnf.size() - 1);
                List<List<Constraint>> leftSide = new ArrayList<>(dnf);
                leftSide.remove(rightSide);

                for (Constraint right : rightSide) {

                    //Remove double negation on the left side
                    Constraint left = new NotConstraint(dnfSimplifier.createDnfFromList(leftSide));
                    if (((NotConstraint) left).getContent() instanceof NotConstraint) {
                        left = ((NotConstraint) ((NotConstraint) left).getContent()).getContent();
                    }

                    implicationConstraints.add(new ImplicationConstraint(left, right));
                }
            }
        }
        return implicationConstraints;
    }

    private static List<Constraint> removeAnds(FeatureModel featureModel) {
        Stack<Constraint> stack = new Stack<>();
        featureModel.getConstraints().forEach(stack::push);
        List<Constraint> sanitisedConstrains = new ArrayList<>();
        while (!stack.empty()) {
            Constraint current = stack.pop();
            if (current instanceof AndConstraint) {
                AndConstraint andConstraint = (AndConstraint) current;
                stack.push(andConstraint.getLeft());
                stack.push(andConstraint.getRight());
            } else {
                sanitisedConstrains.add(current);
            }
        }
        sanitisedConstrains = Lists.reverse(sanitisedConstrains);
        return sanitisedConstrains;
    }

    private List<Rule> createRules(IDecisionModel decisionModel, List<ImplicationConstraint> implicationConstraints) {
        List<Rule> rules = new ArrayList<>();

        for (ImplicationConstraint implicationConstraint : implicationConstraints) {
            ICondition condition = conditionCreator.createCondition(decisionModel, implicationConstraint.getLeft());
            IAction action = actionCreator.createAction(decisionModel, implicationConstraint.getRight());
            rules.add(new Rule(condition, action));
        }

        return rules;
    }

    /** Distribute the generated rules to the decisions */
    private void distributeRules(List<Rule> rules) {
        for (Rule rule : rules) {
            rule.getAction().getVariable().addRule(rule);
        }
    }
}
