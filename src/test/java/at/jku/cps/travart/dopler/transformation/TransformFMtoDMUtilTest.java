package at.jku.cps.travart.dopler.transformation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.core.helpers.TraVarTUtils;
import at.jku.cps.travart.core.transformation.DefaultModelTransformationProperties;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.CircleInConditionException;
import at.jku.cps.travart.dopler.decision.exc.ConditionCreationException;
import at.jku.cps.travart.dopler.decision.factory.impl.DecisionModelFactory;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.AllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.And;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.DeSelectDecisionAction;
import at.jku.cps.travart.dopler.decision.model.impl.DecisionValueCondition;
import at.jku.cps.travart.dopler.decision.model.impl.DisAllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.EnumDecision;
import at.jku.cps.travart.dopler.decision.model.impl.IsSelectedFunction;
import at.jku.cps.travart.dopler.decision.model.impl.Not;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;
import at.jku.cps.travart.dopler.decision.model.impl.SelectDecisionAction;
import at.jku.cps.travart.dopler.decision.model.impl.SetValueAction;
import at.jku.cps.travart.dopler.transformation.roundtrip.TransformFMtoDMUtil;
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
import de.vill.model.constraint.ParenthesisConstraint;

@SuppressWarnings("rawtypes")
class TransformFMtoDMUtilTest {
	DecisionModelFactory factory;
	IDecisionModel dm;
	FeatureModel fm;
	String root = "root";
	String childA = "childA";
	String childB = "childB";
	String childC = "childC";
	Feature rootFeature;
	Feature childAFeature;
	Feature childBFeature;
	Feature childCFeature;
	Set<Rule> controlSet;
	Group orGroup;

	IDecision childADec;
	IDecision childBDec;
	IDecision childCDec;
	IDecision rootDec;
	IDecision root0Dec;

	@BeforeEach
	void setUp() throws Exception {
		factory = DecisionModelFactory.getInstance();
		dm = factory.create();
		fm = new FeatureModel();
		orGroup = new Group(GroupType.OR);
		rootFeature = new Feature(root);
		childAFeature = new Feature(childA);
		childBFeature = new Feature(childB);
		childCFeature = new Feature(childC);
		orGroup.getFeatures().add(childAFeature);
		orGroup.getFeatures().add(childBFeature);
		orGroup.getFeatures().add(childCFeature);
		rootFeature.getChildren().add(orGroup);
		fm.setRootFeature(rootFeature);
		fm.getFeatureMap().putAll(TraVarTUtils.getFeatureMapFromRoot(rootFeature));
		controlSet = new HashSet<>();
	}

	@AfterEach
	void clear() {
		childADec = null;
		childBDec = null;
		childCDec = null;
		rootDec = null;
		root0Dec = null;
	}

	private void getDecisions(final IDecisionModel dm) {
		childADec = dm.get(childA);
		childBDec = dm.get(childB);
		childCDec = dm.get(childC);
		rootDec = dm.get(rootFeature.getFeatureName());
		root0Dec = dm.get("root#0");
	}

	@Test
	void testConvertFeatureSingleMandatoryGroup() throws NotSupportedVariabilityTypeException {
		String root = "root";
		String child = "child";
		Feature rootFeature = new Feature(root);
		Group manGroup = new Group(GroupType.MANDATORY);
		rootFeature.getChildren().add(manGroup);
		manGroup.getFeatures().add(new Feature(child));
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		IDecision dmRoot = dm.get(root);
		IDecision dmChild = dm.get(child);
		assertNotNull(dmRoot);
		assertEquals(ICondition.TRUE, dmRoot.getVisiblity());
		assertNotNull(dmChild);
		assertEquals(new And(ICondition.FALSE, new IsSelectedFunction(dmRoot)), dmChild.getVisiblity());
	}

	@Test
	void testConvertFeatureSingleOptionalGroup() throws NotSupportedVariabilityTypeException {
		String root = "root";
		String child = "child";
		Feature rootFeature = new Feature(root);
		Group manGroup = new Group(GroupType.OPTIONAL);
		rootFeature.getChildren().add(manGroup);
		manGroup.getFeatures().add(new Feature(child));
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		assertNotNull(dm.get(root));
		assertNotNull(dm.get(child));
	}

