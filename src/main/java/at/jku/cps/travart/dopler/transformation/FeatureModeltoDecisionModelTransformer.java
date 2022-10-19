package at.jku.cps.travart.dopler.transformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.prop4j.Literal;
import org.prop4j.Node;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.common.Prop4JUtils;
import at.jku.cps.travart.core.common.TraVarTUtils;
import at.jku.cps.travart.core.common.exc.ConditionCreationException;
import at.jku.cps.travart.core.common.exc.NotSupportedVariablityTypeException;
import at.jku.cps.travart.core.transformation.DefaultModelTransformationProperties;
import at.jku.cps.travart.dopler.common.DecisionModelUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.CircleInConditionException;
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
import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;

@SuppressWarnings({ "rawtypes", "unchecked" })
public final class FeatureModeltoDecisionModelTransformer implements IModelTransformer<IFeatureModel, IDecisionModel> {

	private DecisionModelFactory factory;
	private IFeatureModel fm;
	private DecisionModel dm;

	@Override
	public DecisionModel transform(final IFeatureModel fm, final String modelName)
			throws NotSupportedVariablityTypeException {
		this.fm = fm;
		try {
			factory = DecisionModelFactory.getInstance();
			dm = factory.create();
			dm.setName(FeatureUtils.getName(FeatureUtils.getRoot(fm)));
			convertFeature(FeatureUtils.getRoot(fm));
			convertConstraints(FeatureUtils.getConstraints(fm));
			convertVisibilityCustomProperties(FeatureUtils.getFeatures(fm));
			return dm;
		} catch (CircleInConditionException | ConditionCreationException e) {
			throw new NotSupportedVariablityTypeException(e);
		}
	}

	private void convertFeature(final IFeature feature) throws NotSupportedVariablityTypeException {
		if (!FeatureUtils.getName(feature).equals(DefaultModelTransformationProperties.ARTIFICAL_MODEL_NAME)) {
			BooleanDecision decision = factory.createBooleanDecision(feature.getName());
			dm.add(decision);
			decision.setQuestion(feature.getName() + "?");
			if (TraVarTUtils.isEnumerationType(feature)) {
				createEnumDecisions(feature);
			}
			if (FeatureUtils.isRoot(feature)) {
				decision.setVisibility(ICondition.TRUE);
			} else if (isEnumSubFeature(feature) && !hasVirtualParent(feature)) {
				decision.setVisibility(ICondition.FALSE);
			} else if (FeatureUtils.isMandatorySet(feature) && !hasVirtualParent(feature)) {
				String parentName = FeatureUtils.getName(FeatureUtils.getParent(feature));
				// as tree traversal, the parent should be dealt with already
				IDecision parent = dm.get(parentName);
				assert parent != null;
				Rule rule = new Rule(new IsSelectedFunction(parent), new SelectDecisionAction(decision));
				parent.addRule(rule);
				updateRules(parent, rule);
				decision.setVisibility(new And(ICondition.FALSE, new IsSelectedFunction(parent)));
			} else if (!hasVirtualParent(feature)) {
				IFeature parentFeature = FeatureUtils.getParent(feature);
				Objects.requireNonNull(parentFeature);
				IDecision parent = dm.get(parentFeature.getName());
				decision.setVisibility(new IsSelectedFunction(parent));
			} else {
				decision.setVisibility(ICondition.TRUE);
			}
		}
		for (IFeature child : FeatureUtils.getChildren(feature)) {
			if (FeatureUtils.getName(feature).equals(DefaultModelTransformationProperties.ARTIFICAL_MODEL_NAME)
					|| !isEnumSubFeature(child)) {
				convertFeature(child);
			}
		}
	}

	private boolean hasVirtualParent(final IFeature feature) {
		IFeature parent = FeatureUtils.getParent(feature);
		if (parent != null) {
			return FeatureUtils.getName(parent).equals(DefaultModelTransformationProperties.ARTIFICAL_MODEL_NAME);
		}
		return false;
	}

