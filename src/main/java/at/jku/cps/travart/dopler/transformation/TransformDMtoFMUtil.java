package at.jku.cps.travart.dopler.transformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import at.jku.cps.travart.dopler.common.DecisionModelUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.CircleInConditionException;
import at.jku.cps.travart.dopler.decision.model.ABinaryCondition;
import at.jku.cps.travart.dopler.decision.model.ADecision;
import at.jku.cps.travart.dopler.decision.model.AFunction;
import at.jku.cps.travart.dopler.decision.model.ARangeValue;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.IValue;
import at.jku.cps.travart.dopler.decision.model.impl.And;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanValue;
import at.jku.cps.travart.dopler.decision.model.impl.Cardinality;
import at.jku.cps.travart.dopler.decision.model.impl.DeSelectDecisionAction;
import at.jku.cps.travart.dopler.decision.model.impl.DecisionValueCondition;
import at.jku.cps.travart.dopler.decision.model.impl.DisAllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.EnumDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Equals;
import at.jku.cps.travart.dopler.decision.model.impl.Greater;
import at.jku.cps.travart.dopler.decision.model.impl.GreaterEquals;
import at.jku.cps.travart.dopler.decision.model.impl.IsSelectedFunction;
import at.jku.cps.travart.dopler.decision.model.impl.Less;
import at.jku.cps.travart.dopler.decision.model.impl.LessEquals;
import at.jku.cps.travart.dopler.decision.model.impl.Not;
import at.jku.cps.travart.dopler.decision.model.impl.NumberDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;
import at.jku.cps.travart.dopler.decision.model.impl.SelectDecisionAction;
import at.jku.cps.travart.dopler.decision.model.impl.SetValueAction;
import at.jku.cps.travart.dopler.decision.model.impl.StringDecision;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;
import de.vill.model.Attribute;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import de.vill.model.Group.GroupType;
import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ImplicationConstraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;
import de.vill.model.constraint.OrConstraint;

@SuppressWarnings("rawtypes")
public abstract class TransformDMtoFMUtil {

	protected static void createFeatures(final FeatureModel fm, final IDecisionModel dm)
			throws CircleInConditionException {
		// first add all Boolean decisions
		for (BooleanDecision decision : DecisionModelUtils.getBooleanDecisions(dm)) {
			String featureName = retriveFeatureName(decision);
			Feature feature = new Feature(featureName);
			// Features are optional by default
			fm.getFeatureMap().put(featureName, feature);
		}
		// second add all Number decisions
		for (NumberDecision decision : DecisionModelUtils.getNumberDecisions(dm)) {
			String featureName = retriveFeatureName(decision);
			Feature feature = new Feature(featureName);
			Group alternativeGroup = null;
			if (!decision.getRange().isEmpty()) {
				alternativeGroup = new Group(GroupType.ALTERNATIVE);
			}
			for (Object o : decision.getRange()) {
				IValue value = (IValue) o;
				String childName = featureName
						+ DefaultDecisionModelTransformationProperties.CONFIGURATION_VALUE_SEPERATOR
						+ value.getValue().toString();
				// first check if you can find a feature with the same name as the value
				// if so use it, otherwise create it
				Feature child = fm.getFeatureMap().get(childName);
				if ((child == null)) {
					child = new Feature(childName);
					fm.getFeatureMap().put(childName, child);
				}
				alternativeGroup.getFeatures().add(child);
			}
			// TODO check all instances of newly created features that there in the actual
			// tree as well, not just the
			// map.
			feature.addChildren(alternativeGroup);
		}
		// third add all String decisions
		for (StringDecision decision : DecisionModelUtils.getStringDecisions(dm)) {
			String featureName = retriveFeatureName(decision);
			Feature feature = new Feature(featureName);
			// Features are optional by default
			fm.getFeatureMap().put(featureName, feature);
		}
		// finally build enumeration decisions from exiting features
		for (EnumDecision decision : DecisionModelUtils.getEnumDecisions(dm)) {
			if (!DecisionModelUtils.isEnumDecisionConstraint(decision)) {
				String featureName = retriveFeatureName(decision);
				Feature feature = fm.getFeatureMap().get(featureName);
				if (feature == null) {
					feature = new Feature(featureName);
					fm.getFeatureMap().put(featureName, feature);
				}
				Cardinality cardinality = decision.getCardinality();
				Group enumGroup = null;
				if (cardinality.isAlternative()) {
					enumGroup = new Group(GroupType.ALTERNATIVE);
				} else if (cardinality.isOr()) {
					enumGroup = new Group(GroupType.OR);
				}
				for (Object o : decision.getRange()) {
					IValue value = (IValue) o;
					String childName = value.getValue().toString();
					// first check if you can find a feature with the same name as the value
					// if so use it, otherwise create it
					Feature child = fm.getFeatureMap().get(childName);
					if (child != null) {
						fm.getFeatureMap().put(childName, child);
						enumGroup.getFeatures().add(child);
					}
					// If None value is read, don't add it, as it is special added for optional
					// groups
					else if (!DecisionModelUtils.isEnumNoneOption(decision, value)) {
						child = new Feature(childName);
						fm.getFeatureMap().put(childName, child);
						enumGroup.getFeatures().add(child);
					}
				}
				// Features are optional by default
				feature.addChildren(enumGroup);
			} else {
				// TODO: return transformation if it contains at least 2 # in decision id, then
				// it is a constraint --> build constraint based on cardinality and setdecision
				// actions in rules
				List<Feature> features = new ArrayList<>(decision.getRange().size());
				decision.getRange()
						.forEach(val -> features.add(fm.getFeatureMap().get(String.valueOf(val.getValue()))));
				Constraint constraint = null;
				if (decision.getCardinality().isOr()) {
					constraint = consumeToBinaryCondition(features, new OrConstraint(null, null), false);
				} else if (decision.getCardinality().isAnd()) {
					constraint = consumeToBinaryCondition(features, new AndConstraint(null, null), false);
				}
				addConstraintIfEligible(fm, constraint);
			}
		}
	}