	@Test
	void testConvertFeatureSingleOrGroup() throws NotSupportedVariabilityTypeException {
		String root = "root";
		String child = "child";
		Feature rootFeature = new Feature(root);
		Group manGroup = new Group(GroupType.OR);
		rootFeature.getChildren().add(manGroup);
		manGroup.getFeatures().add(new Feature(child));
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		IDecision dmRoot = dm.get(root);
		IDecision dmChild = dm.get(child);
		assertNotNull(dmRoot);
		assertEquals(ICondition.TRUE, dmRoot.getVisiblity());
		assertNotNull(dmChild);
		assertEquals(ICondition.FALSE, dmChild.getVisiblity());
	}

	@Test
	void testConvertFeatureVirtualParent() throws NotSupportedVariabilityTypeException {
		String root = DefaultModelTransformationProperties.ARTIFICIAL_MODEL_NAME;
		String child = "child";
		Feature rootFeature = new Feature(root);
		Group manGroup = new Group(GroupType.MANDATORY);
		rootFeature.getChildren().add(manGroup);
		Feature childFeature = new Feature(child);
		manGroup.getFeatures().add(childFeature);
		TransformFMtoDMUtil.convertFeature(factory, dm, childFeature);
		IDecision dmRoot = dm.get(root);
		IDecision dmChild = dm.get(child);
		// virtual root should be null here
		assertNull(dmRoot);
		assertNotNull(dmChild);
		assertEquals(ICondition.TRUE, dmChild.getVisiblity());
	}

	@Test
	void testHasVirtualParentFeatureTrue() {
		String root = DefaultModelTransformationProperties.ARTIFICIAL_MODEL_NAME;
		String child = "child";
		Feature rootFeature = new Feature(root);
		Group manGroup = new Group(GroupType.MANDATORY);
		rootFeature.getChildren().add(manGroup);
		Feature childFeature = new Feature(child);
		manGroup.getFeatures().add(childFeature);
		assertTrue(TransformFMtoDMUtil.hasVirtualParent(childFeature));
	}

	@Test
	void testHasVirtualParentFeatureNoParent() {
		String child = "child";
		Feature childFeature = new Feature(child);
		assertFalse(TransformFMtoDMUtil.hasVirtualParent(childFeature));
	}

	@Test
	void testHasVirtualParentFeatureFalse() {
		String root = "root";
		String child = "child";
		Feature rootFeature = new Feature(root);
		Group manGroup = new Group(GroupType.OR);
		rootFeature.getChildren().add(manGroup);
		Feature childFeature = new Feature(child);
		manGroup.getFeatures().add(childFeature);
		assertFalse(TransformFMtoDMUtil.hasVirtualParent(childFeature));
	}

	@Test
	void testCreateEnumDecisionsMandatory() throws NotSupportedVariabilityTypeException {
		String root = "root";
		String rootOptChild = "optChild";
		String[] optChildren = { "child0", "child1", "child2" };
		Feature rootFeature = new Feature(root);
		Feature rootOptChildFeature = new Feature(rootOptChild);
		Group optGroup = new Group(GroupType.OPTIONAL);
		optGroup.getFeatures().add(rootOptChildFeature);
		rootFeature.getChildren().add(optGroup);
		Group mandGroup = new Group(GroupType.MANDATORY);
		rootOptChildFeature.getChildren().add(mandGroup);
		Feature[] childFeat = new Feature[optChildren.length];
		for (int i = 0; i < optChildren.length; i++) {
			childFeat[i] = new Feature(optChildren[i]);
			mandGroup.getFeatures().add(childFeat[i]);
		}
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		IDecision rootOptChildDec = dm.get(rootOptChild);
		IDecision[] childList = new IDecision[optChildren.length];
		for (int i = 0; i < optChildren.length; i++) {
			childList[i] = dm.get(optChildren[i]);
		}
		assertNotNull(rootOptChildDec.getRules());
		for (int i = 0; i < optChildren.length; i++) {
			assertTrue(rootOptChildDec.getRules().contains(
					new Rule(new IsSelectedFunction(rootOptChildDec), new SelectDecisionAction(childList[i]))));
		}
	}

