/*******************************************************************************
 * TODO: explanation what the class does
 *  
 *  @author Kevin Feichtinger
 *  
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision.factory;

import at.jku.cps.travart.core.common.IFactory;
import at.jku.cps.travart.dopler.decision.impl.DecisionModel;
import at.jku.cps.travart.dopler.decision.model.AbstractRangeValue;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.AllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.And;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Cardinality;
import at.jku.cps.travart.dopler.decision.model.impl.DisAllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.EnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.GetValueFunction;
import at.jku.cps.travart.dopler.decision.model.impl.IsSelectedFunction;
import at.jku.cps.travart.dopler.decision.model.impl.IsTakenFunction;
import at.jku.cps.travart.dopler.decision.model.impl.Not;
import at.jku.cps.travart.dopler.decision.model.impl.NumberDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;
import at.jku.cps.travart.dopler.decision.model.impl.SetValueAction;
import at.jku.cps.travart.dopler.decision.model.impl.StringDecision;

public interface IDecisionModelFactory extends IFactory<DecisionModel> {

	BooleanDecision createBooleanDecision(String id);

	EnumerationDecision createEnumDecision(String id);

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

	Not createNotCondition(ICondition value);

	@SuppressWarnings("rawtypes")
	GetValueFunction createGetValueFunction(IDecision decision);

	ICondition createEquals(ICondition getValue, AbstractRangeValue<?> value);

	IAction createSelectDecisionAction(BooleanDecision decision);

	IAction createDeSelectDecisionAction(BooleanDecision decision);

	Rule createRule(ICondition condition, IAction action);

	DisAllowAction createDisAllowAction(final EnumerationDecision userManagement, final String value);

	AllowAction createAllowAction(final EnumerationDecision userManagement, final String value);

	SetValueAction setValueAction(EnumerationDecision userManagement, String value);
}
