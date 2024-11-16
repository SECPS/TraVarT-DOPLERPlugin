package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanValue;
import at.jku.cps.travart.dopler.decision.model.impl.DisAllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.SetValueAction;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;
import at.jku.cps.travart.dopler.transformation.util.DecisionNotPresentException;
import at.jku.cps.travart.dopler.transformation.util.MyUtil;
import at.jku.cps.travart.dopler.transformation.util.UnexpectedTypeException;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ExpressionConstraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;

import java.util.Optional;

class ActionCreatorImpl implements ActionCreator {

    @Override
    public IAction createAction(IDecisionModel decisionModel, FeatureModel featureModel, Constraint right) {
        return switch (right) {
            case LiteralConstraint literalConstraint -> handleLiteral(decisionModel, featureModel, literalConstraint);
            case NotConstraint notConstraint -> handleNot(decisionModel, featureModel, notConstraint);
            case ExpressionConstraint expressionConstraint -> {
                //TODO
                throw new UnexpectedTypeException(right);
            }
            case null, default -> throw new UnexpectedTypeException(right);
        };
    }

    private static IAction handleLiteral(IDecisionModel decisionModel, FeatureModel featureModel,
                                         LiteralConstraint literalConstraint) {
        IAction action;
        Optional<LiteralConstraint> firstNonMandatoryParent =
                MyUtil.findFirstNonMandatoryParent(featureModel, literalConstraint);
        if (firstNonMandatoryParent.isEmpty()) {
            //TODO Hier müsste eine Aktion hinkommen, die immer wahr ist.
            throw new RuntimeException("Could not find non mandatory parent to replace action");
        } else {
            literalConstraint = firstNonMandatoryParent.get();
        }

        String literal = literalConstraint.getLiteral();
        Optional<IDecision<?>> decisionById = MyUtil.findDecisionById(decisionModel, literal);
        Optional<IDecision<?>> decisionByValue = MyUtil.findDecisionByValue(decisionModel, literal);

        if (decisionByValue.isPresent()) {
            action = new SetValueAction(decisionByValue.get(), new StringValue(literal));
        } else if (decisionById.isPresent()) {
            action = switch (decisionById.get().getType()) {
                case BOOLEAN -> new SetValueAction(decisionById.get(), BooleanValue.getTrue());
                case ENUM -> new SetValueAction(decisionById.get(), new StringValue(literal));
                case null, default -> throw new UnexpectedTypeException(decisionById.get().getType());
            };
        } else {
            throw new DecisionNotPresentException(literal);
        }

        return action;
    }

    private static IAction handleNot(IDecisionModel decisionModel, FeatureModel featureModel,
                                     NotConstraint notConstraint) {
        IAction action;

        //NotConstraint should contain a literal (thanks to working on DNFs)
        if (!(notConstraint.getContent() instanceof LiteralConstraint literalConstraint)) {
            throw new UnexpectedTypeException(notConstraint.getContent());
        }

        Optional<LiteralConstraint> firstNonMandatoryParent =
                MyUtil.findFirstNonMandatoryParent(featureModel, literalConstraint);
        if (firstNonMandatoryParent.isEmpty()) {
            //TODO Hier müsste eine Aktion hinkommen, die immer falsch ist.
            throw new RuntimeException("Could not find non mandatory parent to replace action");
        } else {
            literalConstraint = firstNonMandatoryParent.get();
        }

        String literal = literalConstraint.getLiteral();
        Optional<IDecision<?>> decisionById = MyUtil.findDecisionById(decisionModel, literal);
        Optional<IDecision<?>> decisionByValue = MyUtil.findDecisionByValue(decisionModel, literal);

        if (decisionByValue.isPresent()) {
            action = new DisAllowAction(decisionByValue.get(), new StringValue(literal));
        } else if (decisionById.isPresent()) {
            action = switch (decisionById.get().getType()) {
                case BOOLEAN -> new SetValueAction(decisionById.get(), BooleanValue.getFalse());
                case ENUM -> throw new RuntimeException("Can not disallow taking complete enum decisions!");
                case null, default -> throw new UnexpectedTypeException(decisionById.get().getType());

                //TODO
                //Problem in branch ENUM: there is no action to disallow taking enum decisions. Only boolean decisions.
                //Should look something like:
                //action = new DisallowAction(decisionById.get())
            };
        } else {
            throw new DecisionNotPresentException(literal);
        }
        return action;
    }
}