	protected static void addConstraintIfEligible(final FeatureModel fm, final Constraint constraint) {

		if (constraint == null || isInItSelfConstraint(constraint)) {
			return;
		}
		for (Constraint constr : fm.getConstraints()) {
			if (constr.equals(constraint)) {
				return;
			}
		}
		Arrays.asList();
		fm.getConstraints().add(constraint);
	}

	protected static boolean isInItSelfConstraint(final Constraint constraint) {
		if (constraint instanceof LiteralConstraint) {
			return false;
		}
		for (Constraint c : constraint.getConstraintSubParts()) {
			if (!(c instanceof LiteralConstraint)) {
				return false;
			}
		}
		return constraint.getConstraintSubParts().get(0).equals(constraint.getConstraintSubParts().get(1));
	}

	protected static void createFeatureTree(final FeatureModel fm, final IDecisionModel dm) {
		for (IDecision decision : dm.getDecisions()) {
			if (!DecisionModelUtils.isEnumDecisionConstraint(decision)) {
				Feature feature = fm.getFeatureMap().get(retriveFeatureName(decision));
				if (feature.getParentFeature() == null) {
					ICondition visiblity = decision.getVisiblity();
					if (DecisionModelUtils.isMandatoryVisibilityCondition(visiblity)) {
//						FeatureUtils.setMandatory(feature, true);
						IDecision parentD = DecisionModelUtils.retriveMandatoryVisibilityCondition(visiblity);
						String parentFName = retriveFeatureName(parentD);
						Feature parentF = fm.getFeatureMap().get(parentFName);
						if (!parentF.getChildren().stream().anyMatch(g -> g.GROUPTYPE.equals(GroupType.MANDATORY))) {
							parentF.getChildren().add(new Group(GroupType.MANDATORY));
						}
						Group mandatoryGroup = parentF.getChildren().stream()
								.filter(g -> g.GROUPTYPE.equals(GroupType.MANDATORY)).findFirst().get();
						mandatoryGroup.getFeatures().add(feature);
					} else if (visiblity instanceof IsSelectedFunction) {
						IsSelectedFunction isSelected = (IsSelectedFunction) visiblity;
						ADecision parentD = (ADecision) isSelected.getParameters().get(0);
						String parentFName = retriveFeatureName(parentD);
						Feature parentF = fm.getFeatureMap().get(parentFName);
						if (parentF != feature) {
							// TODO check here how groups need to be handled
//							FeatureUtils.addChild(parentF, feature);
							Optional<Group> mandGroup = parentF.getChildren().stream()
									.filter(e -> e.GROUPTYPE.equals(GroupType.OPTIONAL)).findFirst();
							Group targetGroup = null;
							if (!mandGroup.isPresent()) {
								targetGroup = new Group(GroupType.OPTIONAL);
								parentF.addChildren(targetGroup);
							} else {
								targetGroup = mandGroup.get();
							}
							targetGroup.getFeatures().add(feature);
						}
					}
				}
			}
		}
	}