	@Test
	void testCreateEnumDecisionsOptional() throws NotSupportedVariabilityTypeException {
		String root = "root";
		String rootOptChild = "optChild";
		String[] optChildren = { "child0", "child1", "child2" };
		Feature rootFeature = new Feature(root);
		Feature rootOptChildFeature = new Feature(rootOptChild);
		Group optGroup = new Group(GroupType.OPTIONAL);
		optGroup.getFeatures().add(rootOptChildFeature);
		rootFeature.getChildren().add(optGroup);
		Group altGroup = new Group(GroupType.ALTERNATIVE);
		rootOptChildFeature.getChildren().add(altGroup);
		Feature[] childFeat = new Feature[optChildren.length];
		for (int i = 0; i < optChildren.length; i++) {
			childFeat[i] = new Feature(optChildren[i]);
			altGroup.getFeatures().add(childFeat[i]);
		}
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		IDecision rootOptChildDec = dm.get(rootOptChild);
		EnumDecision rootOptChildEnumDec = (EnumDecision) dm.get(rootOptChild + "#0");
		IDecision[] childList = new IDecision[optChildren.length];
		for (int i = 0; i < childList.length; i++) {
			childList[i] = dm.get(optChildren[i]);
		}
		assertNotNull(rootOptChildEnumDec.getRules());
		assertEquals(new IsSelectedFunction(rootOptChildDec), rootOptChildEnumDec.getVisiblity());
	}

	@Test
	void testConvertConstraintRecExcludes()
			throws NotSupportedVariabilityTypeException, CircleInConditionException, ConditionCreationException {
		// A => !B
		Constraint implConstraint = new ImplicationConstraint(new LiteralConstraint(childA),
				new NotConstraint(new LiteralConstraint(childB)));
		fm.getConstraints().add(implConstraint);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		TransformFMtoDMUtil.convertConstraintRec(factory, dm, fm, implConstraint);
		getDecisions(dm);
		assertFalse(childADec.getRules().isEmpty());
		// A disallows B
		assertEquals(
				new Rule(new Not(new IsSelectedFunction(childADec)),
						new AllowAction(root0Dec, root0Dec.getRangeValue(childB))),
				childADec.getRules().iterator().next());
		// B disallows A
		assertEquals(
				new Rule(new Not(new IsSelectedFunction(childBDec)),
						new AllowAction(root0Dec, root0Dec.getRangeValue(childA))),
				childBDec.getRules().iterator().next());

		Set<Rule> ruleSet = root0Dec.getRules();
		controlSet.add(new Rule(new DecisionValueCondition(root0Dec, root0Dec.getRangeValue(childA)),
				new SelectDecisionAction(dm.get(childA))));
		controlSet.add(new Rule(new DecisionValueCondition(root0Dec, root0Dec.getRangeValue(childB)),
				new SelectDecisionAction(dm.get(childB))));
		controlSet.add(new Rule(new DecisionValueCondition(root0Dec, root0Dec.getRangeValue(childC)),
				new SelectDecisionAction(dm.get(childC))));
		assertEquals(controlSet, ruleSet);
	}

	@Test
	void testConvertConstraintRecRequires()
			throws NotSupportedVariabilityTypeException, CircleInConditionException, ConditionCreationException {
		// A => B
		Constraint implConstraint = new ImplicationConstraint(new LiteralConstraint(childA),
				new LiteralConstraint(childB));
		fm.getConstraints().add(implConstraint);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		TransformFMtoDMUtil.convertConstraintRec(factory, dm, fm, implConstraint);
		getDecisions(dm);
		// A requires B
		assertEquals(
				new Rule(new IsSelectedFunction(childADec),
						new SetValueAction(root0Dec, root0Dec.getRangeValue(childB))),
				childADec.getRules().iterator().next());

		Set<Rule> ruleSet = root0Dec.getRules();
		controlSet.add(new Rule(new DecisionValueCondition(root0Dec, root0Dec.getRangeValue(childA)),
				new SelectDecisionAction(dm.get(childA))));
		controlSet.add(new Rule(new DecisionValueCondition(root0Dec, root0Dec.getRangeValue(childB)),
				new SelectDecisionAction(dm.get(childB))));
		controlSet.add(new Rule(new DecisionValueCondition(root0Dec, root0Dec.getRangeValue(childC)),
				new SelectDecisionAction(dm.get(childC))));
		assertEquals(controlSet, ruleSet);
	}

