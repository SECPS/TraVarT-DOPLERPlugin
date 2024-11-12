package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.dnf.DnfConverter;
import at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.dnf.DnfSimplifier;
import at.jku.cps.travart.dopler.transformation.util.DMUtil;
import at.jku.cps.travart.dopler.transformation.util.DecisionNotPresentException;
import at.jku.cps.travart.dopler.transformation.util.UnexpectedTypeException;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adds the constraints from the feature model to the decision model.
 */
public class ConstraintHandler {

    /**
     * Temporary variable to save current decision model
     */
    private IDecisionModel decisionModel;

    /**
     * Temporary variable to save feature decision model
     */
    private FeatureModel featureModel;

    private final List<Matcher<?>> matchers;

    private final DnfConverter dnfConverter;
    private final DnfSimplifier dnfSimplifier;

    public ConstraintHandler() {
        decisionModel = null;
        featureModel = null;

        matchers = new ArrayList<>();

        //Order is important
        //matchers.add(new OrMatcher());
        matchers.add(new ImplicationMatcher());
        dnfConverter = new DnfConverter();
        dnfSimplifier = new DnfSimplifier();
    }

    public void handleOwnConstraints(FeatureModel featureModel, IDecisionModel decisionModel) {
        this.decisionModel = decisionModel;
        this.featureModel = featureModel;

        //Bring constraints into normalised form
        List<ImplicationConstraint> implicationConstraints = normaliseConstraints(featureModel);

        List<Rule> rules = createRules(implicationConstraints);

        distributeRules(rules);

        System.out.println(implicationConstraints);
    }

    /** Distribute the generated rules to the actions */
    private void distributeRules(List<Rule> rules) {
        for (Rule rule : rules) {
            rule.getAction().getVariable().addRule(rule);
        }
    }

    private List<Rule> createRules(List<ImplicationConstraint> implicationConstraints) {
        List<Rule> rules = new ArrayList<>();

        for (ImplicationConstraint implicationConstraint : implicationConstraints) {
            ICondition condition = createCondition(implicationConstraint.getLeft());
            IAction action = createAction(implicationConstraint.getRight());
            rules.add(new Rule(condition, action));
        }

        return rules;
    }

    private IAction createAction(Constraint right) {
        IAction action = null;

        if (right instanceof LiteralConstraint) {
            String literal = ((LiteralConstraint) right).getLiteral();
            Optional<IDecision<?>> decisionById = DMUtil.findDecisionById(decisionModel, literal);
            Optional<IDecision<?>> decisionByValue = DMUtil.findDecisionByValue(decisionModel, literal);

            if (decisionByValue.isPresent()) {
                action = new SetValueAction(decisionByValue.get(), new StringValue(literal));
            } else if (decisionById.isPresent()) {
                //Boolean types get a true on the right side
                boolean isBoolean = AbstractDecision.DecisionType.BOOLEAN == decisionById.get().getType();
                action = new SetValueAction(decisionById.get(),
                        isBoolean ? BooleanValue.getTrue() : new StringValue(literal));
            } else {
                throw new DecisionNotPresentException(literal);
            }
        } else if (right instanceof NotConstraint) {
            NotConstraint notConstraint = (NotConstraint) right;
            if (!(notConstraint.getContent() instanceof LiteralConstraint)) {
                throw new UnexpectedTypeException(notConstraint.getContent());
            }
            LiteralConstraint literalConstraint = (LiteralConstraint) notConstraint.getContent();

            String literal = literalConstraint.getLiteral();
            Optional<IDecision<?>> decisionByIdRight = DMUtil.findDecisionById(decisionModel, literal);
            Optional<IDecision<?>> decisionByValueRight = DMUtil.findDecisionByValue(decisionModel, literal);

            if (decisionByValueRight.isPresent()) {
                action = new DisAllowAction(decisionByValueRight.get(), new StringValue(literal));
            } else if (decisionByIdRight.isPresent()) {
                //Boolean types get a true on the right side
                action = AbstractDecision.DecisionType.BOOLEAN == decisionByIdRight.get().getType() ?
                        new DisAllowAction(decisionByIdRight.get(), BooleanValue.getTrue()) :
                        new DisAllowAction(decisionByIdRight.get(), new StringValue(literal));
            } else {
                throw new DecisionNotPresentException(literal);
            }
        } else if (right instanceof ExpressionConstraint) {
            //TODO
            throw new UnexpectedTypeException(right);
        } else {
            throw new UnexpectedTypeException(right);
        }

        return action;
    }

    private ICondition createCondition(Constraint left) {
        ICondition condition;
        if (left instanceof NotConstraint) {
            NotConstraint notConstraint = (NotConstraint) left;
            condition = new Not(createCondition(notConstraint.getContent()));
        } else if (left instanceof LiteralConstraint) {
            String literal = ((LiteralConstraint) left).getLiteral();
            Optional<IDecision<?>> decisionById = DMUtil.findDecisionById(decisionModel, literal);
            Optional<IDecision<?>> decisionByValue = DMUtil.findDecisionByValue(decisionModel, literal);

            if (decisionByValue.isPresent()) {
                condition = new DecisionValueCondition(decisionByValue.get(), new StringValue(literal));
            } else if (decisionById.isPresent()) {
                condition = new StringValue(literal);
            } else {
                throw new DecisionNotPresentException(literal);
            }
        } else if (left instanceof OrConstraint) {
            OrConstraint orConstraint = (OrConstraint) left;
            condition = new Or(createCondition(orConstraint.getLeft()), createCondition(orConstraint.getRight()));
        } else if (left instanceof AndConstraint) {
            AndConstraint andConstraint = (AndConstraint) left;
            condition = new And(createCondition(andConstraint.getLeft()), createCondition(andConstraint.getRight()));
        } else {
            throw new UnexpectedTypeException(left);
        }

        return condition;
    }

    /** Converts the constraints in a normalised form. The form follows this pattern: {@code !(A & ... & Z) => ALPHA} */
    private List<ImplicationConstraint> normaliseConstraints(FeatureModel featureModel) {
        List<ImplicationConstraint> implicationConstraints = new ArrayList<>();

        for (Constraint constraint : featureModel.getConstraints()) {
            List<List<Constraint>> dnf = dnfConverter.convertToDnf(constraint);

            if (1 == dnf.size()) { // Contains no OR
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
}
