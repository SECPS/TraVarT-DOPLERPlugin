/*******************************************************************************
 * TODO: explanation what the class does
 *  
 *  @author Kevin Feichtinger
 *  
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.transformation.old;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.logicng.formulas.FormulaFactory;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.core.helpers.TraVarTUtils;
import at.jku.cps.travart.core.transformation.DefaultModelTransformationProperties;
import at.jku.cps.travart.dopler.common.DecisionModelUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.CircleInConditionException;
import at.jku.cps.travart.dopler.decision.exc.ConditionCreationException;
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.factory.impl.DecisionModelFactory;
import at.jku.cps.travart.dopler.decision.impl.DecisionModel;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision;
import at.jku.cps.travart.dopler.decision.model.AbstractRangeValue;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.AllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.And;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Cardinality;
import at.jku.cps.travart.dopler.decision.model.impl.DeSelectDecisionAction;
import at.jku.cps.travart.dopler.decision.model.impl.DecisionValueCondition;
import at.jku.cps.travart.dopler.decision.model.impl.DisAllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.EnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.IsSelectedFunction;
import at.jku.cps.travart.dopler.decision.model.impl.Not;
import at.jku.cps.travart.dopler.decision.model.impl.Or;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;
import at.jku.cps.travart.dopler.decision.model.impl.SelectDecisionAction;
import at.jku.cps.travart.dopler.decision.model.impl.SetValueAction;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;
import at.jku.cps.travart.dopler.decision.parser.ConditionParser;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group.GroupType;
import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;
import de.vill.model.constraint.OrConstraint;
import de.vill.model.constraint.ParenthesisConstraint;

@SuppressWarnings("rawtypes")
public abstract class TransformFMtoDMUtil {

	public static void convertFeature(final DecisionModelFactory factory, final IDecisionModel dm,
			final Feature feature) throws NotSupportedVariabilityTypeException {
		if (!feature.getFeatureName().equals(DefaultModelTransformationProperties.ARTIFICIAL_MODEL_NAME)) {
			BooleanDecision decision = factory.createBooleanDecision(TraVarTUtils.getFeatureName(feature));
			dm.add(decision);
			decision.setQuestion(feature.getFeatureName() + "?");
			if (TraVarTUtils.isEnumerationType(feature)) {
				createEnumDecisions(factory, dm, feature);
			}
			if (feature.getParentFeature() == null) {
				decision.setVisibility(ICondition.TRUE);
			} else if (isEnumSubFeature(feature) && !hasVirtualParent(feature)) {
				decision.setVisibility(ICondition.FALSE);
			} else if (feature.getParentGroup().GROUPTYPE.equals(GroupType.MANDATORY) && !hasVirtualParent(feature)) {
				String parentName = TraVarTUtils.getFeatureName(TraVarTUtils.getParentFeature(feature));
				// as tree traversal, the parent should be dealt with already
				IDecision parent = dm.get(parentName);
				assert parent != null;
				// funky behaviour due to roundtrip compatibility - don't change
				Rule rule = new Rule(new IsSelectedFunction(parent), new SelectDecisionAction(decision));
				Rule ruleReverse = new Rule(new IsSelectedFunction(decision), new SelectDecisionAction(parent));
				decision.addRule(ruleReverse);
				updateRules(decision, ruleReverse);
				parent.addRule(rule);
				updateRules(parent, rule);
				decision.setVisibility(new And(ICondition.FALSE, new IsSelectedFunction(parent)));
			} else if (!hasVirtualParent(feature)) {
				Feature parentFeature = TraVarTUtils.getParentFeature(feature);
				Objects.requireNonNull(parentFeature);
				IDecision parent = dm.get(TraVarTUtils.getFeatureName(parentFeature));
				decision.setVisibility(new IsSelectedFunction(parent));
			} else {
				decision.setVisibility(ICondition.TRUE);
			}
		}
		for (Feature child : feature.getChildren().stream().flatMap(g -> g.getFeatures().stream())
				.collect(Collectors.toList())) {
			if (feature.getFeatureName().equals(DefaultModelTransformationProperties.ARTIFICIAL_MODEL_NAME)
					|| !isEnumSubFeature(child)) {
				convertFeature(factory, dm, child);
			}
		}
	}

	public static boolean hasVirtualParent(final Feature feature) {
		Feature parent = feature.getParentFeature();
		if (parent != null) {
			return DefaultModelTransformationProperties.ARTIFICIAL_MODEL_NAME
					.equals(TraVarTUtils.getFeatureName(parent));
		}
		return false;
	}

	public static void createEnumDecisions(final DecisionModelFactory factory, final IDecisionModel dm,
			final Feature feature) throws NotSupportedVariabilityTypeException {
		// in FeatureIDE only one feature group is possible per feature
		EnumerationDecision enumDecision = factory.createEnumDecision(TraVarTUtils.getFeatureName(feature) + "#0");
		dm.add(enumDecision);
		enumDecision.setQuestion("Choose your " + TraVarTUtils.getFeatureName(feature) + "?");
		defineCardinality(enumDecision, feature);
		defineRange(enumDecision, feature);
		IDecision parent = dm.get(TraVarTUtils.getFeatureName(feature));
		enumDecision.setVisibility(new IsSelectedFunction(parent));
//		 add rule from parent to enum decision so when you select the boolean one it
//		 has to be selected as well --> done with visiblity condition not with a rule
//		Rule rule = new Rule(new IsSelectedFunction(parent), new SelectDecisionAction(enumDecision));
//		parent.addRule(rule);
//		updateRules(parent, rule);
		// TODO check how to handle Groups here
		for (Feature optionFeature : feature.getChildren().stream().flatMap(g -> g.getFeatures().stream())
				.collect(Collectors.toList())) {
			convertFeature(factory, dm, optionFeature);
			BooleanDecision optionDecision = (BooleanDecision) dm.get(optionFeature.getFeatureName());
			AbstractRangeValue optionValue = enumDecision.getRangeValue(TraVarTUtils.getFeatureName(optionFeature));
			// as tree traversal, the parent should be dealt with already
			assert optionDecision != null;
			Rule rule = new Rule(new DecisionValueCondition(enumDecision, optionValue),
					new SelectDecisionAction(optionDecision));
			enumDecision.addRule(rule);
			updateRules(enumDecision, rule);
		}
		// create rule for optional parent features
		if (feature.getParentFeature() != null && !feature.getParentGroup().GROUPTYPE.equals(GroupType.MANDATORY)) {
			Rule rule = new Rule(new Not(new IsSelectedFunction(parent)),
					new SetValueAction(enumDecision, enumDecision.getNoneOption()));
			parent.addRule(rule);
			updateRules(parent, rule);
		}
	}

	public static void convertConstraints(final DecisionModelFactory factory, final IDecisionModel dm,
			final FeatureModel fm, final List<Constraint> constraints)
			throws CircleInConditionException, ConditionCreationException, ReflectiveOperationException {
		for (Constraint constraint : constraints) {
			convertConstraintRec(factory, dm, fm, constraint);
		}
	}

	public static void convertConstraintRec(final DecisionModelFactory factory, final IDecisionModel dm,
			final FeatureModel fm, final Constraint node)
			throws CircleInConditionException, ConditionCreationException, ReflectiveOperationException {
		// create a CNF from nodes enables separating the concerns how to transform the
		// different groups.
		// A requires B <=> CNF: Not(A) or B
		// A excludes B <=> CNF: Not(A) or Not(B)

		Constraint cnfConstraint = TraVarTUtils
				.buildConstraintFromFormula(TraVarTUtils.buildFormulaFromConstraint(node, new FormulaFactory()).cnf());
		if (TraVarTUtils.isComplexConstraint(cnfConstraint)) {
			for (Constraint child : cnfConstraint.getConstraintSubParts()) {
				convertConstraintRec(factory, dm, fm, child);
			}
		} else {
			convertConstraint(factory, dm, fm, cnfConstraint);
		}
	}

	/**
	 * converts a given constraint in CNF-Form from a given feature model over to an
	 * equivalent representation in the given Decision Model. This new
	 * representation may entail additional decisions, rules and visibility
	 * conditions.
	 *
	 * @param factory       the factory with which the decision model is built
	 * @param dm            the decision model
	 * @param fm            the feature model
	 * @param cnfConstraint the constraint in CNF form
	 * @throws ConditionCreationException   when no translation could be found for
	 *                                      the constraint or an error happened in
	 *                                      the translation
	 * @throws ReflectiveOperationException
	 */
	public static void convertConstraint(final DecisionModelFactory factory, final IDecisionModel dm,
			final FeatureModel fm, final Constraint cnfConstraint)
			throws ConditionCreationException, ReflectiveOperationException {
		Constraint parenthesisLessConstraint = cnfConstraint instanceof ParenthesisConstraint
				? ((ParenthesisConstraint) cnfConstraint).getContent()
				: cnfConstraint;
		if (TraVarTUtils.countLiterals(parenthesisLessConstraint) >= 1
				&& TraVarTUtils.countNegativeLiterals(parenthesisLessConstraint) == 0
				&& (parenthesisLessConstraint instanceof AndConstraint
						|| parenthesisLessConstraint instanceof LiteralConstraint)) {
			// mandatory from root - requires rule
			deriveMandatoryRules(dm, fm, parenthesisLessConstraint);
		} else if (TraVarTUtils.isNegativeLiteral(parenthesisLessConstraint)) {
			// excludes from root - dead feature - excludes rule
			deriveDeadFeatureRules(dm, fm, parenthesisLessConstraint);
		} else if (TraVarTUtils.isRequires(parenthesisLessConstraint)) {
			deriveRequiresRules(dm, fm, parenthesisLessConstraint);
		} else if (TraVarTUtils.isExcludes(parenthesisLessConstraint)) {
			// FIXME does not handle multi-excludes ?!?!
			deriveExcludeRules(dm, fm, parenthesisLessConstraint);
		} else if (TraVarTUtils.isRequiredForAllConstraint(parenthesisLessConstraint)) {
			deriveRequiredForAllRule(dm, parenthesisLessConstraint);
		} else if (TraVarTUtils.countLiterals(parenthesisLessConstraint) > 1
				&& TraVarTUtils.countNegativeLiterals(parenthesisLessConstraint) == 0
				&& parenthesisLessConstraint instanceof OrConstraint) {
			deriveOrSelectionRules(factory, dm, fm, parenthesisLessConstraint);
		} else {
			// any other type of mode
			// create demorgen condition node 1:1
			ICondition condition = deriveConditionFromConstraint(dm, parenthesisLessConstraint);
			// create implies with root decision to set to false
			BooleanDecision rootDecision = (BooleanDecision) dm
					.get(TraVarTUtils.getFeatureName(TraVarTUtils.getRoot(fm)));
			Rule rule = new Rule(new Not(condition), new DeSelectDecisionAction(rootDecision));
			rootDecision.addRule(rule);
			updateRules(rootDecision, rule);
		}
	}

	/**
	 * Part of the convertConstraint-process. DO NOT USE AS-IS. Requires passed
	 * constraint to be of form: !A . Creates rules for mandatory features in the
	 * decision model.
	 *
	 * @param dm         the decision model
	 * @param fm         the feature model
	 * @param constraint The filtered out constraint of special format
	 */
	private static void deriveDeadFeatureRules(final IDecisionModel dm, final FeatureModel fm,
			final Constraint constraint) {
		Feature root = fm.getFeatureMap().get(TraVarTUtils.deriveFeatureModelRoot(fm, "virtual root"));
		NotConstraint targetLiteral = (NotConstraint) TraVarTUtils.getFirstNegativeLiteral(constraint);
		String targetFeature = ((LiteralConstraint) targetLiteral.getContent()).getLiteral();
		IDecision source = dm.get(TraVarTUtils.getFeatureName(root));
		IDecision target = dm.get(targetFeature);
		Rule rule = new Rule(new IsSelectedFunction(source), new DeSelectDecisionAction((BooleanDecision) target));
		source.addRule(rule);
		updateRules(source, rule);
	}

	/**
	 * Part of the convertConstraint-process. DO NOT USE AS-IS. Requires passed
	 * constraint to be of form: A or B or C or.... creates rules for mandatory
	 * features in the decision model.
	 *
	 * @param dm         the decision model
	 * @param fm         the feature model
	 * @param constraint The filtered out constraint of special format
	 */
	private static void deriveOrSelectionRules(final DecisionModelFactory factory, final IDecisionModel dm,
			final FeatureModel fm, final Constraint constraint) {
		Set<Constraint> literals = TraVarTUtils.getLiterals(constraint);
		List<IDecision> literalDecisions = findDecisionsForLiterals(dm, literals);
		EnumerationDecision enumDecision = factory
				.createEnumDecision(String.format("or#constr#%s", literalDecisions.size()));
		dm.add(enumDecision);
		enumDecision.setQuestion("Which features should be added? Pick at least one!");
		enumDecision.setCardinality(new Cardinality(1, literalDecisions.size()));
		enumDecision.setRange(new Range<>());
		IDecision rootDecision = dm.get(TraVarTUtils.getFeatureName(TraVarTUtils.getRoot(fm)));
		enumDecision.setVisibility(rootDecision != null ? new IsSelectedFunction(rootDecision) : ICondition.TRUE);
		for (IDecision literalDecision : literalDecisions) {
			StringValue option = new StringValue(literalDecision.getId());
			enumDecision.getRange().add(option);
			BooleanDecision optionDecision = (BooleanDecision) dm.get(literalDecision.getId());
			Rule rule = new Rule(new DecisionValueCondition(enumDecision, option),
					new SelectDecisionAction(optionDecision));
			enumDecision.addRule(rule);
			updateRules(enumDecision, rule);
		}
	}

	/**
	 * Part of the convertConstraint-process. DO NOT USE AS-IS. Requires passed
	 * constraint to be of form: A and B and C and.... creates rules for mandatory
	 * features in the decision model.
	 *
	 * @param dm         the decision model
	 * @param fm         the feature model
	 * @param constraint The filtered out constraint of special format
	 */
	private static void deriveMandatoryRules(final IDecisionModel dm, final FeatureModel fm,
			final Constraint constraint) {
		for (Constraint literal : TraVarTUtils.getLiterals(constraint)) {
			Feature root = fm.getFeatureMap().get(TraVarTUtils.deriveFeatureModelRoot(fm, "virtual root"));
			Constraint targetLiteral = TraVarTUtils.getFirstPositiveLiteral(literal);
			String targetFeature = ((LiteralConstraint) targetLiteral).getLiteral();
			IDecision source = dm.get(TraVarTUtils.getFeatureName(root));
			IDecision target = dm.get(targetFeature);
			Rule rule = new Rule(new IsSelectedFunction(source), new SelectDecisionAction(target));
			source.addRule(rule);
			updateRules(source, rule);
		}
	}

	/**
	 * Part of the convertConstraint-process. DO NOT USE AS-IS. Requires passed
	 * constraint to be of form: !A or !B or ... or C . creates required-for-all
	 * rules in the decision model.
	 *
	 * @param dm         the decision model
	 * @param constraint the constraint in special form
	 * @throws ConditionCreationException if an error occurs while creating
	 *                                    sub-rules
	 */
	private static void deriveRequiredForAllRule(final IDecisionModel dm, final Constraint constraint)
			throws ConditionCreationException {
		Constraint positive = TraVarTUtils.getFirstPositiveLiteral(constraint);
		String feature = ((LiteralConstraint) positive).getLiteral();
		IDecision decision = dm.get(feature);
		if (decision.getType() == AbstractDecision.DecisionType.BOOLEAN) {
			BooleanDecision target = (BooleanDecision) decision;
			Set<Constraint> negativeLiterals = TraVarTUtils.getNegativeLiterals(constraint);
			List<IDecision> conditionDecisions = findDecisionsForLiterals(dm, negativeLiterals);
			List<IDecision> ruleDecisions = findDecisionsForLiterals(dm, negativeLiterals);
			ICondition ruleCondition = DecisionModelUtils.consumeToBinaryCondition(conditionDecisions, And.class,
					false);
			Rule rule = new Rule(ruleCondition, new SelectDecisionAction(target));
			// add new rule to all rule decisions
			for (IDecision source : ruleDecisions) {
				source.addRule(rule);
				updateRules(source, rule);
			}
		}
	}

	/**
	 * Part of the convertConstraint-process. DO NOT USE AS-IS. Requires passed
	 * constraint to be of form: !A or B or C ... . creates required rules in the
	 * decision model.
	 *
	 * @param dm            the decision model
	 * @param fm            the feature model
	 * @param cnfConstraint the constraint in the required format
	 */
	private static void deriveRequiresRules(final IDecisionModel dm, final FeatureModel fm,
			final Constraint cnfConstraint) {
		NotConstraint sourceLiteral = (NotConstraint) TraVarTUtils.getFirstNegativeLiteral(cnfConstraint);
		Constraint targetLiteral = TraVarTUtils.getFirstPositiveLiteral(cnfConstraint);
		if (TraVarTUtils.isLiteral(sourceLiteral) && TraVarTUtils.isLiteral(targetLiteral)) {
			String sourceFeature = ((LiteralConstraint) sourceLiteral.getContent()).getLiteral();
			String targetFeature = ((LiteralConstraint) targetLiteral).getLiteral();
			IDecision source = dm.get(sourceFeature);
			IDecision target = dm.get(targetFeature);
			if (isEnumFeature(fm, source) && !isEnumFeature(fm, target)) {
				EnumerationDecision enumDecision = findEnumDecisionWithVisiblityCondition(dm, source);
				if (enumDecision.hasNoneOption()) {
					Rule rule = new Rule(
							new Not(new DecisionValueCondition(enumDecision, enumDecision.getNoneOption())),
							new SelectDecisionAction(target));
					enumDecision.addRule(rule);
					updateRules(enumDecision, rule);
				} else if (hasOptionalParent(fm, sourceFeature) && target.getType() == AbstractDecision.DecisionType.BOOLEAN) {
					// search the boolean parent decision for the enum and then set the rule there
					IDecision parent = ((IsSelectedFunction) enumDecision.getVisiblity()).getParameters().get(0);
					Rule rule = new Rule(new IsSelectedFunction(parent), new SelectDecisionAction(target));
					source.addRule(rule);
					updateRules(source, rule);
				}
			} else if (!isEnumFeature(fm, source) && isEnumFeature(fm, target)) {
				// the none value of the enum feature if available must be disallowed
				EnumerationDecision enumDecision = findEnumDecisionWithVisiblityCondition(dm, target);
				if (enumDecision.hasNoneOption()) {
					Rule rule = new Rule(new IsSelectedFunction(source),
							new DisAllowAction(enumDecision, enumDecision.getNoneOption()));
					source.addRule(rule);
					updateRules(source, rule);
				}
				Rule rule = new Rule(new IsSelectedFunction(source), new SelectDecisionAction(target));
				source.addRule(rule);
				updateRules(source, rule);

			} else if (!hasVirtualParent(fm, target) && isEnumSubFeature(fm, target)) {
				EnumerationDecision enumDecision = findEnumDecisionByRangeValue(dm, target);
				AbstractRangeValue value = enumDecision.getRangeValue(targetFeature);
				Rule rule = new Rule(new IsSelectedFunction(source), new SetValueAction(enumDecision, value));
				source.addRule(rule);
				updateRules(source, rule);
			} else if (target.getType() == AbstractDecision.DecisionType.BOOLEAN) {
				Rule rule = new Rule(new IsSelectedFunction(source), new SelectDecisionAction(target));
				source.addRule(rule);
				updateRules(source, rule);
			}
		}
	}

	public static boolean hasOptionalParent(final FeatureModel fm, final String featureName) {
		Feature feature = fm.getFeatureMap().get(featureName);
		if (feature == null) {
			return false;
		}
		Feature parent = feature.getParentFeature();
		while (parent != null && parent.getParentFeature() != null) {
			if (!parent.getParentGroup().GROUPTYPE.equals(GroupType.MANDATORY)) {
				return true;
			}
			parent = parent.getParentFeature();
		}
		return false;
	}

	public static boolean hasVirtualParent(final FeatureModel fm, final IDecision decision) {
		Feature feature = fm.getFeatureMap().get(
				DecisionModelUtils.retriveFeatureName(decision, decision.getType() == AbstractDecision.DecisionType.BOOLEAN));
		Feature parent = feature.getParentFeature();
		if (parent == null) {
			return false;
		}
		return DefaultModelTransformationProperties.ARTIFICIAL_MODEL_NAME.equals(TraVarTUtils.getFeatureName(parent));
	}

	public static void deriveExcludeRules(final IDecisionModel dm, final FeatureModel fm,
			final Constraint cnfConstraint) throws ReflectiveOperationException {
		// excludes constraints are bidirectional by default, therefore direction for
		// defined constraint is negligible, but we need two rules

		Constraint sourceLiteral = TraVarTUtils.getLeftConstraint(cnfConstraint);
		Constraint targetLiteral = TraVarTUtils.getRightConstraint(cnfConstraint);
		if (TraVarTUtils.isLiteral(sourceLiteral) && TraVarTUtils.isLiteral(targetLiteral)) {
			String sourceFeature = sourceLiteral instanceof NotConstraint
					? ((LiteralConstraint) ((NotConstraint) sourceLiteral).getContent()).getLiteral()
					: ((LiteralConstraint) sourceLiteral).getLiteral();
			String targetFeature = targetLiteral instanceof NotConstraint
					? ((LiteralConstraint) ((NotConstraint) targetLiteral).getContent()).getLiteral()
					: ((LiteralConstraint) targetLiteral).getLiteral();
			IDecision source = dm.get(sourceFeature);
			IDecision target = dm.get(targetFeature);
			if (isEnumSubFeature(fm, source) && !isEnumSubFeature(fm, target)) {
				// if either of the decisions is a enum sub feature the value for the
				// enumeration decision must be disallowed
				Rule rule = new Rule(new IsSelectedFunction(source),
						new DeSelectDecisionAction((BooleanDecision) target));
				source.addRule(rule);
				updateRules(source, rule);
				EnumerationDecision enumDecision = findEnumDecisionByRangeValue(dm, source);
				AbstractRangeValue value = enumDecision.getRangeValue(sourceFeature);
				rule = new Rule(new IsSelectedFunction(target), new DisAllowAction(enumDecision, value));
				target.addRule(rule);
				updateRules(target, rule);
			} else if (!isEnumSubFeature(fm, source) && isEnumSubFeature(fm, target)) {
				EnumerationDecision enumDecision = findEnumDecisionByRangeValue(dm, target);
				AbstractRangeValue value = enumDecision.getRangeValue(targetFeature);
				Rule rule = new Rule(new IsSelectedFunction(source), new DisAllowAction(enumDecision, value));
				source.addRule(rule);
				updateRules(target, rule);
				rule = new Rule(new IsSelectedFunction(target), new DeSelectDecisionAction((BooleanDecision) source));
				target.addRule(rule);
				updateRules(target, rule);
			} else if (isEnumSubFeature(fm, source) && isEnumSubFeature(fm, target)) {
				EnumerationDecision sourceEnumDecision = findEnumDecisionByRangeValue(dm, source);
				EnumerationDecision targetEnumDecision = findEnumDecisionByRangeValue(dm, target);
				AbstractRangeValue sourceValue = sourceEnumDecision.getRangeValue(sourceFeature);
				AbstractRangeValue targetValue = targetEnumDecision.getRangeValue(targetFeature);
				Rule rule = new Rule(new IsSelectedFunction(source),
						new DisAllowAction(targetEnumDecision, targetValue));
				source.addRule(rule);
				updateRules(source, rule);
				rule = new Rule(new IsSelectedFunction(target), new DisAllowAction(sourceEnumDecision, sourceValue));
				target.addRule(rule);
				updateRules(target, rule);
			} else if (source.getType() == AbstractDecision.DecisionType.BOOLEAN
					&& target.getType() == AbstractDecision.DecisionType.BOOLEAN) {
				Rule rule = new Rule(new IsSelectedFunction(source),
						new DeSelectDecisionAction((BooleanDecision) target));
				source.addRule(rule);
				updateRules(source, rule);
				rule = new Rule(new IsSelectedFunction(target), new DeSelectDecisionAction((BooleanDecision) source));
				target.addRule(rule);
				updateRules(target, rule);
			}
		}
	}

	public static ICondition deriveConditionFromConstraint(final IDecisionModel dm, final Constraint node)
			throws ConditionCreationException {
		Constraint workingConstraint = node;
		while (node instanceof ParenthesisConstraint) {
			workingConstraint = ((ParenthesisConstraint) workingConstraint).getContent();
		}
		if (workingConstraint instanceof AndConstraint) {
			List<Constraint> workingConstraints = new ArrayList<>(workingConstraint.getConstraintSubParts());
			ICondition first = deriveConditionFromConstraint(dm, workingConstraints.remove(0));
			ICondition second = deriveConditionFromConstraint(dm, workingConstraints.remove(0));
			And and = new And(first, second);
			while (!workingConstraints.isEmpty()) {
				ICondition next = deriveConditionFromConstraint(dm, workingConstraints.remove(0));
				and = new And(and, next);
			}
			return and;
		}
		if (workingConstraint instanceof OrConstraint) {
			List<Constraint> workingConstraints = new ArrayList<>(workingConstraint.getConstraintSubParts());
			ICondition first = deriveConditionFromConstraint(dm, workingConstraints.remove(0));
			ICondition second = deriveConditionFromConstraint(dm, workingConstraints.remove(0));
			Or or = new Or(first, second);
			while (!workingConstraints.isEmpty()) {
				ICondition next = deriveConditionFromConstraint(dm, workingConstraints.remove(0));
				or = new Or(or, next);
			}
			return or;
		}
		if (TraVarTUtils.isLiteral(workingConstraint)) {
			IDecision decision = dm.get(((LiteralConstraint) workingConstraint).getLiteral());
			IsSelectedFunction function = new IsSelectedFunction(decision);
			return TraVarTUtils.isNegativeLiteral(workingConstraint) ? new Not(function) : function;
		}
		throw new ConditionCreationException(String.format("Not supported condition type: %s", workingConstraint));
	}

	public static EnumerationDecision findEnumDecisionWithVisiblityCondition(final IDecisionModel dm,
			final IDecision decision) {
		// TODO: order causes issues; in one model (financial services) it is needed the
		// other way around
		// TODO: in another model as it is right now (Ubuntu_14_1)
		// TODO: which is the best way to find out
		List<EnumerationDecision> decisions = ((DecisionModel) dm).findWithVisibility(decision).stream()
				.filter(d -> (d.getType() == AbstractDecision.DecisionType.ENUM)).map(d -> (EnumerationDecision) d)
				.collect(Collectors.toList());
		if (!decisions.isEmpty()) {
			return decisions.remove(0);
		}
		return null;
	}

	public static EnumerationDecision findEnumDecisionByRangeValue(final IDecisionModel dm, final IDecision decision) {
		List<EnumerationDecision> decisions = new ArrayList<>(((DecisionModel) dm).findWithRangeValue(decision));
		if (!decisions.isEmpty()) {
			return decisions.remove(0);
		}
		return null;
	}

	public static List<IDecision> findDecisionsForLiterals(final IDecisionModel dm, final Set<Constraint> literals) {
		List<IDecision> literalDecisions = new ArrayList<>();
		for (Constraint positive : literals) {
			String name = ((LiteralConstraint) positive).getLiteral();
			IDecision d = dm.get(name);
			if (d != null) {
				literalDecisions.add(d);
			}
		}
		return literalDecisions;
	}

	public static void updateRules(final IDecision decision, final Rule rule) {
		// Invert the created rule and add it
		invertRule(decision, rule);
		// if the rule contains a none option also disallow it and invert it again for
		// the enumeration
		if (DecisionModelUtils.isNoneAction(rule.getAction())) {
			EnumerationDecision enumDecision = (EnumerationDecision) rule.getAction().getVariable();
			AbstractRangeValue rangeValue = (AbstractRangeValue) rule.getAction().getValue();
			Rule r = new Rule(new IsSelectedFunction(decision), new DisAllowAction(enumDecision, rangeValue));
			decision.addRule(r);
			// and again invert the added rule
			invertRule(decision, r);
		}
	}

	public static void invertRule(final IDecision decision, final Rule rule) {
		if (rule.getAction() instanceof DisAllowAction) {
			DisAllowAction function = (DisAllowAction) rule.getAction();
			IAction allow = new AllowAction(function.getVariable(), (AbstractRangeValue) function.getValue());
			ICondition condition = invertCondition(rule.getCondition());
			Rule r = new Rule(condition, allow);
			decision.addRule(r);
		} else if (rule.getAction() instanceof SelectDecisionAction && decision.getType() == AbstractDecision.DecisionType.ENUM
				&& ((EnumerationDecision) decision).getCardinality().isAlternative()
				&& !DecisionModelUtils.isNoneCondition(rule.getCondition())) {
			SelectDecisionAction select = (SelectDecisionAction) rule.getAction();
			ICondition condition = invertCondition(rule.getCondition());
			IAction deselect = new DeSelectDecisionAction((BooleanDecision) select.getVariable());
			Rule r = new Rule(condition, deselect);
			decision.addRule(r);
		}
	}

	public static ICondition invertCondition(final ICondition condition) {
		if (condition instanceof Not) {
			return ((Not) condition).getOperand();
		}
		return new Not(condition);
	}

	public static boolean isEnumSubFeature(final Feature feature) {
		// works as each tree in FeatureIDE has only one cardinality across all
		// sub-features.
		Feature parent = feature.getParentFeature();
		return parent != null && TraVarTUtils.isEnumerationType(parent);
	}

	public static boolean isEnumSubFeature(final FeatureModel fm, final IDecision decision) {
		Feature feature = fm.getFeatureMap().get(
				DecisionModelUtils.retriveFeatureName(decision, decision.getType() == AbstractDecision.DecisionType.ENUM));
		return isEnumSubFeature(feature);
	}

	public static boolean isEnumFeature(final FeatureModel fm, final IDecision decision) {
		Feature feature = fm.getFeatureMap().get(
				DecisionModelUtils.retriveFeatureName(decision, decision.getType() == AbstractDecision.DecisionType.ENUM));
		return TraVarTUtils.isEnumerationType(feature);
	}

	public static void defineCardinality(final EnumerationDecision decision, final Feature feature) {
		Cardinality cardinality = null;
		// TODO probably needs larger refactor due to group cardinalities
		if (feature.getChildren().stream().anyMatch(g -> g.GROUPTYPE.equals(GroupType.OR))) {
			cardinality = new Cardinality(1, feature.getChildren().stream().flatMap(g -> g.getFeatures().stream())
					.collect(Collectors.toList()).size());
		} else if (feature.getChildren().stream().anyMatch(g -> g.GROUPTYPE.equals(GroupType.ALTERNATIVE))) {
			cardinality = new Cardinality(1, 1);
		} else {
			cardinality = new Cardinality(0, feature.getChildren().stream().flatMap(g -> g.getFeatures().stream())
					.collect(Collectors.toList()).size());
		}
		decision.setCardinality(cardinality);
	}

	public static void defineRange(final EnumerationDecision decision, final Feature feature)
			throws NotSupportedVariabilityTypeException {
		Range<String> range = new Range<>();
		for (Feature optionFeature : feature.getChildren().stream().flatMap(g -> g.getFeatures().stream())
				.collect(Collectors.toList())) {
			StringValue option = new StringValue(optionFeature.getFeatureName());
			range.add(option);
		}
		decision.setRange(range);
		// add non option if it comes from a enum decision
		if (feature.getParentGroup() != null
				&& (!feature.getParentGroup().GROUPTYPE.equals(GroupType.MANDATORY) || isEnumSubFeature(feature))) {
			AbstractRangeValue noneOption = decision.getNoneOption();
			noneOption.enable();
			decision.getRange().add(noneOption);
			try {
				decision.setValue((String) noneOption.getValue());
			} catch (RangeValueException e) {
				throw new NotSupportedVariabilityTypeException(e);
			}
		}
	}

	public static void convertVisibilityCustomProperties(final IDecisionModel dm, final Collection<Feature> features) {
		for (Feature feature : features) {
			if (feature.getAttributes()
					.containsKey(DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY)) {// ,DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY_TYPE))
																											// {
				ConditionParser conditionParser = new ConditionParser(dm);
				String visbility = feature.getAttributes()
						.get(DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY).toString();// ,DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY_TYPE);
				ICondition visiblity = conditionParser.parse(visbility);
				IDecision decision = dm.get(TraVarTUtils.getFeatureName(feature));
				decision.setVisibility(visiblity);
			}
		}
	}
}