	private void createEnumDecisions(final IFeature feature) throws NotSupportedVariablityTypeException {
		// in FeatureIDE only one feature group is possible per feature
		EnumDecision enumDecision = factory.createEnumDecision(feature.getName() + "#0");
		dm.add(enumDecision);
		enumDecision.setQuestion("Choose your " + feature.getName() + "?");
		defineCardinality(enumDecision, feature);
		defineRange(enumDecision, feature);
		IDecision parent = dm.get(feature.getName());
		enumDecision.setVisibility(new IsSelectedFunction(parent));
//		 add rule from parent to enum decision so when you select the boolean one it
//		 has to be selected as well --> done with visiblity condition not with a rule
//		Rule rule = new Rule(new IsSelectedFunction(parent), new SelectDecisionAction(enumDecision));
//		parent.addRule(rule);
//		updateRules(parent, rule);
		for (IFeature optionFeature : FeatureUtils.getChildren(feature)) {
			convertFeature(optionFeature);
			BooleanDecision optionDecision = (BooleanDecision) dm.get(optionFeature.getName());
			ARangeValue optionValue = enumDecision.getRangeValue(optionFeature.getName());
			// as tree traversal, the parent should be dealt with already
			assert optionDecision != null;
			Rule rule = new Rule(new DecisionValueCondition(enumDecision, optionValue),
					new SelectDecisionAction(optionDecision));
			enumDecision.addRule(rule);
			updateRules(enumDecision, rule);
		}
		// create rule for optional parent features
		if (!FeatureUtils.isRoot(feature) && !FeatureUtils.isMandatorySet(feature)) {
			Rule rule = new Rule(new Not(new IsSelectedFunction(parent)),
					new SetValueAction(enumDecision, enumDecision.getNoneOption()));
			parent.addRule(rule);
			updateRules(parent, rule);
		}
	}

	private void convertConstraints(final List<IConstraint> constraints)
			throws CircleInConditionException, ConditionCreationException {
		for (IConstraint constraint : constraints) {
			convertConstraintNodeRec(constraint.getNode());
		}
	}

	private void convertConstraintNodeRec(final Node node)
			throws CircleInConditionException, ConditionCreationException {
		// create a CNF from nodes enables separating the concerns how to transform the
		// different groups.
		// A requires B <=> CNF: Not(A) or B
		// A excludes B <=> CNF: Not(A) or Not(B)
		Node cnfNode = node.toCNF();
		if (Prop4JUtils.isComplexNode(cnfNode)) {
			for (Node child : cnfNode.getChildren()) {
				convertConstraintNodeRec(child);
			}
		} else {
			convertConstraintNode(cnfNode);
		}
	}

	private void deriveUnidirectionalRules(final Node cnfNode)
			throws CircleInConditionException, ConditionCreationException {
		Node negative = Prop4JUtils.getFirstNegativeLiteral(cnfNode);
		String feature = Prop4JUtils.getLiteralName((Literal) negative);
		IDecision decision = dm.get(feature);
		if (decision.getType() == ADecision.DecisionType.BOOLEAN) {
			BooleanDecision target = (BooleanDecision) decision;
			Set<Node> positiveLiterals = Prop4JUtils.getPositiveLiterals(cnfNode);
			List<IDecision> conditionDecisions = findDecisionsForLiterals(positiveLiterals);
			List<IDecision> ruleDecisions = findDecisionsForLiterals(positiveLiterals);
			ICondition ruleCondition = DecisionModelUtils.consumeToBinaryCondition(conditionDecisions, And.class, true);
			Rule rule = new Rule(ruleCondition, new DeSelectDecisionAction(target));
			// add new rule to all rule decisions
			for (IDecision source : ruleDecisions) {
				source.addRule(rule);
				updateRules(source, rule);
			}
		}
	}