	/**
	 * recursively consumes a list of features into a constraint of Literals.
	 *
	 * @param features
	 * @param c
	 * @param negated
	 * @return
	 */
	protected static Constraint consumeToBinaryCondition(final List<Feature> features, final Constraint c,
			final boolean negated) {
		if (features.isEmpty()) {
			throw new IllegalArgumentException("Set of decisions is empty.");
		}
		if (features.stream().anyMatch(d -> d == null)) {
			throw new IllegalArgumentException("Set of decisions contains Null.");
		}
		if (features.size() == 1) {
			return negated ? new LiteralConstraint(features.remove(0).getFeatureName())
					: new NotConstraint(new LiteralConstraint(features.remove(0).getFeatureName()));
		}
		if (c instanceof OrConstraint) {
			if (negated) {
				return new OrConstraint(new NotConstraint(new LiteralConstraint(features.remove(0).getFeatureName())),
						consumeToBinaryCondition(features, c, negated));
			} else {
				return new OrConstraint(new LiteralConstraint(features.remove(0).getFeatureName()),
						consumeToBinaryCondition(features, c, negated));
			}
		}
		if (c instanceof AndConstraint) {
			if (negated) {
				return new AndConstraint(new NotConstraint(new LiteralConstraint(features.remove(0).getFeatureName())),
						consumeToBinaryCondition(features, c, negated));
			} else {
				return new AndConstraint(new LiteralConstraint(features.remove(0).getFeatureName()),
						consumeToBinaryCondition(features, c, negated));
			}
		}
		return c;
	}

	protected static String retriveFeatureName(final IDecision decision) {
		return DecisionModelUtils.retriveFeatureName(decision, decision.getType() == ADecision.DecisionType.ENUM);
	}

	protected static void createConstraints(final FeatureModel fm, final IDecisionModel dm) {
		for (IDecision decision : dm.getDecisions()) {
			if (!DecisionModelUtils.isEnumDecisionConstraint(decision)) {
				// first store complex visibility condition in properties, to avoid losing the
				// information
				if (DecisionModelUtils.isComplexVisibilityCondition(decision.getVisiblity())
						&& !DecisionModelUtils.isMandatoryVisibilityCondition(decision.getVisiblity())) {
					Feature feature = fm.getFeatureMap().get(retriveFeatureName(decision));
					// add visibility as property for restoring them later if necessary
					feature.getAttributes().put(DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY,
							new Attribute<>(
									DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY_TYPE,
									decision.getVisiblity().toString()));
				}
				// second transform constraints from the rules
				for (Object o : decision.getRules()) {
					Rule rule = (Rule) o;
					if (!DecisionModelUtils.isInItSelfRule(rule)) {
						if (DecisionModelUtils.isCompareCondition(rule.getCondition())) {
							deriveCompareConstraints(fm, decision, rule.getCondition(), rule.getAction());
						} else {
							deriveConstraint(fm, dm, decision, rule.getCondition(), rule.getAction());
						}
					}
				}
			}
		}
	}