	@Test
	void testConvertConstraintRecSingleRequires()
			throws NotSupportedVariabilityTypeException, CircleInConditionException, ConditionCreationException {
		// child A is required
		Constraint implConstraint = new LiteralConstraint(childA);
		fm.getConstraints().add(implConstraint);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		TransformFMtoDMUtil.convertConstraintRec(factory, dm, fm, implConstraint);
		getDecisions(dm);
		// A disallows B
		assertEquals(new Rule(new IsSelectedFunction(rootDec), new SelectDecisionAction(childADec)),
				rootDec.getRules().iterator().next());
	}

	@Test
	void testConvertConstraintRecDoubleRequires()
			throws NotSupportedVariabilityTypeException, CircleInConditionException, ConditionCreationException {
		// A and B
		Constraint implConstraint = new AndConstraint(new LiteralConstraint(childA), new LiteralConstraint(childB));
		fm.getConstraints().add(implConstraint);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		TransformFMtoDMUtil.convertConstraintRec(factory, dm, fm, implConstraint);
		getDecisions(dm);

		Set<Rule> ruleSet = root0Dec.getRules();
		controlSet.add(new Rule(new DecisionValueCondition(root0Dec, root0Dec.getRangeValue(childA)),
				new SelectDecisionAction(childADec)));
		controlSet.add(new Rule(new DecisionValueCondition(root0Dec, root0Dec.getRangeValue(childB)),
				new SelectDecisionAction(childBDec)));
		controlSet.add(new Rule(new DecisionValueCondition(root0Dec, root0Dec.getRangeValue(childC)),
				new SelectDecisionAction(childCDec)));
		assertEquals(controlSet, ruleSet);
	}

	@Test
	void testConvertConstraintRecSingleExcludes()
			throws NotSupportedVariabilityTypeException, CircleInConditionException, ConditionCreationException {
		// not A
		Constraint notConstraint = new NotConstraint(new LiteralConstraint(childA));
		fm.getConstraints().add(notConstraint);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		TransformFMtoDMUtil.convertConstraintRec(factory, dm, fm, notConstraint);
		getDecisions(dm);
		assertEquals(new Rule(new IsSelectedFunction(rootDec), new DeSelectDecisionAction((BooleanDecision) childADec)),
				rootDec.getRules().iterator().next());
	}

	@Test
	void testConvertConstraintRecComplexConstraintSplit()
			throws NotSupportedVariabilityTypeException, CircleInConditionException, ConditionCreationException {
		// A => B and !C
		Constraint implConstraint = new ImplicationConstraint(new LiteralConstraint(childA),
				new AndConstraint(new LiteralConstraint(childB), new NotConstraint(new LiteralConstraint(childC))));
		fm.getConstraints().add(implConstraint);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		TransformFMtoDMUtil.convertConstraintRec(factory, dm, fm, implConstraint);
		getDecisions(dm);

		Set<Rule> ruleSet = childADec.getRules();
		controlSet.add(new Rule(new Not(new IsSelectedFunction(childADec)),
				new AllowAction(root0Dec, root0Dec.getRangeValue(childC))));
		controlSet.add(new Rule(new IsSelectedFunction(childADec),
				new DisAllowAction(root0Dec, root0Dec.getRangeValue(childC))));
		controlSet.add(new Rule(new IsSelectedFunction(childADec),
				new SetValueAction(root0Dec, root0Dec.getRangeValue(childB))));

		assertEquals(controlSet, ruleSet);
	}

	@Test
	void testConvertConstraintRecComplexConstraintDoubleRequires()
			throws NotSupportedVariabilityTypeException, CircleInConditionException, ConditionCreationException {
		// A => B and C
		Constraint implConstraint = new ImplicationConstraint(new LiteralConstraint(childA),
				new AndConstraint(new LiteralConstraint(childB), new LiteralConstraint(childC)));
		fm.getConstraints().add(implConstraint);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		TransformFMtoDMUtil.convertConstraintRec(factory, dm, fm, implConstraint);
		getDecisions(dm);

		Set<Rule> ruleSet = childADec.getRules();
		controlSet.add(new Rule(new IsSelectedFunction(childADec),
				new SetValueAction(root0Dec, root0Dec.getRangeValue(childC))));
		controlSet.add(new Rule(new IsSelectedFunction(childADec),
				new SetValueAction(root0Dec, root0Dec.getRangeValue(childB))));
		assertEquals(controlSet, ruleSet);
	}

