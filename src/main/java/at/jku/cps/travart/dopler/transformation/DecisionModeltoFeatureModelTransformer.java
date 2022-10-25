package at.jku.cps.travart.dopler.transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.prop4j.Literal;
import org.prop4j.Node;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.common.Prop4JUtils;
import at.jku.cps.travart.core.common.TraVarTUtils;
import at.jku.cps.travart.core.common.exc.ConditionCreationException;
import at.jku.cps.travart.core.common.exc.NotSupportedVariablityTypeException;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
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
import de.ovgu.featureide.fm.core.ExtensionManager.NoSuchExtensionException;
import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelFactory;
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
public class DecisionModeltoFeatureModelTransformer implements IModelTransformer<IDecisionModel> {

	static {
		LibraryManager.registerLibrary(FMCoreLibrary.getInstance());
	}

	private FeatureModel fm;
	private IDecisionModel dm;

	public FeatureModel transform(IDecisionModel arg0, String arg1) throws NotSupportedVariabilityTypeException {
		this.dm = dm;
		this.fm = new FeatureModel();
		try {
			createFeatures();
			createFeatureTree();
			createConstraints();
			TraVarTUtils.deriveFeatureModelRoot(factory, fm, true);
			return fm;
		} catch (NoSuchExtensionException | CircleInConditionExaception | ConditionCreationException e) {
			throw new NotSupportedVariablityTypeException(e);
		}
		return null;
	}

