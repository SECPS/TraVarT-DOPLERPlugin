package at.jku.cps.travart.dopler.transformation;

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
import at.jku.cps.travart.dopler.decision.model.ADecision;
import at.jku.cps.travart.dopler.decision.model.ARangeValue;
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
import at.jku.cps.travart.dopler.decision.model.impl.EnumDecision;
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

@SuppressWarnings("rawtypes")
public abstract class TransformFMtoDMUtil {

	protected static void convertFeature(DecisionModelFactory factory, IDecisionModel dm, final Feature feature)
			throws NotSupportedVariabilityTypeException {
		if (!feature.getFeatureName().equals(DefaultModelTransformationProperties.ARTIFICIAL_MODEL_NAME)) {
			BooleanDecision decision = factory.createBooleanDecision(feature.getFeatureName());
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
				String parentName = feature.getParentFeature().getFeatureName();
				// as tree traversal, the parent should be dealt with already
				IDecision parent = dm.get(parentName);
				assert parent != null;
				// funky behaviour due to roundtrip compatibility - don't change
				Rule rule = new Rule(new IsSelectedFunction(parent), new SelectDecisionAction(decision));
				parent.addRule(rule);
				updateRules(parent, rule);
				decision.setVisibility(new And(ICondition.FALSE, new IsSelectedFunction(parent)));
			} else if (!hasVirtualParent(feature)) {
				Feature parentFeature = feature.getParentFeature();
				Objects.requireNonNull(parentFeature);
				IDecision parent = dm.get(parentFeature.getFeatureName());
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

	protected static boolean hasVirtualParent(final Feature feature) {
		Feature parent = feature.getParentFeature();
		if (parent != null) {
			return parent.getFeatureName().equals(DefaultModelTransformationProperties.ARTIFICIAL_MODEL_NAME);
		}
		return false;
	}

	protected static void createEnumDecisions(DecisionModelFactory factory, IDecisionModel dm, final Feature feature)
			throws NotSupportedVariabilityTypeException {
		// in FeatureIDE only one feature group is possible per feature
		EnumDecision enumDecision = factory.createEnumDecision(feature.getFeatureName() + "#0");
		dm.add(enumDecision);
		enumDecision.setQuestion("Choose your " + feature.getFeatureName() + "?");
		defineCardinality(enumDecision, feature);
		defineRange(enumDecision, feature);
		IDecision parent = dm.get(feature.getFeatureName());
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
			ARangeValue optionValue = enumDecision.getRangeValue(optionFeature.getFeatureName());
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

	protected static void convertConstraints(DecisionModelFactory factory, IDecisionModel dm, FeatureModel fm,
			final List<Constraint> constraints) throws CircleInConditionException, ConditionCreationException {
		for (Constraint constraint : constraints) {
			convertConstraintRec(factory, dm, fm, constraint);
		}
	}

	protected static void convertConstraintRec(DecisionModelFactory factory, IDecisionModel dm, FeatureModel fm,
			final Constraint node) throws CircleInConditionException, ConditionCreationException {
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

	protected static void deriveUnidirectionalRules(IDecisionModel dm, final Constraint cnfConstraint)
			throws CircleInConditionException, ConditionCreationException {
		Constraint negative = TraVarTUtils.getFirstNegativeLiteral(cnfConstraint);
		String feature = ((LiteralConstraint) negative).getLiteral();
		IDecision decision = dm.get(feature);
		if (decision.getType() == ADecision.DecisionType.BOOLEAN) {
			BooleanDecision target = (BooleanDecision) decision;
			Set<Constraint> positiveLiterals = TraVarTUtils.getPositiveLiterals(cnfConstraint);
			List<IDecision> conditionDecisions = findDecisionsForLiterals(dm, positiveLiterals);
			List<IDecision> ruleDecisions = findDecisionsForLiterals(dm, positiveLiterals);
			ICondition ruleCondition = DecisionModelUtils.consumeToBinaryCondition(conditionDecisions, And.class, true);
			Rule rule = new Rule(ruleCondition, new DeSelectDecisionAction(target));
			// add new rule to all rule decisions
			for (IDecision source : ruleDecisions) {
				source.addRule(rule);
				updateRules(source, rule);
			}
		}
	}

	protected static void convertConstraint(DecisionModelFactory factory, IDecisionModel dm, FeatureModel fm,
			final Constraint cnfConstraint) throws CircleInConditionException, ConditionCreationException,
			at.jku.cps.travart.dopler.decision.exc.ConditionCreationException {
		if (TraVarTUtils.isSingleFeatureRequires(cnfConstraint)) {
			// mandatory from root - requires rule
			Feature root = fm.getFeatureMap()
					.get(TraVarTUtils.deriveFeatureModelRoot(fm.getFeatureMap(), "virtual root"));
			Constraint targetLiteral = TraVarTUtils.getFirstPositiveLiteral(cnfConstraint);
			String targetFeature = ((LiteralConstraint) targetLiteral).getLiteral();
			IDecision source = dm.get(root.getFeatureName());
			IDecision target = dm.get(targetFeature);
			Rule rule = new Rule(new IsSelectedFunction(source), new SelectDecisionAction((BooleanDecision) target));
			source.addRule(rule);
			updateRules(source, rule);
		} else if (TraVarTUtils.isSingleFeatureExcludes(cnfConstraint)) {
			// excludes from root - dead feature - excludes rule
			Feature root = fm.getFeatureMap()
					.get(TraVarTUtils.deriveFeatureModelRoot(fm.getFeatureMap(), "virtual root"));
			Constraint targetLiteral = TraVarTUtils.getFirstNegativeLiteral(cnfConstraint);
			String targetFeature = ((LiteralConstraint) targetLiteral).getLiteral();
			IDecision source = dm.get(root.getFeatureName());
			IDecision target = dm.get(targetFeature);
			Rule rule = new Rule(new IsSelectedFunction(source), new DeSelectDecisionAction((BooleanDecision) target));
			source.addRule(rule);
			updateRules(source, rule);
		} else if (TraVarTUtils.isRequires(cnfConstraint)) {
			deriveRequiresRules(dm, fm, cnfConstraint);
		} else if (TraVarTUtils.isExcludes(cnfConstraint)) {
			deriveExcludeRules(dm, fm, cnfConstraint);
		} else if (TraVarTUtils.countLiterals(cnfConstraint) == 2 && TraVarTUtils.hasNegativeLiteral(cnfConstraint)
				&& TraVarTUtils.countNegativeLiterals(cnfConstraint) == 1) {
			deriveUnidirectionalRules(dm, cnfConstraint);
		} else if (TraVarTUtils.countLiterals(cnfConstraint) > 1 && TraVarTUtils.hasNegativeLiteral(cnfConstraint)
				&& TraVarTUtils.countNegativeLiterals(cnfConstraint) == 1) {
			deriveUnidirectionalRules(dm, cnfConstraint);
		} else if (TraVarTUtils.countLiterals(cnfConstraint) > 1 && TraVarTUtils.hasPositiveLiteral(cnfConstraint)
				&& TraVarTUtils.countPositiveLiterals(cnfConstraint) == 1) {
			Constraint positive = TraVarTUtils.getFirstPositiveLiteral(cnfConstraint);
			String feature = ((LiteralConstraint) positive).getLiteral();
			IDecision decision = dm.get(feature);
			if (decision.getType() == ADecision.DecisionType.BOOLEAN) {
				BooleanDecision target = (BooleanDecision) decision;
				Set<Constraint> negativeLiterals = TraVarTUtils.getNegativeLiterals(cnfConstraint);
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
		} else if (TraVarTUtils.countLiterals(cnfConstraint) > 1
				&& TraVarTUtils.countLiterals(cnfConstraint) == TraVarTUtils.countPositiveLiterals(cnfConstraint)
				&& cnfConstraint instanceof OrConstraint) {
			Set<Constraint> literals = TraVarTUtils.getLiterals(cnfConstraint);
			List<IDecision> literalDecisions = findDecisionsForLiterals(dm, literals);
			EnumDecision enumDecision = factory
					.createEnumDecision(String.format("or#constr#%s", literalDecisions.size()));
			dm.add(enumDecision);
			enumDecision.setQuestion("Which features should be added? Pick at least one!");
			enumDecision.setCardinality(new Cardinality(1, literalDecisions.size()));
			enumDecision.setRange(new Range<>());
			IDecision rootDecision = dm.get(fm.getRootFeature().getFeatureName());
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
		} else if (TraVarTUtils.countLiterals(cnfConstraint) > 1
				&& TraVarTUtils.countLiterals(cnfConstraint) == TraVarTUtils.countPositiveLiterals(cnfConstraint)
				&& cnfConstraint instanceof AndConstraint) {
			Set<Constraint> literals = TraVarTUtils.getLiterals(cnfConstraint);
			List<IDecision> literalDecisions = findDecisionsForLiterals(dm, literals);
			EnumDecision enumDecision = factory
					.createEnumDecision(String.format("or#constr#%s", literalDecisions.size()));
			dm.add(enumDecision);
			enumDecision.setQuestion("Which features should be added? Pick at least one!");
			enumDecision.setCardinality(new Cardinality(literalDecisions.size(), literalDecisions.size()));
			enumDecision.setRange(new Range<>());
			IDecision rootDecision = dm.get(fm.getRootFeature().getFeatureName());
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
		} else if (TraVarTUtils.countLiterals(cnfConstraint) == 1 && TraVarTUtils.isLiteral(cnfConstraint)) {
			if (TraVarTUtils.hasPositiveLiteral(cnfConstraint)) {
				String feature = ((LiteralConstraint) cnfConstraint).getLiteral();
				IDecision decision = dm.get(feature);
				if (decision.getType() == ADecision.DecisionType.BOOLEAN) {
					BooleanDecision boolDecision = (BooleanDecision) decision;
					if (decision.getVisiblity() instanceof ADecision) {
						IDecision source = (IDecision) decision.getVisiblity();
						Rule rule = new Rule(new IsSelectedFunction(source), new SelectDecisionAction(boolDecision));
						source.addRule(rule);
						updateRules(source, rule);
					} else {
						Rule rule = new Rule(new Not(new IsSelectedFunction(boolDecision)),
								new SelectDecisionAction(boolDecision));
						boolDecision.addRule(rule);
						updateRules(boolDecision, rule);
					}
				}
			} else {
				String feature;
				if (cnfConstraint instanceof NotConstraint) {
					feature = ((LiteralConstraint) ((NotConstraint) cnfConstraint).getContent()).getLiteral();
				} else {
					feature = ((LiteralConstraint) cnfConstraint).getLiteral();
				}
				IDecision decision = dm.get(feature);
				if (decision.getType() == ADecision.DecisionType.BOOLEAN) {
					BooleanDecision boolDecision = (BooleanDecision) decision;
					Rule rule = new Rule(new Not(new IsSelectedFunction(boolDecision)),
							new DeSelectDecisionAction(boolDecision));
					boolDecision.addRule(rule);
					updateRules(boolDecision, rule);
				}
			}
		} else {
			// any other type of mode
			// create demorgen condition node 1:1
			ICondition condition = deriveConditionFromConstraint(dm, cnfConstraint);
			// create implies with root decision to set to false
			BooleanDecision rootDecision = (BooleanDecision) dm.get(fm.getRootFeature().getFeatureName());
			Rule rule = new Rule(new Not(condition), new DeSelectDecisionAction(rootDecision));
			rootDecision.addRule(rule);
			updateRules(rootDecision, rule);
		}
	}

	protected static void deriveRequiresRules(IDecisionModel dm, FeatureModel fm, final Constraint cnfConstraint) {
		Constraint sourceLiteral = TraVarTUtils.getFirstNegativeLiteral(cnfConstraint);
		Constraint targetLiteral = TraVarTUtils.getFirstPositiveLiteral(cnfConstraint);
		if (TraVarTUtils.isLiteral(sourceLiteral) && TraVarTUtils.isLiteral(targetLiteral)) {
			String sourceFeature = ((LiteralConstraint) sourceLiteral).getLiteral();
			String targetFeature = ((LiteralConstraint) targetLiteral).getLiteral();
			IDecision source = dm.get(sourceFeature);
			IDecision target = dm.get(targetFeature);
			if (isEnumFeature(fm, source) && !isEnumFeature(fm, target)) {
				EnumDecision enumDecision = findEnumDecisionWithVisiblityCondition(dm, source);
				if (enumDecision.hasNoneOption()) {
					Rule rule = new Rule(
							new Not(new DecisionValueCondition(enumDecision, enumDecision.getNoneOption())),
							new SelectDecisionAction((BooleanDecision) target));
					enumDecision.addRule(rule);
					updateRules(enumDecision, rule);
				} else if (hasOptionalParent(fm, sourceFeature) && target.getType() == ADecision.DecisionType.BOOLEAN) {
					// search the boolean parent decision for the enum and then set the rule there
					IDecision parent = ((IsSelectedFunction) enumDecision.getVisiblity()).getParameters().get(0);
					Rule rule = new Rule(new IsSelectedFunction(parent),
							new SelectDecisionAction((BooleanDecision) target));
					source.addRule(rule);
					updateRules(source, rule);
				}
			} else if (!isEnumFeature(fm, source) && isEnumFeature(fm, target)) {
				// the none value of the enum feature if available must be disallowed
				EnumDecision enumDecision = findEnumDecisionWithVisiblityCondition(dm, target);
				if (enumDecision.hasNoneOption()) {
					Rule rule = new Rule(new IsSelectedFunction(source),
							new DisAllowAction(enumDecision, enumDecision.getNoneOption()));
					source.addRule(rule);
					updateRules(source, rule);
				}

			} else if (!hasVirtualParent(fm, target) && isEnumSubFeature(fm, target)) {
				EnumDecision enumDecision = findEnumDecisionByRangeValue(dm, target);
				ARangeValue value = enumDecision.getRangeValue(targetFeature);
				Rule rule = new Rule(new IsSelectedFunction(source), new SetValueAction(enumDecision, value));
				source.addRule(rule);
				updateRules(source, rule);
			} else if (target.getType() == ADecision.DecisionType.BOOLEAN) {
				Rule rule = new Rule(new IsSelectedFunction(source),
						new SelectDecisionAction((BooleanDecision) target));
				source.addRule(rule);
				updateRules(source, rule);
			}
		}
	}

	protected static boolean hasOptionalParent(FeatureModel fm, final String featureName) {
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

	protected static boolean hasVirtualParent(FeatureModel fm, final IDecision decision) {
		Feature feature = fm.getFeatureMap().get(
				DecisionModelUtils.retriveFeatureName(decision, decision.getType() == ADecision.DecisionType.BOOLEAN));
		Feature parent = feature.getParentFeature();
		return parent.getFeatureName().equals(DefaultModelTransformationProperties.ARTIFICIAL_MODEL_NAME);
	}

	protected static void deriveExcludeRules(IDecisionModel dm, FeatureModel fm, final Constraint cnfConstraint) {
		// excludes constraints are bidirectional by default, therefore direction for
		// defined constraint is negligible, but we need two rules
		Constraint sourceLiteral = TraVarTUtils.getLeftConstraint(cnfConstraint);
		Constraint targetLiteral = TraVarTUtils.getRightConstraint(cnfConstraint);
		if (TraVarTUtils.isLiteral(sourceLiteral) && TraVarTUtils.isLiteral(targetLiteral)) {
			String sourceFeature = ((LiteralConstraint) sourceLiteral).getLiteral();
			String targetFeature = ((LiteralConstraint) targetLiteral).getLiteral();
			IDecision source = dm.get(sourceFeature);
			IDecision target = dm.get(targetFeature);
			if (isEnumSubFeature(fm, source) && !isEnumSubFeature(fm, target)) {
				// if either of the decisions is a enum sub feature the value for the
				// enumeration decision must be disallowed
				Rule rule = new Rule(new IsSelectedFunction(source),
						new DeSelectDecisionAction((BooleanDecision) target));
				source.addRule(rule);
				updateRules(source, rule);
				EnumDecision enumDecision = findEnumDecisionByRangeValue(dm, source);
				ARangeValue value = enumDecision.getRangeValue(sourceFeature);
				rule = new Rule(new IsSelectedFunction(target), new DisAllowAction(enumDecision, value));
				target.addRule(rule);
				updateRules(target, rule);
			} else if (!isEnumSubFeature(fm, source) && isEnumSubFeature(fm, target)) {
				EnumDecision enumDecision = findEnumDecisionByRangeValue(dm, target);
				ARangeValue value = enumDecision.getRangeValue(targetFeature);
				Rule rule = new Rule(new IsSelectedFunction(source), new DisAllowAction(enumDecision, value));
				source.addRule(rule);
				updateRules(target, rule);
				rule = new Rule(new IsSelectedFunction(target), new DeSelectDecisionAction((BooleanDecision) source));
				target.addRule(rule);
				updateRules(target, rule);
			} else if (isEnumSubFeature(fm, source) && isEnumSubFeature(fm, target)) {
				EnumDecision sourceEnumDecision = findEnumDecisionByRangeValue(dm, source);
				EnumDecision targetEnumDecision = findEnumDecisionByRangeValue(dm, target);
				ARangeValue sourceValue = sourceEnumDecision.getRangeValue(sourceFeature);
				ARangeValue targetValue = targetEnumDecision.getRangeValue(targetFeature);
				Rule rule = new Rule(new IsSelectedFunction(source),
						new DisAllowAction(targetEnumDecision, targetValue));
				source.addRule(rule);
				updateRules(source, rule);
				rule = new Rule(new IsSelectedFunction(target), new DisAllowAction(sourceEnumDecision, sourceValue));
				target.addRule(rule);
				updateRules(target, rule);
			} else if (source.getType() == ADecision.DecisionType.BOOLEAN
					&& target.getType() == ADecision.DecisionType.BOOLEAN) {
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

	protected static ICondition deriveConditionFromConstraint(IDecisionModel dm, final Constraint node)
			throws ConditionCreationException {
		if (node instanceof AndConstraint) {
			List<Constraint> nodes = new ArrayList<Constraint>();
			nodes.addAll(node.getConstraintSubParts());
			ICondition first = deriveConditionFromConstraint(dm, nodes.remove(0));
			ICondition second = deriveConditionFromConstraint(dm, nodes.remove(0));
			And and = new And(first, second);
			while (!nodes.isEmpty()) {
				ICondition next = deriveConditionFromConstraint(dm, nodes.remove(0));
				and = new And(and, next);
			}
			return and;
		}
		if (node instanceof OrConstraint) {
			List<Constraint> nodes = new ArrayList<Constraint>();
			nodes.addAll(node.getConstraintSubParts());
			ICondition first = deriveConditionFromConstraint(dm, nodes.remove(0));
			ICondition second = deriveConditionFromConstraint(dm, nodes.remove(0));
			Or or = new Or(first, second);
			while (!nodes.isEmpty()) {
				ICondition next = deriveConditionFromConstraint(dm, nodes.remove(0));
				or = new Or(or, next);
			}
			return or;
		}
		if (TraVarTUtils.isLiteral(node)) {
			IDecision decision = dm.get(((LiteralConstraint) node).getLiteral());
			IsSelectedFunction function = new IsSelectedFunction(decision);
			return TraVarTUtils.isNegativeLiteral(node) ? new Not(function) : function;
		}
		throw new ConditionCreationException(String.format("Not supported condition type: %s", node));
	}

	protected static EnumDecision findEnumDecisionWithVisiblityCondition(IDecisionModel dm, final IDecision decision) {
		// TODO: order causes issues; in one model (financial services) it is needed the
		// other way around
		// TODO: in another model as it is right now (Ubuntu_14_1)
		// TODO: which is the best way to find out
		List<EnumDecision> decisions = ((DecisionModel) dm).findWithVisibility(decision).stream()
				.filter(d -> (d.getType() == ADecision.DecisionType.ENUM)).map(d -> (EnumDecision) d)
				.collect(Collectors.toList());
		if (!decisions.isEmpty()) {
			return decisions.remove(0);
		}
		return null;
	}

	protected static EnumDecision findEnumDecisionByRangeValue(IDecisionModel dm, final IDecision decision) {
		List<EnumDecision> decisions = new ArrayList<>(((DecisionModel) dm).findWithRangeValue(decision));
		if (!decisions.isEmpty()) {
			return decisions.remove(0);
		}
		return null;
	}

	protected static List<IDecision> findDecisionsForLiterals(IDecisionModel dm, final Set<Constraint> literals) {
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

	protected static void updateRules(final IDecision decision, final Rule rule) {
		// Invert the created rule and add it
		invertRule(decision, rule);
		// if the rule contains a none option also disallow it and invert it again for
		// the enumeration
		if (DecisionModelUtils.isNoneAction(rule.getAction())) {
			EnumDecision enumDecision = (EnumDecision) rule.getAction().getVariable();
			ARangeValue rangeValue = (ARangeValue) rule.getAction().getValue();
			Rule r = new Rule(new IsSelectedFunction(decision), new DisAllowAction(enumDecision, rangeValue));
			decision.addRule(r);
			// and again invert the added rule
			invertRule(decision, r);
		}
	}

	protected static void invertRule(final IDecision decision, final Rule rule) {
		if (rule.getAction() instanceof DisAllowAction) {
			DisAllowAction function = (DisAllowAction) rule.getAction();
			IAction allow = new AllowAction(function.getVariable(), (ARangeValue) function.getValue());
			ICondition condition = invertCondition(rule.getCondition());
			Rule r = new Rule(condition, allow);
			decision.addRule(r);
		} else if (rule.getAction() instanceof SelectDecisionAction && decision.getType() == ADecision.DecisionType.ENUM
				&& ((EnumDecision) decision).getCardinality().isAlternative()
				&& !DecisionModelUtils.isNoneCondition(rule.getCondition())) {
			SelectDecisionAction select = (SelectDecisionAction) rule.getAction();
			ICondition condition = invertCondition(rule.getCondition());
			IAction deselect = new DeSelectDecisionAction((BooleanDecision) select.getVariable());
			Rule r = new Rule(condition, deselect);
			decision.addRule(r);
		}
	}

	protected static ICondition invertCondition(final ICondition condition) {
		if (condition instanceof Not) {
			return ((Not) condition).getOperand();
		}
		return new Not(condition);
	}

	protected static boolean isEnumSubFeature(final Feature feature) {
		// works as each tree in FeatureIDE has only one cardinality across all
		// sub-features.
		Feature parent = feature.getParentFeature();
		return parent != null && TraVarTUtils.isEnumerationType(parent);
	}

	protected static boolean isEnumSubFeature(FeatureModel fm, final IDecision decision) {
		Feature feature = fm.getFeatureMap().get(
				DecisionModelUtils.retriveFeatureName(decision, decision.getType() == ADecision.DecisionType.ENUM));
		return isEnumSubFeature(feature);
	}

	protected static boolean isEnumFeature(FeatureModel fm, final IDecision decision) {
		Feature feature = fm.getFeatureMap().get(
				DecisionModelUtils.retriveFeatureName(decision, decision.getType() == ADecision.DecisionType.ENUM));
		return TraVarTUtils.isEnumerationType(feature);
	}

	protected static void defineCardinality(final EnumDecision decision, final Feature feature) {
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

	protected static void defineRange(final EnumDecision decision, final Feature feature)
			throws NotSupportedVariabilityTypeException {
		Range<String> range = new Range<>();
		// TODO Groups?
		for (Feature optionFeature : feature.getChildren().stream().flatMap(g -> g.getFeatures().stream())
				.collect(Collectors.toList())) {
			StringValue option = new StringValue(optionFeature.getFeatureName());
			range.add(option);
		}
		decision.setRange(range);
		// add non option if it comes from a enum decision
		if (feature.getParentGroup() != null
				&& (!feature.getParentGroup().GROUPTYPE.equals(GroupType.MANDATORY) || isEnumSubFeature(feature))) {
			ARangeValue noneOption = decision.getNoneOption();
			noneOption.enable();
			decision.getRange().add(noneOption);
			try {
				decision.setValue((String) noneOption.getValue());
			} catch (RangeValueException e) {
				throw new NotSupportedVariabilityTypeException(e);
			}
		}
	}

	protected static void convertVisibilityCustomProperties(IDecisionModel dm, final Collection<Feature> features) {
		for (Feature feature : features) {
			if (feature.getAttributes()
					.containsKey(DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY)) {// ,DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY_TYPE))
																											// {
				ConditionParser conditionParser = new ConditionParser(dm);
				String visbility = feature.getAttributes()
						.get(DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY).toString();// ,DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY_TYPE);
				ICondition visiblity = conditionParser.parse(visbility);
				IDecision decision = dm.get(feature.getFeatureName());
				decision.setVisibility(visiblity);
			}
		}
	}
}