	protected static void deriveCompareConstraints(final FeatureModel fm, final IDecision decision,
			final ICondition condition, final IAction action) {
		if (decision.getType() == ADecision.DecisionType.NUMBER && action instanceof DisAllowAction) {
			NumberDecision numberDecision = (NumberDecision) decision;
			ABinaryCondition binCondition = (ABinaryCondition) condition;
			ARangeValue<Double> conditionValue = (ARangeValue<Double>) binCondition.getRight();
			Feature disAllowFeature = fm.getFeatureMap().get(((DisAllowAction) action).getValue().toString());
			LiteralConstraint disAllowLiteral = new LiteralConstraint(disAllowFeature.getFeatureName());
			if (condition instanceof Equals) {
				createExcludesConstraint(fm, numberDecision, disAllowLiteral, conditionValue);
			} else if (condition instanceof Greater) {
				Set<ARangeValue<Double>> values = numberDecision.getRange().stream()
						.filter(v -> v.getValue() > conditionValue.getValue()).collect(Collectors.toSet());
				for (ARangeValue<Double> value : values) {
					createExcludesConstraint(fm, numberDecision, disAllowLiteral, value);
				}
			} else if (condition instanceof Less) {
				Set<ARangeValue<Double>> values = numberDecision.getRange().stream()
						.filter(v -> v.getValue() < conditionValue.getValue()).collect(Collectors.toSet());
				for (ARangeValue<Double> value : values) {
					createExcludesConstraint(fm, numberDecision, disAllowLiteral, value);
				}
			} else if (condition instanceof GreaterEquals) {
				Set<ARangeValue<Double>> values = numberDecision.getRange().stream()
						.filter(v -> v.getValue() >= conditionValue.getValue()).collect(Collectors.toSet());
				for (ARangeValue<Double> value : values) {
					createExcludesConstraint(fm, numberDecision, disAllowLiteral, value);
				}
			} else if (condition instanceof LessEquals) {
				Set<ARangeValue<Double>> values = numberDecision.getRange().stream()
						.filter(v -> v.getValue() <= conditionValue.getValue()).collect(Collectors.toSet());
				for (ARangeValue<Double> value : values) {
					createExcludesConstraint(fm, numberDecision, disAllowLiteral, value);
				}
			}
		}
	}

	protected static void createExcludesConstraint(final FeatureModel fm, final NumberDecision numberDecision,
			final LiteralConstraint disAllowLiteral, final ARangeValue<Double> value) {
		Feature valueFeature = fm.getFeatureMap()
				.get(numberDecision.getId() + DefaultDecisionModelTransformationProperties.CONFIGURATION_VALUE_SEPERATOR
						+ value.getValue().toString());
		LiteralConstraint valueLiteral = new LiteralConstraint(valueFeature.getFeatureName());
		Constraint constraint = new ImplicationConstraint(new LiteralConstraint(valueLiteral.toString()),
				new NotConstraint(new LiteralConstraint(disAllowLiteral.toString())));
		addConstraintIfEligible(fm, constraint);
	}

