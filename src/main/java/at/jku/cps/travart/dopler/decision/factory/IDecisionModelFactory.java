package at.jku.cps.travart.dopler.decision.factory;

import at.jku.cps.travart.core.common.IFactory;
import at.jku.cps.travart.dopler.decision.impl.DecisionModel;
import at.jku.cps.travart.dopler.decision.model.ARangeValue;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.And;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Cardinality;
import at.jku.cps.travart.dopler.decision.model.impl.EnumDecision;
import at.jku.cps.travart.dopler.decision.model.impl.GetValueFunction;
import at.jku.cps.travart.dopler.decision.model.impl.IsSelectedFunction;
import at.jku.cps.travart.dopler.decision.model.impl.IsTakenFunction;
import at.jku.cps.travart.dopler.decision.model.impl.NumberDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;
import at.jku.cps.travart.dopler.decision.model.impl.StringDecision;

public interface IDecisionModelFactory extends IFactory<DecisionModel> {

	BooleanDecision createBooleanDecision(String id);

	EnumDecision createEnumDecision(String id);

	NumberDecision createNumberDecision(String id);

	StringDecision createStringDecision(String id);

	Range<String> createEnumValueOptions(String[] options);

	Range<Double> createNumberValueRange(String[] ranges);

	Cardinality createCardinality(int min, int max);

	@SuppressWarnings("rawtypes")
	IsTakenFunction createIsTakenFunction(IDecision decision);

	@SuppressWarnings("rawtypes")
	IsSelectedFunction createIsSelectedFunction(IDecision decision);

	And createAndCondition(ICondition left, ICondition right);

	@SuppressWarnings("rawtypes")
	GetValueFunction createGetValueFunction(IDecision decision);

	ICondition createEquals(ICondition getValue, ARangeValue<?> value);

	IAction createSelectDecisionAction(BooleanDecision decision);

	Rule createRule(ICondition condition, IAction action);
}