	private void convertConstraintNode(final Node cnfNode)
			throws CircleInConditionException, ConditionCreationException {
		if (Prop4JUtils.isSingleFeatureRequires(cnfNode)) {
			// mandatory from root - requires rule
			IFeature root = FeatureUtils.getRoot(fm);
			Node targetLiteral = Prop4JUtils.getFirstPositiveLiteral(cnfNode);
			String targetFeature = Prop4JUtils.getLiteralName((Literal) targetLiteral);
			IDecision source = dm.get(FeatureUtils.getName(root));
			IDecision target = dm.get(targetFeature);
			Rule rule = new Rule(new IsSelectedFunction(source), new SelectDecisionAction((BooleanDecision) target));
			source.addRule(rule);
			updateRules(source, rule);
		} else if (Prop4JUtils.isSingleFeatureExcludes(cnfNode)) {
			// excludes from root - dead feature - excludes rule
			IFeature root = FeatureUtils.getRoot(fm);
			Node targetLiteral = Prop4JUtils.getFirstNegativeLiteral(cnfNode);
			String targetFeature = Prop4JUtils.getLiteralName((Literal) targetLiteral);
			IDecision source = dm.get(FeatureUtils.getName(root));
			IDecision target = dm.get(targetFeature);
			Rule rule = new Rule(new IsSelectedFunction(source), new DeSelectDecisionAction((BooleanDecision) target));
			source.addRule(rule);
			updateRules(source, rule);
		} else if (Prop4JUtils.isRequires(cnfNode)) {
			deriveRequiresRules(cnfNode);
		} else if (Prop4JUtils.isExcludes(cnfNode)) {
			deriveExcludeRules(cnfNode);
		} else if (Prop4JUtils.countLiterals(cnfNode) == 2 && Prop4JUtils.hasNegativeLiteral(cnfNode)
				&& Prop4JUtils.countNegativeLiterals(cnfNode) == 1) {
			deriveUnidirectionalRules(cnfNode);
		} else if (Prop4JUtils.countLiterals(cnfNode) > 1 && Prop4JUtils.hasNegativeLiteral(cnfNode)
				&& Prop4JUtils.countNegativeLiterals(cnfNode) == 1) {
			deriveUnidirectionalRules(cnfNode);
		} else if (Prop4JUtils.countLiterals(cnfNode) == 2 && Prop4JUtils.hasPositiveLiteral(cnfNode)
				&& Prop4JUtils.countPositiveLiterals(cnfNode) == 1) {
			// TODO this path may be identical with the one in line 194.
			// better double check before deleting this though, that functionality is not
			// impaired.
			Node positive = Prop4JUtils.getFirstPositiveLiteral(cnfNode);
			String feature = Prop4JUtils.getLiteralName((Literal) positive);
			IDecision decision = dm.get(feature);
			if (decision.getType() == ADecision.DecisionType.BOOLEAN) {
				BooleanDecision target = (BooleanDecision) decision;
				Set<Node> negativeLiterals = Prop4JUtils.getNegativeLiterals(cnfNode);
				List<IDecision> conditionDecisions = findDecisionsForLiterals(negativeLiterals);
				List<IDecision> ruleDecisions = findDecisionsForLiterals(negativeLiterals);
				ICondition ruleCondition = DecisionModelUtils.consumeToBinaryCondition(conditionDecisions, And.class,
						false);
				Rule rule = new Rule(ruleCondition, new SelectDecisionAction(target));
				// add new rule to all rule decisions
				for (IDecision source : ruleDecisions) {
					source.addRule(rule);
					updateRules(source, rule);
				}
			}
		} else if (Prop4JUtils.countLiterals(cnfNode) > 1 && Prop4JUtils.hasPositiveLiteral(cnfNode)
				&& Prop4JUtils.countPositiveLiterals(cnfNode) == 1) {
			Node positive = Prop4JUtils.getFirstPositiveLiteral(cnfNode);
			String feature = Prop4JUtils.getLiteralName((Literal) positive);
			IDecision decision = dm.get(feature);
			if (decision.getType() == ADecision.DecisionType.BOOLEAN) {
				BooleanDecision target = (BooleanDecision) decision;
				Set<Node> negativeLiterals = Prop4JUtils.getNegativeLiterals(cnfNode);
				List<IDecision> conditionDecisions = findDecisionsForLiterals(negativeLiterals);
				List<IDecision> ruleDecisions = findDecisionsForLiterals(negativeLiterals);
				ICondition ruleCondition = DecisionModelUtils.consumeToBinaryCondition(conditionDecisions, And.class,
						false);
				Rule rule = new Rule(ruleCondition, new SelectDecisionAction(target));
				// add new rule to all rule decisions
				for (IDecision source : ruleDecisions) {
					source.addRule(rule);
					updateRules(source, rule);
				}
			}
		} else if (Prop4JUtils.countLiterals(cnfNode) > 1
				&& Prop4JUtils.countLiterals(cnfNode) == Prop4JUtils.countPositiveLiterals(cnfNode)
				&& Prop4JUtils.isOr(cnfNode)) {
			Set<Node> literals = Prop4JUtils.getLiterals(cnfNode);
			List<IDecision> literalDecisions = findDecisionsForLiterals(literals);
			EnumDecision enumDecision = factory
					.createEnumDecision(String.format("or#constr#%s", literalDecisions.size()));
			dm.add(enumDecision);
			enumDecision.setQuestion("Which features should be added? Pick at least one!");
			enumDecision.setCardinality(new Cardinality(1, literalDecisions.size()));
			enumDecision.setRange(new Range<>());
			IDecision rootDecision = dm.get(FeatureUtils.getName(FeatureUtils.getRoot(fm)));
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
		} else if (Prop4JUtils.countLiterals(cnfNode) > 1
				&& Prop4JUtils.countLiterals(cnfNode) == Prop4JUtils.countPositiveLiterals(cnfNode)
				&& Prop4JUtils.isAnd(cnfNode)) {
			Set<Node> literals = Prop4JUtils.getLiterals(cnfNode);
			List<IDecision> literalDecisions = findDecisionsForLiterals(literals);
			EnumDecision enumDecision = factory
					.createEnumDecision(String.format("or#constr#%s", literalDecisions.size()));
			dm.add(enumDecision);
			enumDecision.setQuestion("Which features should be added? Pick at least one!");
			enumDecision.setCardinality(new Cardinality(literalDecisions.size(), literalDecisions.size()));
			enumDecision.setRange(new Range<>());
			IDecision rootDecision = dm.get(FeatureUtils.getName(FeatureUtils.getRoot(fm)));
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
		} else if (Prop4JUtils.countLiterals(cnfNode) == 1 && Prop4JUtils.isLiteral(cnfNode)) {
			if (Prop4JUtils.hasPositiveLiteral(cnfNode)) {
				String feature = Prop4JUtils.getLiteralName((Literal) cnfNode);
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
				String feature = Prop4JUtils.getLiteralName((Literal) cnfNode);
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
			ICondition condition = deriveConditionFromNode(cnfNode);
			// create implies with root decision to set to false
			BooleanDecision rootDecision = (BooleanDecision) dm.get(FeatureUtils.getName(FeatureUtils.getRoot(fm)));
			Rule rule = new Rule(new Not(condition), new DeSelectDecisionAction(rootDecision));
			rootDecision.addRule(rule);
			updateRules(rootDecision, rule);
		}
	}

	private void deriveRequiresRules(final Node cnfNode) {
		Node sourceLiteral = Prop4JUtils.getFirstNegativeLiteral(cnfNode);
		Node targetLiteral = Prop4JUtils.getFirstPositiveLiteral(cnfNode);
		if (Prop4JUtils.isLiteral(sourceLiteral) && Prop4JUtils.isLiteral(targetLiteral)) {
			String sourceFeature = Prop4JUtils.getLiteralName((Literal) sourceLiteral);
			String targetFeature = Prop4JUtils.getLiteralName((Literal) targetLiteral);
			IDecision source = dm.get(sourceFeature);
			IDecision target = dm.get(targetFeature);
			if (isEnumFeature(source) && !isEnumFeature(target)) {
				EnumDecision enumDecision = findEnumDecisionWithVisiblityCondition(source);
				if (enumDecision.hasNoneOption()) {
					Rule rule = new Rule(
							new Not(new DecisionValueCondition(enumDecision, enumDecision.getNoneOption())),
							new SelectDecisionAction((BooleanDecision) target));
					enumDecision.addRule(rule);
					updateRules(enumDecision, rule);
				} else if (hasOptionalParent(sourceFeature) && target.getType() == ADecision.DecisionType.BOOLEAN) {
					// search the boolean parent decision for the enum and then set the rule there
					IDecision parent = ((IsSelectedFunction) enumDecision.getVisiblity()).getParameters().get(0);
					Rule rule = new Rule(new IsSelectedFunction(parent),
							new SelectDecisionAction((BooleanDecision) target));
					source.addRule(rule);
					updateRules(source, rule);
				}
			} else if (!isEnumFeature(source) && isEnumFeature(target)) {
				// the none value of the enum feature if available must be disallowed
				EnumDecision enumDecision = findEnumDecisionWithVisiblityCondition(target);
				if (enumDecision.hasNoneOption()) {
					Rule rule = new Rule(new IsSelectedFunction(source),
							new DisAllowAction(enumDecision, enumDecision.getNoneOption()));
					source.addRule(rule);
					updateRules(source, rule);
				}

			} else if (!hasVirtualParent(target) && isEnumSubFeature(target)) {
				EnumDecision enumDecision = findEnumDecisionByRangeValue(target);
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

	private boolean hasOptionalParent(final String featureName) {
		IFeature feature = fm.getFeature(featureName);
		if (feature == null) {
			return false;
		}
		IFeature parent = FeatureUtils.getParent(feature);
		while (parent != null && !FeatureUtils.isRoot(feature)) {
			if (!FeatureUtils.isMandatorySet(parent)) {
				return true;
			}
			parent = FeatureUtils.getParent(parent);
		}
		return false;
	}

	private boolean hasVirtualParent(final IDecision decision) {
		IFeature feature = fm.getFeature(
				DecisionModelUtils.retriveFeatureName(decision, decision.getType() == ADecision.DecisionType.BOOLEAN));
		IFeature parent = FeatureUtils.getParent(feature);
		return FeatureUtils.getName(parent).equals(DefaultModelTransformationProperties.ARTIFICAL_MODEL_NAME);
	}

	private void deriveExcludeRules(final Node cnfNode) {
		// excludes constraints are bidirectional by default, therefore direction for
		// defined constraint is negligible, but we need two rules
		Node sourceLiteral = Prop4JUtils.getLeftNode(cnfNode);
		Node targetLiteral = Prop4JUtils.getRightNode(cnfNode);
		if (Prop4JUtils.isLiteral(sourceLiteral) && Prop4JUtils.isLiteral(targetLiteral)) {
			String sourceFeature = Prop4JUtils.getLiteralName((Literal) sourceLiteral);
			String targetFeature = Prop4JUtils.getLiteralName((Literal) targetLiteral);
			IDecision source = dm.get(sourceFeature);
			IDecision target = dm.get(targetFeature);
			if (isEnumSubFeature(source) && !isEnumSubFeature(target)) {
				// if either of the decisions is a enum sub feature the value for the
				// enumeration decision must be disallowed
				Rule rule = new Rule(new IsSelectedFunction(source),
						new DeSelectDecisionAction((BooleanDecision) target));
				source.addRule(rule);
				updateRules(source, rule);
				EnumDecision enumDecision = findEnumDecisionByRangeValue(source);
				ARangeValue value = enumDecision.getRangeValue(sourceFeature);
				rule = new Rule(new IsSelectedFunction(target), new DisAllowAction(enumDecision, value));
				target.addRule(rule);
				updateRules(target, rule);
			} else if (!isEnumSubFeature(source) && isEnumSubFeature(target)) {
				EnumDecision enumDecision = findEnumDecisionByRangeValue(target);
				ARangeValue value = enumDecision.getRangeValue(targetFeature);
				Rule rule = new Rule(new IsSelectedFunction(source), new DisAllowAction(enumDecision, value));
				source.addRule(rule);
				updateRules(target, rule);
				rule = new Rule(new IsSelectedFunction(target), new DeSelectDecisionAction((BooleanDecision) source));
				target.addRule(rule);
				updateRules(target, rule);
			} else if (isEnumSubFeature(source) && isEnumSubFeature(target)) {
				EnumDecision sourceEnumDecision = findEnumDecisionByRangeValue(source);
				EnumDecision targetEnumDecision = findEnumDecisionByRangeValue(target);
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

	private ICondition deriveConditionFromNode(final Node node) throws ConditionCreationException {
		if (Prop4JUtils.isAnd(node)) {
			List<Node> nodes = new ArrayList<Node>();
			nodes.addAll(Arrays.asList(node.getChildren()));
			ICondition first = deriveConditionFromNode(nodes.remove(0));
			ICondition second = deriveConditionFromNode(nodes.remove(0));
			And and = new And(first, second);
			while (!nodes.isEmpty()) {
				ICondition next = deriveConditionFromNode(nodes.remove(0));
				and = new And(and, next);
			}
			return and;
		}
		if (Prop4JUtils.isOr(node)) {
			List<Node> nodes = new ArrayList<Node>();
			nodes.addAll(Arrays.asList(node.getChildren()));
			ICondition first = deriveConditionFromNode(nodes.remove(0));
			ICondition second = deriveConditionFromNode(nodes.remove(0));
			Or or = new Or(first, second);
			while (!nodes.isEmpty()) {
				ICondition next = deriveConditionFromNode(nodes.remove(0));
				or = new Or(or, next);
			}
			return or;
		}
		if (Prop4JUtils.isLiteral(node)) {
			IDecision decision = dm.get(Prop4JUtils.getLiteralName((Literal) node));
			IsSelectedFunction function = new IsSelectedFunction(decision);
			return Prop4JUtils.isNegativeLiteral(node) ? new Not(function) : function;
		}
		throw new ConditionCreationException(String.format("Not supported condition type: %s", node));
	}

	private EnumDecision findEnumDecisionWithVisiblityCondition(final IDecision decision) {
		// TODO: order causes issues; in one model (financial services) it is needed the
		// other way around
		// TODO: in another model as it is right now (Ubuntu_14_1)
		// TODO: which is the best way to find out
		List<EnumDecision> decisions = dm.findWithVisibility(decision).stream()
				.filter(d -> (d.getType() == ADecision.DecisionType.ENUM)).map(d -> (EnumDecision) d)
				.collect(Collectors.toList());
		if (!decisions.isEmpty()) {
			return decisions.remove(0);
		}
		return null;
	}

	private EnumDecision findEnumDecisionByRangeValue(final IDecision decision) {
		List<EnumDecision> decisions = new ArrayList<>(dm.findWithRangeValue(decision));
		if (!decisions.isEmpty()) {
			return decisions.remove(0);
		}
		return null;
	}

	private List<IDecision> findDecisionsForLiterals(final Set<Node> literals) {
		List<IDecision> literalDecisions = new ArrayList<>();
		for (Node positive : literals) {
			String name = Prop4JUtils.getLiteralName((Literal) positive);
			IDecision d = dm.get(name);
			if (d != null) {
				literalDecisions.add(d);
			}
		}
		return literalDecisions;
	}

	private void updateRules(final IDecision decision, final Rule rule) {
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

	private void invertRule(final IDecision decision, final Rule rule) {
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

	private ICondition invertCondition(final ICondition condition) {
		if (condition instanceof Not) {
			return ((Not) condition).getOperand();
		}
		return new Not(condition);
	}

	private boolean isEnumSubFeature(final IFeature feature) {
		// works as each tree in FeatureIDE has only one cardinality across all
		// sub-features.
		IFeature parent = FeatureUtils.getParent(feature);
		return parent != null && TraVarTUtils.isEnumerationType(parent);
	}

	private boolean isEnumSubFeature(final IDecision decision) {
		IFeature feature = fm.getFeature(
				DecisionModelUtils.retriveFeatureName(decision, decision.getType() == ADecision.DecisionType.ENUM));
		return isEnumSubFeature(feature);
	}

	private boolean isEnumFeature(final IDecision decision) {
		IFeature feature = fm.getFeature(
				DecisionModelUtils.retriveFeatureName(decision, decision.getType() == ADecision.DecisionType.ENUM));
		return TraVarTUtils.isEnumerationType(feature);
	}

	private void defineCardinality(final EnumDecision decision, final IFeature feature) {
		Cardinality cardinality = null;
		if (FeatureUtils.isOr(feature)) {
			cardinality = new Cardinality(1, FeatureUtils.getChildrenCount(feature));
		} else if (FeatureUtils.isAlternative(feature)) {
			cardinality = new Cardinality(1, 1);
		} else {
			cardinality = new Cardinality(0, FeatureUtils.getChildrenCount(feature));
		}
		decision.setCardinality(cardinality);
	}

	private void defineRange(final EnumDecision decision, final IFeature feature)
			throws NotSupportedVariablityTypeException {
		Range<String> range = new Range<>();
		for (IFeature optionFeature : FeatureUtils.getChildren(feature)) {
			StringValue option = new StringValue(optionFeature.getName());
			range.add(option);
		}
		decision.setRange(range);
		// add non option if it comes from a enum decision
		if (!FeatureUtils.isMandatorySet(feature) || isEnumSubFeature(feature)) {
			ARangeValue noneOption = decision.getNoneOption();
			noneOption.enable();
			decision.getRange().add(noneOption);
			try {
				decision.setValue((String) noneOption.getValue());
			} catch (RangeValueException e) {
				throw new NotSupportedVariablityTypeException(e);
			}
		}
	}

	private void convertVisibilityCustomProperties(final Collection<IFeature> features) {
		for (IFeature feature : features) {
			if (feature.getCustomProperties().has(DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY,
					DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY_TYPE)) {
				ConditionParser conditionParser = new ConditionParser(dm);
				String visbility = feature.getCustomProperties().get(
						DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY,
						DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY_TYPE);
				ICondition visiblity = conditionParser.parse(visbility);
				IDecision decision = dm.get(feature.getName());
				decision.setVisibility(visiblity);
			}
		}
	}
}