	protected static void deriveConstraint(final FeatureModel fm, final IDecisionModel dm, final IDecision decision,
			final ICondition condition, final IAction action) {
		Constraint conditionConstraint = deriveConditionConstraint(fm, decision, condition);
		// case: if decision is selected another one has to be selected as well: implies
		if (condition instanceof IsSelectedFunction && action.getVariable() instanceof ADecision
				&& action.getValue().equals(BooleanValue.getTrue())) {
			IsSelectedFunction isSelected = (IsSelectedFunction) condition;
			ADecision conditionDecision = (ADecision) isSelected.getParameters().get(0);
			String conditionFeatureName = retriveFeatureName(conditionDecision);
			Feature conditionFeature = fm.getFeatureMap().get(conditionFeatureName);
			ADecision variableDecision = (ADecision) action.getVariable();
			String variableFeatureName = retriveFeatureName(variableDecision);
			Feature variableFeature = fm.getFeatureMap().get(variableFeatureName);
			if (variableFeature.getParentFeature() != conditionFeature
					|| !variableFeature.getParentGroup().GROUPTYPE.equals(GroupType.MANDATORY)) {
				Constraint constraint = new ImplicationConstraint(conditionConstraint,
						new LiteralConstraint(variableFeature.getFeatureName()));
				addConstraintIfEligible(fm, constraint);
			}
		}
		// case: excludes constraint
		else if (condition instanceof IsSelectedFunction && action.getVariable() instanceof ADecision
				&& action.getValue().equals(BooleanValue.getFalse())) {
			ADecision variableDecision = (ADecision) action.getVariable();
			String featureName = retriveFeatureName(variableDecision);
			Feature feature = fm.getFeatureMap().get(featureName);
			Constraint constraint = new ImplicationConstraint(conditionConstraint,
					new NotConstraint(new LiteralConstraint(feature.getFeatureName())));
			addConstraintIfEligible(fm, constraint);
		}
		// case: if the action allows or disallows/sets a decision to a None value
		else if (condition instanceof IsSelectedFunction && action instanceof SetValueAction
				&& DecisionModelUtils.isNoneAction(action)) {
			ADecision variableDecision = (ADecision) action.getVariable();
			String featureName = retriveFeatureName(variableDecision);
			// set cardinality of variable feature
			Feature varFeature = fm.getFeatureMap().get(featureName);
			Constraint constraint = new ImplicationConstraint(conditionConstraint,
					new NotConstraint(new LiteralConstraint(varFeature.getFeatureName())));
			addConstraintIfEligible(fm, constraint);
		}
		// case: condition is negated: if both bool then it is an or constraint,
		// otherwise implies
		else if (condition instanceof Not && DecisionModelUtils.isNoneCondition(condition)
				&& action instanceof SelectDecisionAction) {
			String conditionFeatureName = retriveFeatureName(decision);
			ADecision variableDecision = (ADecision) action.getVariable();
			String variableDecisionFeatureName = retriveFeatureName(variableDecision);
			Feature conditionFeature = fm.getFeatureMap().get(conditionFeatureName);
			Feature variableFeature = fm.getFeatureMap().get(variableDecisionFeatureName);
			Constraint constraint = new ImplicationConstraint(new LiteralConstraint(conditionFeature.getFeatureName()),
					new LiteralConstraint(variableFeature.getFeatureName()));
			addConstraintIfEligible(fm, constraint);
		}
		// case: if the condition is a enum value and sets the value of a different
		// decision (Number/String decision)
		else if (!(condition instanceof Not) && action.getVariable() instanceof ADecision
				&& !DecisionModelUtils.isEnumDecisionConstraint(action.getVariable())
				&& action.getValue() instanceof ARangeValue && !action.getValue().equals(BooleanValue.getTrue())
				&& !action.getValue().equals(BooleanValue.getFalse())) {
			// create a new child feature for the feature the rule writes - if it is not a
			// none value
			ADecision variableDecision = (ADecision) action.getVariable();
			// feature name
			String variableDecisionFeatureName = retriveFeatureName(variableDecision);
			// set cardinality of variable feature
			Feature variableFeature = fm.getFeatureMap().get(variableDecisionFeatureName);
			if (DecisionModelUtils.isNoneAction(action)) {
				Constraint constraint = new ImplicationConstraint(conditionConstraint,
						new LiteralConstraint(variableFeature.getFeatureName()));
				addConstraintIfEligible(fm, constraint);
			} else {
				Group featuregroup = null;
				if (variableDecision instanceof EnumDecision) {
					Cardinality cardinality = ((EnumDecision) variableDecision).getCardinality();
					if (cardinality.isAlternative()) {
						featuregroup = variableFeature.getChildren().stream()
								.filter(g -> g.GROUPTYPE.equals(GroupType.ALTERNATIVE)).findFirst().get();
						if (featuregroup == null) {
							featuregroup = new Group(GroupType.ALTERNATIVE);
							variableFeature.getChildren().add(featuregroup);
						}
					} else if (cardinality.isOr()) {
						featuregroup = variableFeature.getChildren().stream()
								.filter(g -> g.GROUPTYPE.equals(GroupType.OR)).findFirst().get();
						if (featuregroup == null) {
							featuregroup = new Group(GroupType.OR);
							variableFeature.getChildren().add(featuregroup);
						}
					}
				}
				Feature valueFeature = fm.getFeatureMap().get(action.getValue().toString());
				// if it was not found, then create it
				if (valueFeature == null) {// && !isNoneAction(action)) {
					valueFeature = new Feature(variableDecisionFeatureName
							+ DefaultDecisionModelTransformationProperties.CONFIGURATION_VALUE_SEPERATOR
							+ action.getValue());
					// add value feature as part of the feature model and as child to the variable
					// feature if it was created
					fm.getFeatureMap().put(valueFeature.getFeatureName(), valueFeature);
					// TODO check grouptype issue
					final GroupType gt = featuregroup.GROUPTYPE;
					variableFeature.getChildren().stream().filter(g -> g.GROUPTYPE.equals(gt)).findFirst().get()
							.getFeatures().add(valueFeature);
//					FeatureUtils.addChild(variableFeature, valueFeature);
				}

				// create and add constraint for setting the condition to set, if is no value
				// condition
				Constraint constraint = null;
				if (action instanceof DisAllowAction) {
					constraint = new ImplicationConstraint(conditionConstraint,
							new NotConstraint(new LiteralConstraint(valueFeature.getFeatureName())));
				} else {
					constraint = new ImplicationConstraint(conditionConstraint,
							new LiteralConstraint(valueFeature.getFeatureName()));
				}
				addConstraintIfEligible(fm, constraint);
			}
		} else if (DecisionModelUtils.isComplexCondition(condition)
				&& (action instanceof SelectDecisionAction || action instanceof DeSelectDecisionAction)) {
			Set<IDecision> conditionDecisions = DecisionModelUtils.retriveConditionDecisions(condition);
			List<LiteralConstraint> conditionLiterals = new ArrayList<>(conditionDecisions.size());
			for (IDecision conditionDecision : conditionDecisions) {
				String conditionFeatureName = retriveFeatureName(conditionDecision);
				Feature conditionFeature = fm.getFeatureMap().get(conditionFeatureName);
				conditionLiterals.add(new LiteralConstraint(conditionFeature.getFeatureName()));
			}
			if (conditionConstraint instanceof AndConstraint) {
//				if () {
//					conditionConstraint = TraVarTUtils.consumeToOrGroup(conditionLiterals, true);
//				} else {
				conditionConstraint = consumeToGroup(conditionLiterals, false, hasNegativeLiteral(conditionConstraint));
				// }
//			} else if (TraVarTUtils.hasNegativeLiteral(conditionConstraint)) {
//				conditionConstraint = TraVarTUtils.consumeToAndGroup(conditionLiterals, true);
			} else {
				conditionConstraint = consumeToGroup(conditionLiterals, true, !hasNegativeLiteral(conditionConstraint));
			}
			ADecision variableDecision = (ADecision) action.getVariable();
			String variableDecisionFeatureName = retriveFeatureName(variableDecision);
			Feature variableFeature = fm.getFeatureMap().get(variableDecisionFeatureName);
			LiteralConstraint variableLiteral = new LiteralConstraint(variableFeature.getFeatureName());
			Constraint constraint;
			if (DecisionModelUtils.isNotCondition(condition)) {
				constraint = new OrConstraint(new LiteralConstraint(conditionConstraint.toString()),
						new LiteralConstraint(variableLiteral.toString()));
			} else if (action instanceof SelectDecisionAction) {
				constraint = new ImplicationConstraint(conditionConstraint, variableLiteral);
			} else if (action instanceof DeSelectDecisionAction) {
//				variableLiteral.positive = false;
				constraint = new ImplicationConstraint(conditionConstraint, new NotConstraint(variableLiteral));
			} else {
//				variableLiteral.positive = false;
				constraint = new OrConstraint(conditionConstraint, new NotConstraint(variableLiteral));
			}
			addConstraintIfEligible(fm, constraint);
		}
	}