	public IDecisionModel transform(FeatureModel arg0, String arg1) throws NotSupportedVariabilityTypeException {
		// TODO Auto-generated method stub
		return null;
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
				Group enumGroup=null;
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
					constraint= consumeToBinaryCondition(features, new OrConstraint(null,null), false);
				} else if (decision.getCardinality().isAnd()) {
					constraint = consumeToBinaryCondition(features, new AndConstraint(null,null), false);
				}
				addConstraintIfEligible(constraint);
			}
		}
	}

	/**
	 * recursively consumes a list of features into a constraint of Literals.
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
		if(c instanceof OrConstraint) {
			if(negated) {
				return new OrConstraint(new NotConstraint(new LiteralConstraint(features.remove(0).getFeatureName())),consumeToBinaryCondition(features,c,negated));
			}else {
				return new OrConstraint(new LiteralConstraint(features.remove(0).getFeatureName()),consumeToBinaryCondition(features,c,negated));
			}
		}else if(c instanceof AndConstraint) {
			if(negated) {
				return new AndConstraint(new NotConstraint(new LiteralConstraint(features.remove(0).getFeatureName())),consumeToBinaryCondition(features,c,negated));
			}else {
				return new AndConstraint(new LiteralConstraint(features.remove(0).getFeatureName()),consumeToBinaryCondition(features,c,negated));
			}
		}
		return c;
	}

	private void createFeatureTree() {
		for (IDecision decision : dm.getDecisions()) {
			if (!DecisionModelUtils.isEnumDecisionConstraint(decision)) {
				Feature feature = fm.getFeatureMap().get(retriveFeatureName(decision));
				if (getParent(fm, feature) == null) {
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
							FeatureUtils.addChild(parentF, feature);
						}
					}
				}
			}
		}
	}

	/**
	 * Gets the parent of a feature by iterating the featuremap and checking the
	 * children for all features. May take a long time for large models, use with
	 * care.
	 * 
	 * @param fm   the feature model containing the feature
	 * @param feat the feature whose parent is desired
	 * @return the parent feature
	 */
	private static Feature getParent(FeatureModel fm, Feature feat) {
		Optional<Feature> parent = fm.getFeatureMap().values().stream().filter(f -> f.getChildren().stream()
				.flatMap(g -> g.getFeatures().stream()).anyMatch(c -> c.getFeatureName().equals(feat.getFeatureName())))
				.findFirst();
		return parent.isPresent() ? parent.get() : null;
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
					feature.getAttributes().put(DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY
							,new Attribute<String>(DefaultDecisionModelTransformationProperties.PROPERTY_KEY_VISIBILITY_TYPE,decision.getVisiblity().toString()));
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
			Literal disAllowLiteral = new Literal(disAllowFeature.getFeatureName());
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

	private void createExcludesConstraint(final NumberDecision numberDecision, final Literal disAllowLiteral,
			final ARangeValue<Double> value) {
		Feature valueFeature = fm.getFeatureMap()
				.get(numberDecision.getId() + DefaultDecisionModelTransformationProperties.CONFIGURATION_VALUE_SEPERATOR
						+ value.getValue().toString());
		Literal valueLiteral = new Literal(valueFeature.getFeatureName());
		Constraint constraint = new ImplicationConstraint(new LiteralConstraint(valueLiteral.toString()),
				new NotConstraint(new LiteralConstraint(disAllowLiteral.toString())));
		addConstraintIfEligible(constraint);
	}

	private void deriveConstraint(final IDecision decision, final ICondition condition, final IAction action) {
		Constraint conditionNode = deriveConditionNode(decision, condition);
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
			if (getParent(fm,variableFeature) != conditionFeature
					|| !FeatureUtils.isMandatorySet(variableFeature)) {
				Constraint constraint = new ImplicationConstraint(conditionNode, new LiteralConstraint(variableFeature.getFeatureName())));
				addConstraintIfEligible(constraint);
			}
		}
		// case: excludes constraint
		else if (condition instanceof IsSelectedFunction && action.getVariable() instanceof ADecision
				&& action.getValue().equals(BooleanValue.getFalse())) {
			ADecision variableDecision = (ADecision) action.getVariable();
			String featureName = retriveFeatureName(variableDecision);
			Feature feature = fm.getFeatureMap().get(featureName);
			Constraint constraint = new ImplicationConstraint(conditionNode,
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
			Constraint constraint = new ImplicationConstraint(conditionNode,
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
			Constraint constraint = new ImplicationConstraint(
					new LiteralConstraint(conditionFeature.getFeatureName()),new LiteralConstraint(variableFeature.getFeatureName()));
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
				Constraint constraint = new ImplicationConstraint(conditionNode, new LiteralConstraint(variableFeature.getFeatureName())));
				addConstraintIfEligible(constraint);
			} else {
				Feature valueFeature = fm.getFeatureMap().get(action.getValue().toString());
				// if it was not found, then create it
				if (valueFeature == null) {// && !isNoneAction(action)) {
					valueFeature = new Feature(variableDecisionFeatureName
									+ DefaultDecisionModelTransformationProperties.CONFIGURATION_VALUE_SEPERATOR
									+ action.getValue());
					// add value feature as part of the feature model and as child to the variable
					// feature if it was created
					fm.getFeatureMap().put(valueFeature.getFeatureName(), valueFeature);
					//TODO check grouptype issue
					FeatureUtils.addChild(variableFeature, valueFeature);
				}
				if (variableDecision instanceof EnumDecision) {
					Cardinality cardinality = ((EnumDecision) variableDecision).getCardinality();
					if (cardinality.isAlternative()) {
						FeatureUtils.setAlternative(variableFeature);
					} else if (cardinality.isOr()) {
						FeatureUtils.setOr(variableFeature);
					}
				}
				// create and add constraint for setting the condition to set, if is no value
				// condition
				Constraint constraint = null;
				if (action instanceof DisAllowAction) {
					constraint = new ImplicationConstraint(conditionNode,
							new NotConstraint(new LiteralConstraint(valueFeature.getFeatureName())));
				} else {
					constraint = new ImplicationConstraint(conditionNode, new LiteralConstraint(valueFeature.getFeatureName()));
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
			if (conditionNode instanceof AndConstraint) {
//				if () {
//					conditionNode = Prop4JUtils.consumeToOrGroup(conditionLiterals, true);
//				} else {
				conditionNode = Prop4JUtils.consumeToAndGroup(conditionLiterals,
						Prop4JUtils.hasNegativeLiteral(conditionNode));
				// }
//			} else if (Prop4JUtils.hasNegativeLiteral(conditionNode)) {
//				conditionNode = Prop4JUtils.consumeToAndGroup(conditionLiterals, true);
			} else {
				conditionNode = Prop4JUtils.consumeToOrGroup(conditionLiterals,
						!Prop4JUtils.hasNegativeLiteral(conditionNode));
			}
			ADecision variableDecision = (ADecision) action.getVariable();
			String variableDecisionFeatureName = retriveFeatureName(variableDecision);
			Feature variableFeature = fm.getFeatureMap().get(variableDecisionFeatureName);
			LiteralConstraint variableLiteral =new LiteralConstraint(variableFeature.getFeatureName());
			Constraint constraint;
			if (DecisionModelUtils.isNotCondition(condition)) {
				constraint = new OrConstraint(new LiteralConstraint(conditionNode.toString()), new LiteralConstraint(variableLiteral.toString())));
			} else if (action instanceof SelectDecisionAction) {
				constraint = new ImplicationConstraint(conditionNode, variableLiteral);
			} else if (action instanceof DeSelectDecisionAction) {
//				variableLiteral.positive = false;
				constraint = new ImplicationConstraint(conditionNode, new NotConstraint(variableLiteral));
			} else {
//				variableLiteral.positive = false;
				constraint = new OrConstraint(conditionNode, new NotConstraint(variableLiteral));
			}
			addConstraintIfEligible(constraint);
		}
	}

	private void addConstraintIfEligible(final Constraint constraint) {
		if (constraint == null || TraVarTUtils.isInItSelfConstraint(constraint)) {
			return;
		}
		for (Constraint constr : fm.getConstraints()) {
			if (constr.equals(constraint)) {
				return;
			}
		}
		fm.getConstraints().add(constraint);
	}

	private Constraint deriveConditionNode(final IDecision decision, final ICondition condition) {
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
			Not notNode = (Not) condition;
			ICondition operand = notNode.getOperand();
			LiteralConstraint literal;
			if (operand instanceof IsSelectedFunction) {
				IsSelectedFunction isSelected = (IsSelectedFunction) operand;
				ADecision d = (ADecision) isSelected.getParameters().get(0);
				String featureName = retriveFeatureName(d);
				Feature feature = fm.getFeatureMap().get(featureName);
				literal = new LiteralConstraint(feature.getFeatureName());
			} else {
				literal = Prop4JUtils.createLiteral(operand);
			}
			literal.positive = false;
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
			Not notNode = (Not) node;
			LiteralConstraint literalC = new LiteralConstraint(notNode.getOperand().toString());
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

	private void addChild(Feature parent, Feature child) {

	}

}
