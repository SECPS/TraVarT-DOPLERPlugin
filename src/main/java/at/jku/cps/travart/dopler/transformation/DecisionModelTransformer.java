package at.jku.cps.travart.dopler.transformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.logicng.formulas.FormulaFactory;

import at.jku.cps.travart.core.common.IModelTransformer;
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
import at.jku.cps.travart.dopler.decision.model.ABinaryCondition;
import at.jku.cps.travart.dopler.decision.model.ADecision;
import at.jku.cps.travart.dopler.decision.model.AFunction;
import at.jku.cps.travart.dopler.decision.model.ARangeValue;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.IValue;
import at.jku.cps.travart.dopler.decision.model.impl.AllowAction;
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
import at.jku.cps.travart.dopler.decision.model.impl.Or;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;
import at.jku.cps.travart.dopler.decision.model.impl.SelectDecisionAction;
import at.jku.cps.travart.dopler.decision.model.impl.SetValueAction;
import at.jku.cps.travart.dopler.decision.model.impl.StringDecision;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;
import at.jku.cps.travart.dopler.decision.parser.ConditionParser;
import de.ovgu.featureide.fm.core.ExtensionManager.NoSuchExtensionException;
import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.impl.DefaultFeatureModelFactory;
import de.ovgu.featureide.fm.core.base.impl.FMFactoryManager;
import de.ovgu.featureide.fm.core.init.FMCoreLibrary;
import de.ovgu.featureide.fm.core.init.LibraryManager;
import de.vill.main.UVLModelFactory;
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

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DecisionModelTransformer implements IModelTransformer<IDecisionModel> {

	static {
		LibraryManager.registerLibrary(FMCoreLibrary.getInstance());
	}

	private FeatureModel fm;
	private IDecisionModel dm;
	private DecisionModelFactory factory;

	public FeatureModel transform(IDecisionModel arg0, String arg1) throws NotSupportedVariabilityTypeException {
		this.dm = arg0;
		this.fm = new FeatureModel();
		try {
			createFeatures();
			createFeatureTree();
			createConstraints();
			TraVarTUtils.deriveFeatureModelRoot(fm.getFeatureMap(), "VirtualRoot");
			return fm;
		} catch (CircleInConditionException e) {
			throw new NotSupportedVariabilityTypeException(e);
		}
	}

	public IDecisionModel transform(FeatureModel arg0, String arg1) throws NotSupportedVariabilityTypeException {
		this.fm = fm;
		try {
			factory = DecisionModelFactory.getInstance();
			dm = factory.create();
			dm.setName(fm.getRootFeature().getFeatureName());
			convertFeature(fm.getRootFeature());
			convertConstraints(fm.getConstraints());
			convertVisibilityCustomProperties(fm.getFeatureMap().values());
			return dm;
		} catch (CircleInConditionException | ConditionCreationException e) {
			throw new NotSupportedVariabilityTypeException(e);
		}
	}

	private void createFeatures() throws CircleInConditionException {
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
				if (child != null) {
					alternativeGroup.getFeatures().add(child);
				} else {
					child = new Feature(childName);
					fm.getFeatureMap().put(childName, child);
					alternativeGroup.getFeatures().add(child);
				}
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
				addConstraintIfEligible(constraint);
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
	private Constraint consumeToBinaryCondition(List<Feature> features, Constraint c, boolean negated) {
		if (features.isEmpty())
			throw new IllegalArgumentException("Set of decisions is empty.");
		if (features.stream().anyMatch(d -> d == null))
			throw new IllegalArgumentException("Set of decisions contains Null.");
		if (features.size() == 1)
			return negated ? new LiteralConstraint(features.remove(0).getFeatureName())
					: new NotConstraint(new LiteralConstraint(features.remove(0).getFeatureName()));
		if (c instanceof OrConstraint) {
			if (negated) {
				return new OrConstraint(new NotConstraint(new LiteralConstraint(features.remove(0).getFeatureName())),
						consumeToBinaryCondition(features, c, negated));
			} else {
				return new OrConstraint(new LiteralConstraint(features.remove(0).getFeatureName()),
						consumeToBinaryCondition(features, c, negated));
			}
		} else if (c instanceof AndConstraint) {
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

	private Constraint consumeToGroup(List<LiteralConstraint> conditionLiterals, boolean or, boolean negated) {
		if (conditionLiterals.isEmpty())
			throw new IllegalArgumentException("Set of decisions is empty.");
		if (conditionLiterals.stream().anyMatch(d -> d == null))
			throw new IllegalArgumentException("Set of decisions contains Null.");
		if (conditionLiterals.size() == 1)
			return negated ? new LiteralConstraint(conditionLiterals.remove(0).getLiteral())
					: new NotConstraint(new LiteralConstraint(conditionLiterals.remove(0).getLiteral()));
		if (or) {
			if (negated) {
				return new OrConstraint(
						new NotConstraint(new LiteralConstraint(conditionLiterals.remove(0).getLiteral())),
						consumeToGroup(conditionLiterals, or, negated));
			} else {
				return new OrConstraint(new LiteralConstraint(conditionLiterals.remove(0).getLiteral()),
						consumeToGroup(conditionLiterals, or, negated));
			}
		} else {
			if (negated) {
				return new AndConstraint(
						new NotConstraint(new LiteralConstraint(conditionLiterals.remove(0).getLiteral())),
						consumeToGroup(conditionLiterals, or, negated));
			} else {
				return new AndConstraint(new LiteralConstraint(conditionLiterals.remove(0).getLiteral()),
						consumeToGroup(conditionLiterals, or, negated));
			}
		}
	}

	private void createFeatureTree() {
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

	private String retriveFeatureName(final IDecision decision) {
		return DecisionModelUtils.retriveFeatureName(decision, decision.getType() == ADecision.DecisionType.ENUM);
	}

	private void createConstraints() {
		for (IDecision decision : dm.getDecisions()) {
			if (!DecisionModelUtils.isEnumDecisionConstraint(decision)) {
				// first store complex visibility condition in properties, to avoid losing the
				// information
				if (DecisionModelUtils.isComplexVisibilityCondition(decision.getVisiblity())
						&& !DecisionModelUtils.isMandatoryVisibilityCondition(decision.getVisiblity())) {
					Feature feature = fm.getFeatureMap().get(retriveFeatureName(decision));
					// add visibility as property for restoring them later if necessary
					feature.getAttributes().put(DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY,
							new Attribute<String>(
									DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY_TYPE,
									decision.getVisiblity().toString()));
				}
				// second transform constraints from the rules
				for (Object o : decision.getRules()) {
					Rule rule = (Rule) o;
					if (!DecisionModelUtils.isInItSelfRule(rule)) {
						if (DecisionModelUtils.isCompareCondition(rule.getCondition())) {
							deriveCompareConstraints(decision, rule.getCondition(), rule.getAction());
						} else {
							deriveConstraint(decision, rule.getCondition(), rule.getAction());
						}
					}
				}
			}
		}
	}

	private void deriveCompareConstraints(final IDecision decision, final ICondition condition, final IAction action) {
		if (decision.getType() == ADecision.DecisionType.NUMBER && action instanceof DisAllowAction) {
			NumberDecision numberDecision = (NumberDecision) decision;
			ABinaryCondition binCondition = (ABinaryCondition) condition;
			ARangeValue<Double> conditionValue = (ARangeValue<Double>) binCondition.getRight();
			Feature disAllowFeature = fm.getFeatureMap().get(((DisAllowAction) action).getValue().toString());
			LiteralConstraint disAllowLiteral = new LiteralConstraint(disAllowFeature.getFeatureName());
			if (condition instanceof Equals) {
				createExcludesConstraint(numberDecision, disAllowLiteral, conditionValue);
			} else if (condition instanceof Greater) {
				Set<ARangeValue<Double>> values = numberDecision.getRange().stream()
						.filter(v -> v.getValue() > conditionValue.getValue()).collect(Collectors.toSet());
				for (ARangeValue<Double> value : values) {
					createExcludesConstraint(numberDecision, disAllowLiteral, value);
				}
			} else if (condition instanceof Less) {
				Set<ARangeValue<Double>> values = numberDecision.getRange().stream()
						.filter(v -> v.getValue() < conditionValue.getValue()).collect(Collectors.toSet());
				for (ARangeValue<Double> value : values) {
					createExcludesConstraint(numberDecision, disAllowLiteral, value);
				}
			} else if (condition instanceof GreaterEquals) {
				Set<ARangeValue<Double>> values = numberDecision.getRange().stream()
						.filter(v -> v.getValue() >= conditionValue.getValue()).collect(Collectors.toSet());
				for (ARangeValue<Double> value : values) {
					createExcludesConstraint(numberDecision, disAllowLiteral, value);
				}
			} else if (condition instanceof LessEquals) {
				Set<ARangeValue<Double>> values = numberDecision.getRange().stream()
						.filter(v -> v.getValue() <= conditionValue.getValue()).collect(Collectors.toSet());
				for (ARangeValue<Double> value : values) {
					createExcludesConstraint(numberDecision, disAllowLiteral, value);
				}
			}
		}
	}

	private void createExcludesConstraint(final NumberDecision numberDecision, final LiteralConstraint disAllowLiteral,
			final ARangeValue<Double> value) {
		Feature valueFeature = fm.getFeatureMap()
				.get(numberDecision.getId() + DefaultDecisionModelTransformationProperties.CONFIGURATION_VALUE_SEPERATOR
						+ value.getValue().toString());
		LiteralConstraint valueLiteral = new LiteralConstraint(valueFeature.getFeatureName());
		Constraint constraint = new ImplicationConstraint(new LiteralConstraint(valueLiteral.toString()),
				new NotConstraint(new LiteralConstraint(disAllowLiteral.toString())));
		addConstraintIfEligible(constraint);
	}

	private void deriveConstraint(final IDecision decision, final ICondition condition, final IAction action) {
		Constraint conditionConstraint = deriveConditionConstraint(decision, condition);
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
				addConstraintIfEligible(constraint);
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
			addConstraintIfEligible(constraint);
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
			addConstraintIfEligible(constraint);
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
			addConstraintIfEligible(constraint);
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
				addConstraintIfEligible(constraint);
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
				addConstraintIfEligible(constraint);
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
			addConstraintIfEligible(constraint);
		}
	}

	private boolean hasNegativeLiteral(Constraint constraint) {
		boolean subConstraintLiteral = false;
		for (Constraint subconstraint : constraint.getConstraintSubParts()) {
			if (subconstraint instanceof LiteralConstraint)
				subConstraintLiteral = true;
		}
		if ((constraint instanceof NotConstraint) && subConstraintLiteral) {
			return true;
		} else {
			boolean anyNegative = false;
			for (Constraint subconstraint : constraint.getConstraintSubParts()) {
				anyNegative |= hasNegativeLiteral(subconstraint);
			}
			return anyNegative;
		}
	}

	private void addConstraintIfEligible(final Constraint constraint) {

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

	private boolean isInItSelfConstraint(Constraint constraint) {
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

	private Constraint deriveConditionConstraint(final IDecision decision, final ICondition condition) {
		if (DecisionModelUtils.isBinaryCondition(condition)) {
			ABinaryCondition binCondition = (ABinaryCondition) condition;
			return deriveConstraintRecursive(binCondition.getLeft(), binCondition.getRight(), condition);
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
			literal = new NotConstraint(literal);
			return literal;
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

	private Constraint deriveConstraintRecursive(final ICondition left, final ICondition right,
			final ICondition condition) {
		Constraint cLeft = deriveConstraint(left);
		Constraint cRight = deriveConstraint(right);
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

	private Constraint deriveConstraint(final ICondition node) {
		if (DecisionModelUtils.isBinaryCondition(node)) {
			ABinaryCondition binVis = (ABinaryCondition) node;
			return deriveConstraintRecursive(binVis.getLeft(), binVis.getRight(), binVis);
		}
		if (node instanceof Not) {
			Not notConstraint = (Not) node;
			LiteralConstraint literalC = new LiteralConstraint(notConstraint.getOperand().toString());
			NotConstraint notC = new NotConstraint(literalC);
			return notC;
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

	private void convertFeature(final Feature feature) throws NotSupportedVariabilityTypeException {
		if (!feature.getFeatureName().equals(DefaultModelTransformationProperties.ARTIFICIAL_MODEL_NAME)) {
			BooleanDecision decision = factory.createBooleanDecision(feature.getFeatureName());
			dm.add(decision);
			decision.setQuestion(feature.getFeatureName() + "?");
			if (TraVarTUtils.isEnumerationType(feature)) {
				createEnumDecisions(feature);
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
				convertFeature(child);
			}
		}
	}

	private boolean hasVirtualParent(final Feature feature) {
		Feature parent = feature.getParentFeature();
		if (parent != null) {
			return parent.getFeatureName().equals(DefaultModelTransformationProperties.ARTIFICIAL_MODEL_NAME);
		}
		return false;
	}

	private void createEnumDecisions(final Feature feature) throws NotSupportedVariabilityTypeException {
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
			convertFeature(optionFeature);
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
		if (!(feature.getParentFeature() == null) && !feature.getParentGroup().GROUPTYPE.equals(GroupType.MANDATORY)) {
			Rule rule = new Rule(new Not(new IsSelectedFunction(parent)),
					new SetValueAction(enumDecision, enumDecision.getNoneOption()));
			parent.addRule(rule);
			updateRules(parent, rule);
		}
	}

	private void convertConstraints(final List<Constraint> constraints)
			throws CircleInConditionException, ConditionCreationException {
		for (Constraint constraint : constraints) {
			convertConstraintRec(constraint);
		}
	}

	private void convertConstraintRec(final Constraint node)
			throws CircleInConditionException, ConditionCreationException {
		// create a CNF from nodes enables separating the concerns how to transform the
		// different groups.
		// A requires B <=> CNF: Not(A) or B
		// A excludes B <=> CNF: Not(A) or Not(B)

		Constraint cnfConstraint = TraVarTUtils
				.buildConstraintFromFormula(TraVarTUtils.buildFormulaFromConstraint(node, new FormulaFactory()).cnf());
		if (TraVarTUtils.isComplexConstraint(cnfConstraint)) {
			for (Constraint child : cnfConstraint.getConstraintSubParts()) {
				convertConstraintRec(child);
			}
		} else {
			convertConstraint(cnfConstraint);
		}
	}

	private void deriveUnidirectionalRules(final Constraint cnfConstraint)
			throws CircleInConditionException, ConditionCreationException {
		Constraint negative = TraVarTUtils.getFirstNegativeLiteral(cnfConstraint);
		String feature = ((LiteralConstraint) negative).getLiteral();
		IDecision decision = dm.get(feature);
		if (decision.getType() == ADecision.DecisionType.BOOLEAN) {
			BooleanDecision target = (BooleanDecision) decision;
			Set<Constraint> positiveLiterals = TraVarTUtils.getPositiveLiterals(cnfConstraint);
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

	private void convertConstraint(final Constraint cnfConstraint)
			throws CircleInConditionException, ConditionCreationException, at.jku.cps.travart.dopler.decision.exc.ConditionCreationException {
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
			deriveRequiresRules(cnfConstraint);
		} else if (TraVarTUtils.isExcludes(cnfConstraint)) {
			deriveExcludeRules(cnfConstraint);
		} else if (TraVarTUtils.countLiterals(cnfConstraint) == 2 && TraVarTUtils.hasNegativeLiteral(cnfConstraint)
				&& TraVarTUtils.countNegativeLiterals(cnfConstraint) == 1) {
			deriveUnidirectionalRules(cnfConstraint);
		} else if (TraVarTUtils.countLiterals(cnfConstraint) > 1 && TraVarTUtils.hasNegativeLiteral(cnfConstraint)
				&& TraVarTUtils.countNegativeLiterals(cnfConstraint) == 1) {
			deriveUnidirectionalRules(cnfConstraint);
		} else if (TraVarTUtils.countLiterals(cnfConstraint) > 1 && TraVarTUtils.hasPositiveLiteral(cnfConstraint)
				&& TraVarTUtils.countPositiveLiterals(cnfConstraint) == 1) {
			Constraint positive = TraVarTUtils.getFirstPositiveLiteral(cnfConstraint);
			String feature = ((LiteralConstraint) positive).getLiteral();
			IDecision decision = dm.get(feature);
			if (decision.getType() == ADecision.DecisionType.BOOLEAN) {
				BooleanDecision target = (BooleanDecision) decision;
				Set<Constraint> negativeLiterals = TraVarTUtils.getNegativeLiterals(cnfConstraint);
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
		} else if (TraVarTUtils.countLiterals(cnfConstraint) > 1
				&& TraVarTUtils.countLiterals(cnfConstraint) == TraVarTUtils.countPositiveLiterals(cnfConstraint)
				&& cnfConstraint instanceof OrConstraint) {
			Set<Constraint> literals = TraVarTUtils.getLiterals(cnfConstraint);
			List<IDecision> literalDecisions = findDecisionsForLiterals(literals);
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
			List<IDecision> literalDecisions = findDecisionsForLiterals(literals);
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
				String feature = ((LiteralConstraint) cnfConstraint).getLiteral();
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
			ICondition condition = deriveConditionFromConstraint(cnfConstraint);
			// create implies with root decision to set to false
			BooleanDecision rootDecision = (BooleanDecision) dm.get(fm.getRootFeature().getFeatureName());
			Rule rule = new Rule(new Not(condition), new DeSelectDecisionAction(rootDecision));
			rootDecision.addRule(rule);
			updateRules(rootDecision, rule);
		}
	}

	private void deriveRequiresRules(final Constraint cnfConstraint) {
		Constraint sourceLiteral = TraVarTUtils.getFirstNegativeLiteral(cnfConstraint);
		Constraint targetLiteral = TraVarTUtils.getFirstPositiveLiteral(cnfConstraint);
		if (TraVarTUtils.isLiteral(sourceLiteral) && TraVarTUtils.isLiteral(targetLiteral)) {
			String sourceFeature = ((LiteralConstraint) sourceLiteral).getLiteral();
			String targetFeature = ((LiteralConstraint) targetLiteral).getLiteral();
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

	private boolean hasVirtualParent(final IDecision decision) {
		Feature feature = fm.getFeatureMap().get(
				DecisionModelUtils.retriveFeatureName(decision, decision.getType() == ADecision.DecisionType.BOOLEAN));
		Feature parent = feature.getParentFeature();
		return parent.getFeatureName().equals(DefaultModelTransformationProperties.ARTIFICIAL_MODEL_NAME);
	}

	private void deriveExcludeRules(final Constraint cnfConstraint) {
		// excludes constraints are bidirectional by default, therefore direction for
		// defined constraint is negligible, but we need two rules
		Constraint sourceLiteral = TraVarTUtils.getLeftConstraint(cnfConstraint);
		Constraint targetLiteral = TraVarTUtils.getRightConstraint(cnfConstraint);
		if (TraVarTUtils.isLiteral(sourceLiteral) && TraVarTUtils.isLiteral(targetLiteral)) {
			String sourceFeature = ((LiteralConstraint) sourceLiteral).getLiteral();
			String targetFeature = ((LiteralConstraint) targetLiteral).getLiteral();
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

	private ICondition deriveConditionFromConstraint(final Constraint node) throws ConditionCreationException {
		if (node instanceof AndConstraint) {
			List<Constraint> nodes = new ArrayList<Constraint>();
			nodes.addAll(node.getConstraintSubParts());
			ICondition first = deriveConditionFromConstraint(nodes.remove(0));
			ICondition second = deriveConditionFromConstraint(nodes.remove(0));
			And and = new And(first, second);
			while (!nodes.isEmpty()) {
				ICondition next = deriveConditionFromConstraint(nodes.remove(0));
				and = new And(and, next);
			}
			return and;
		}
		if (node instanceof OrConstraint) {
			List<Constraint> nodes = new ArrayList<Constraint>();
			nodes.addAll(node.getConstraintSubParts());
			ICondition first = deriveConditionFromConstraint(nodes.remove(0));
			ICondition second = deriveConditionFromConstraint(nodes.remove(0));
			Or or = new Or(first, second);
			while (!nodes.isEmpty()) {
				ICondition next = deriveConditionFromConstraint(nodes.remove(0));
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

	private EnumDecision findEnumDecisionWithVisiblityCondition(final IDecision decision) {
		// TODO: order causes issues; in one model (financial services) it is needed the
		// other way around
		// TODO: in another model as it is right now (Ubuntu_14_1)
		// TODO: which is the best way to find out
		List<EnumDecision> decisions = ((DecisionModel)dm).findWithVisibility(decision).stream()
				.filter(d -> (d.getType() == ADecision.DecisionType.ENUM)).map(d -> (EnumDecision) d)
				.collect(Collectors.toList());
		if (!decisions.isEmpty()) {
			return decisions.remove(0);
		}
		return null;
	}

	private EnumDecision findEnumDecisionByRangeValue(final IDecision decision) {
		List<EnumDecision> decisions = new ArrayList<>(((DecisionModel)dm).findWithRangeValue(decision));
		if (!decisions.isEmpty()) {
			return decisions.remove(0);
		}
		return null;
	}

	private List<IDecision> findDecisionsForLiterals(final Set<Constraint> literals) {
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

	private boolean isEnumSubFeature(final Feature feature) {
		// works as each tree in FeatureIDE has only one cardinality across all
		// sub-features.
		Feature parent = feature.getParentFeature();
		return parent != null && TraVarTUtils.isEnumerationType(parent);
	}

	private boolean isEnumSubFeature(final IDecision decision) {
		Feature feature = fm.getFeatureMap().get(
				DecisionModelUtils.retriveFeatureName(decision, decision.getType() == ADecision.DecisionType.ENUM));
		return isEnumSubFeature(feature);
	}

	private boolean isEnumFeature(final IDecision decision) {
		Feature feature = fm.getFeatureMap().get(
				DecisionModelUtils.retriveFeatureName(decision, decision.getType() == ADecision.DecisionType.ENUM));
		return TraVarTUtils.isEnumerationType(feature);
	}

	private void defineCardinality(final EnumDecision decision, final Feature feature) {
		Cardinality cardinality = null;
		//TODO probably needs larger refactor due to group cardinalities
		if (feature.getChildren().stream().anyMatch(g->g.GROUPTYPE.equals(GroupType.OR))) {
			cardinality = new Cardinality(1, feature.getChildren().stream().flatMap(g -> g.getFeatures().stream())
					.collect(Collectors.toList()).size());
		} else if (feature.getChildren().stream().anyMatch(g->g.GROUPTYPE.equals(GroupType.ALTERNATIVE))) {
			cardinality = new Cardinality(1, 1);
		} else {
			cardinality = new Cardinality(0, feature.getChildren().stream().flatMap(g -> g.getFeatures().stream())
					.collect(Collectors.toList()).size());
		}
		decision.setCardinality(cardinality);
	}

	private void defineRange(final EnumDecision decision, final Feature feature)
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
		if (!feature.getParentGroup().GROUPTYPE.equals(GroupType.MANDATORY) || isEnumSubFeature(feature)) {
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

	private void convertVisibilityCustomProperties(final Collection<Feature> features) {
		for (Feature feature : features) {
			if (feature.getAttributes().containsKey(DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY)){//,DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY_TYPE)) {
				ConditionParser conditionParser = new ConditionParser(dm);
				String visbility = feature.getAttributes().get(
						DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY).toString();//,DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY_TYPE);
				ICondition visiblity = conditionParser.parse(visbility);
				IDecision decision = dm.get(feature.getFeatureName());
				decision.setVisibility(visiblity);
			}
		}
	}
}