	protected static boolean hasNegativeLiteral(final Constraint constraint) {
		boolean subConstraintLiteral = false;
		for (Constraint subconstraint : constraint.getConstraintSubParts()) {
			if (subconstraint instanceof LiteralConstraint) {
				subConstraintLiteral = true;
			}
		}
		if (constraint instanceof NotConstraint && subConstraintLiteral) {
			return true;
		}
		boolean anyNegative = false;
		for (Constraint subconstraint : constraint.getConstraintSubParts()) {
			anyNegative |= hasNegativeLiteral(subconstraint);
		}
		return anyNegative;
	}

	protected static Constraint consumeToGroup(final List<LiteralConstraint> conditionLiterals, final boolean or,
			final boolean negated) {
		if (conditionLiterals.isEmpty()) {
			throw new IllegalArgumentException("Set of decisions is empty.");
		}
		if (conditionLiterals.stream().anyMatch(d -> d == null)) {
			throw new IllegalArgumentException("Set of decisions contains Null.");
		}
		if (conditionLiterals.size() == 1) {
			return negated ? new LiteralConstraint(conditionLiterals.remove(0).getLiteral())
					: new NotConstraint(new LiteralConstraint(conditionLiterals.remove(0).getLiteral()));
		}
		if (or) {
			if (negated) {
				return new OrConstraint(
						new NotConstraint(new LiteralConstraint(conditionLiterals.remove(0).getLiteral())),
						consumeToGroup(conditionLiterals, or, negated));
			} else {
				return new OrConstraint(new LiteralConstraint(conditionLiterals.remove(0).getLiteral()),
						consumeToGroup(conditionLiterals, or, negated));
			}
		}
		if (negated) {
			return new AndConstraint(new NotConstraint(new LiteralConstraint(conditionLiterals.remove(0).getLiteral())),
					consumeToGroup(conditionLiterals, or, negated));
		} else {
			return new AndConstraint(new LiteralConstraint(conditionLiterals.remove(0).getLiteral()),
					consumeToGroup(conditionLiterals, or, negated));
		}
	}