	// TODO talk with kevin how to implement this
	@Test
	@Disabled("Check back with Kevin how to this case can be implemented.")
	void testConvertConstraintRecComplexConstraintCoupleRequires()
			throws NotSupportedVariabilityTypeException, CircleInConditionException, ConditionCreationException {
		// A and B => C
		Constraint implConstraint = new ImplicationConstraint(
				new AndConstraint(new LiteralConstraint(childA), new LiteralConstraint(childB)),
				new LiteralConstraint(childC));
		fm.getConstraints().add(implConstraint);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		TransformFMtoDMUtil.convertConstraintRec(factory, dm, fm, implConstraint);
		getDecisions(dm);

		Set<Rule> ruleSet = childADec.getRules();
		controlSet.add(new Rule(new And(new IsSelectedFunction(childADec), new IsSelectedFunction(childBDec)),
				new SelectDecisionAction(childCDec)));
		assertEquals(controlSet, ruleSet);
	}

	@Test
	void testConvertConstraintRecComplexConstraintMultiOr()
			throws NotSupportedVariabilityTypeException, CircleInConditionException, ConditionCreationException {
		// Or relation between all children also as constraint
		Constraint implConstraint = new OrConstraint(new LiteralConstraint(childA),
				new OrConstraint(new LiteralConstraint(childB), new LiteralConstraint(childC)));
		fm.getConstraints().add(implConstraint);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		TransformFMtoDMUtil.convertConstraintRec(factory, dm, fm, implConstraint);
		getDecisions(dm);
		IDecision orConstDec;
		assertNotNull(orConstDec = dm.get("or#constr#3"));
		// A disallows B

		Set<Rule> ruleSet = orConstDec.getRules();
		controlSet.add(new Rule(new DecisionValueCondition(orConstDec, orConstDec.getRangeValue(childA)),
				new SelectDecisionAction(childADec)));
		controlSet.add(new Rule(new DecisionValueCondition(orConstDec, orConstDec.getRangeValue(childB)),
				new SelectDecisionAction(childBDec)));
		controlSet.add(new Rule(new DecisionValueCondition(orConstDec, orConstDec.getRangeValue(childC)),
				new SelectDecisionAction(childCDec)));

		assertEquals(controlSet, ruleSet);
	}

	@Test
	void testConvertConstraintFaulty() throws NotSupportedVariabilityTypeException, ConditionCreationException {
		orGroup.GROUPTYPE = GroupType.OPTIONAL;
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		getDecisions(dm);
		fm.getFeatureMap().putAll(TraVarTUtils.getFeatureMapFromRoot(rootFeature));
		Constraint constraint = new ParenthesisConstraint(new ParenthesisConstraint(null));
		assertThrows(NullPointerException.class,
				() -> TransformFMtoDMUtil.convertConstraint(DecisionModelFactory.getInstance(), dm, fm, constraint));
//		controlSet.add(new Rule(new IsSelectedFunction((BooleanDecision)childBDec),new DeSelectDecisionAction((BooleanDecision)childADec)));
//		controlSet.add(new Rule(new IsSelectedFunction((BooleanDecision)childADec),new DeSelectDecisionAction((BooleanDecision)childBDec)));
//		Set<Rule> ruleSet = childBDec.getRules();
//		ruleSet.addAll(childADec.getRules());
//
//		assertEquals(controlSet, ruleSet);
	}

	@Test
	void testDeriveUnidirectionalRules()
			throws NotSupportedVariabilityTypeException, CircleInConditionException, ConditionCreationException {
		// Or relation between all children also as constraint
		Constraint implConstraint = new OrConstraint(new LiteralConstraint(childA),
				new OrConstraint(new LiteralConstraint(childB), new NotConstraint(new LiteralConstraint(childC))));
		fm.getConstraints().add(implConstraint);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		TransformFMtoDMUtil.convertConstraintRec(factory, dm, fm, implConstraint);
		getDecisions(dm);
		// A disallows B

		Set<Rule> ruleSet = childADec.getRules();
	}

	@Test
	void testDeriveRequiresRules()
			throws NotSupportedVariabilityTypeException, CircleInConditionException, ConditionCreationException {
		// Or relation between all children also as constraint
		Constraint implConstraint = new OrConstraint(new LiteralConstraint(childA),
				new OrConstraint(new LiteralConstraint(childB), new NotConstraint(new LiteralConstraint(childC))));
		fm.getConstraints().add(implConstraint);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		TransformFMtoDMUtil.convertConstraintRec(factory, dm, fm, implConstraint);
		getDecisions(dm);
		// A disallows B

		Set<Rule> ruleSet = childADec.getRules();
	}

	@Test
	void testHasOptionalParentTrue() throws NotSupportedVariabilityTypeException {
		Group optionalGroup = new Group(GroupType.OPTIONAL);
		String childD = "childD";
		Feature childDFeature = new Feature(childD);
		optionalGroup.getFeatures().add(childDFeature);
		childAFeature.addChildren(optionalGroup);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		fm.getFeatureMap().putAll(TraVarTUtils.getFeatureMapFromRoot(rootFeature));
		assertTrue(TransformFMtoDMUtil.hasOptionalParent(fm, childD));
	}

	@Test
	void testHasOptionalParentTrueWithDeeperTree() throws NotSupportedVariabilityTypeException {
		Group optionalGroup = new Group(GroupType.OPTIONAL);
		Group mandGroup = new Group(GroupType.MANDATORY);
		Group altGroup = new Group(GroupType.ALTERNATIVE);
		Group cardGroup = new Group(GroupType.GROUP_CARDINALITY);
		Group orGroup = new Group(GroupType.OR);
		String childD = "childD";
		Feature childDFeature = new Feature(childD);
		optionalGroup.getFeatures().add(childDFeature);
		childAFeature.addChildren(optionalGroup);
		String childE = "childE";
		Feature childEFeature = new Feature(childE);
		mandGroup.getFeatures().add(childEFeature);
		childDFeature.addChildren(mandGroup);
		String childF = "childF";
		Feature childFFeature = new Feature(childF);
		altGroup.getFeatures().add(childFFeature);
		childEFeature.addChildren(altGroup);
		String childG = "childG";
		Feature childGFeature = new Feature(childG);
		cardGroup.getFeatures().add(childGFeature);
		childFFeature.addChildren(cardGroup);
		String childH = "childH";
		Feature childHFeature = new Feature(childH);
		orGroup.getFeatures().add(childHFeature);
		childGFeature.addChildren(orGroup);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		getDecisions(dm);
		fm.getFeatureMap().putAll(TraVarTUtils.getFeatureMapFromRoot(rootFeature));
		assertTrue(TransformFMtoDMUtil.hasOptionalParent(fm, childH));
		assertTrue(TransformFMtoDMUtil.hasOptionalParent(fm, childF));
	}

	@Test
	void testHasOptionalParentFalse() throws NotSupportedVariabilityTypeException {
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		getDecisions(dm);
		fm.getFeatureMap().putAll(TraVarTUtils.getFeatureMapFromRoot(rootFeature));
		assertFalse(TransformFMtoDMUtil.hasOptionalParent(fm, childB));
	}

	@Test
	void testHasVirtualParentFeatureModelIDecision() throws NotSupportedVariabilityTypeException {
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		getDecisions(dm);
		fm.getFeatureMap().putAll(TraVarTUtils.getFeatureMapFromRoot(rootFeature));
		assertFalse(TransformFMtoDMUtil.hasVirtualParent(fm, childADec));
	}

	@Test
	void testDeriveExcludeRulesNormalDecisionExcludesEnumSubDecision() throws NotSupportedVariabilityTypeException {
		Group altGroup = new Group(GroupType.OR);
		String childD = "childD";
		Feature childDFeature = new Feature(childD);
		altGroup.getFeatures().add(childDFeature);
		orGroup.GROUPTYPE = GroupType.OPTIONAL;
		childAFeature.getChildren().add(altGroup);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		getDecisions(dm);
		fm.getFeatureMap().putAll(TraVarTUtils.getFeatureMapFromRoot(rootFeature));
		Constraint constraint = new ImplicationConstraint(new LiteralConstraint(childD),
				new NotConstraint(new LiteralConstraint(childB)));
		TransformFMtoDMUtil.deriveExcludeRules(dm, fm, constraint);
		controlSet.add(new Rule(new IsSelectedFunction(dm.get(childB)), new DisAllowAction(dm.get(childA + "#0"),
				((EnumDecision) dm.get(childA + "#0")).getRangeValue(childD))));
		controlSet.add(new Rule(new Not(new IsSelectedFunction(dm.get(childB))),
				new AllowAction(dm.get(childA + "#0"), ((EnumDecision) dm.get(childA + "#0")).getRangeValue(childD))));

		Set<Rule> ruleSet = childBDec.getRules();

		assertEquals(controlSet, ruleSet);
	}