	protected static Constraint deriveConditionConstraint(final FeatureModel fm, final IDecision decision,
			final ICondition condition) {
		if (DecisionModelUtils.isBinaryCondition(condition)) {
			ABinaryCondition binCondition = (ABinaryCondition) condition;
			return deriveConstraintRecursive(fm, binCondition.getLeft(), binCondition.getRight(), condition);
		}
		if (condition instanceof DecisionValueCondition) {
			ARangeValue value = ((DecisionValueCondition) condition).getValue();
			Feature feature = fm.getFeatureMap().get(value.toString());
			return new LiteralConstraint(feature.getFeatureName());
		}
		if (DecisionModelUtils.isNot(condition)) {
			Not notConstraint = (Not) condition;
			ICondition operand = notConstraint.getOperand();
			Constraint literal;
			if (operand instanceof IsSelectedFunction) {
				IsSelectedFunction isSelected = (IsSelectedFunction) operand;
				ADecision d = (ADecision) isSelected.getParameters().get(0);
				String featureName = retriveFeatureName(d);
				Feature feature = fm.getFeatureMap().get(featureName);
				literal = new LiteralConstraint(feature.getFeatureName());
			} else {
				literal = new LiteralConstraint(operand.toString());
			}
			return new NotConstraint(literal);
		}
		if (condition instanceof IsSelectedFunction) {
			IsSelectedFunction isSelected = (IsSelectedFunction) condition;
			ADecision d = (ADecision) isSelected.getParameters().get(0);
			String featureName = retriveFeatureName(d);
			Feature feature = fm.getFeatureMap().get(featureName);
			return new LiteralConstraint(feature.getFeatureName());

		}
		if (condition instanceof StringValue) {
			return new LiteralConstraint(((StringValue) condition).getValue());
		}
		return new LiteralConstraint(condition.toString());
	}

	protected static Constraint deriveConstraintRecursive(final FeatureModel fm, final ICondition left,
			final ICondition right, final ICondition condition) {
		Constraint cLeft = deriveConstraint(fm, left);
		Constraint cRight = deriveConstraint(fm, right);
		if (cLeft == null && cRight != null) {
			return new LiteralConstraint(cRight.toString());
		}
		if (cLeft != null && cRight == null) {
			return new LiteralConstraint(cLeft.toString());
		}
		if (condition instanceof And) {
			return new AndConstraint(new LiteralConstraint(cLeft.toString()), new LiteralConstraint(cRight.toString()));
		}
		return new AndConstraint(new NotConstraint(new LiteralConstraint(cLeft.toString())),
				new NotConstraint(new LiteralConstraint(cRight.toString())));
	}

	protected static Constraint deriveConstraint(final FeatureModel fm, final ICondition node) {
		if (DecisionModelUtils.isBinaryCondition(node)) {
			ABinaryCondition binVis = (ABinaryCondition) node;
			return deriveConstraintRecursive(fm, binVis.getLeft(), binVis.getRight(), binVis);
		}
		if (node instanceof Not) {
			Not notConstraint = (Not) node;
			LiteralConstraint literalC = new LiteralConstraint(notConstraint.getOperand().toString());
			return new NotConstraint(literalC);
		}
		if (node instanceof IsSelectedFunction) {
			IDecision decision = ((IsSelectedFunction) node).getParameters().get(0);
			Feature feature = fm.getFeatureMap().get(decision.getId());
			return new LiteralConstraint(feature.getFeatureName());
		}
		if (node instanceof AFunction) {
			return null;
		}
		return new LiteralConstraint(node.toString());
	}

}