	@Test
	void testDeriveExcludeRulesEnumSubDecisionExcludesNormalDecision() throws NotSupportedVariabilityTypeException {
		Group altGroup = new Group(GroupType.OR);
		String childD = "childD";
		Feature childDFeature = new Feature(childD);
		altGroup.getFeatures().add(childDFeature);
		orGroup.GROUPTYPE = GroupType.OPTIONAL;
		childAFeature.getChildren().add(altGroup);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		getDecisions(dm);
		fm.getFeatureMap().putAll(TraVarTUtils.getFeatureMapFromRoot(rootFeature));
		Constraint constraint = new ImplicationConstraint(new LiteralConstraint(childB),
				new NotConstraint(new LiteralConstraint(childD)));
		TransformFMtoDMUtil.deriveExcludeRules(dm, fm, constraint);
		controlSet.add(new Rule(new IsSelectedFunction(childBDec), new DisAllowAction(dm.get(childA + "#0"),
				((EnumDecision) dm.get(childA + "#0")).getRangeValue(childD))));
		controlSet.add(new Rule(new Not(new IsSelectedFunction(childBDec)),
				new AllowAction(dm.get(childA + "#0"), ((EnumDecision) dm.get(childA + "#0")).getRangeValue(childD))));
		controlSet.add(new Rule(new IsSelectedFunction(dm.get(childD)),
				new DeSelectDecisionAction((BooleanDecision) childBDec)));
		Set<Rule> ruleSet = childBDec.getRules();
		ruleSet.addAll(dm.get(childD).getRules());

		assertEquals(controlSet, ruleSet);
	}

	@Test
	void testDeriveExcludeRulesNormalDecisionExcludesNormalDecision() throws NotSupportedVariabilityTypeException {
		orGroup.GROUPTYPE = GroupType.OPTIONAL;
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		getDecisions(dm);
		fm.getFeatureMap().putAll(TraVarTUtils.getFeatureMapFromRoot(rootFeature));
		Constraint constraint = new ImplicationConstraint(new LiteralConstraint(childA),
				new NotConstraint(new LiteralConstraint(childB)));
		TransformFMtoDMUtil.deriveExcludeRules(dm, fm, constraint);
		controlSet.add(
				new Rule(new IsSelectedFunction(childBDec), new DeSelectDecisionAction((BooleanDecision) childADec)));
		controlSet.add(
				new Rule(new IsSelectedFunction(childADec), new DeSelectDecisionAction((BooleanDecision) childBDec)));
		Set<Rule> ruleSet = childBDec.getRules();
		ruleSet.addAll(childADec.getRules());

		assertEquals(controlSet, ruleSet);
	}

	@Test
	void testDeriveConditionFromConstraint() {
		fail("Not yet implemented");
	}

	@Test
	void testFindEnumDecisionWithVisiblityCondition() {
		fail("Not yet implemented");
	}

	@Test
	void testFindEnumDecisionByRangeValue() {
		fail("Not yet implemented");
	}

	@Test
	void testFindDecisionsForLiterals() {
		fail("Not yet implemented");
	}

	@Test
	void testUpdateRules() {
		fail("Not yet implemented");
	}

	@Test
	void testInvertRule() {
		fail("Not yet implemented");
	}

	@Test
	void testInvertCondition() {
		fail("Not yet implemented");
	}

	@Test
	void testIsEnumSubFeatureFeature() {
		fail("Not yet implemented");
	}

	@Test
	void testIsEnumSubFeatureFeatureModelIDecision() {
		fail("Not yet implemented");
	}

	@Test
	void testIsEnumFeature() {
		fail("Not yet implemented");
	}

	@Test
	void testDefineCardinality() {
		fail("Not yet implemented");
	}

	@Test
	void testDefineRange() {
		fail("Not yet implemented");
	}

	@Test
	void testConvertVisibilityCustomProperties() {
		fail("Not yet implemented");
	}

}
