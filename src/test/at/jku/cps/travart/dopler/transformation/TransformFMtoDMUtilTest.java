package at.jku.cps.travart.dopler.transformation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.core.transformation.DefaultModelTransformationProperties;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.CircleInConditionException;
import at.jku.cps.travart.dopler.decision.exc.ConditionCreationException;
import at.jku.cps.travart.dopler.decision.factory.impl.DecisionModelFactory;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.And;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.DeSelectDecisionAction;
import at.jku.cps.travart.dopler.decision.model.impl.DecisionValueCondition;
import at.jku.cps.travart.dopler.decision.model.impl.EnumDecision;
import at.jku.cps.travart.dopler.decision.model.impl.IsSelectedFunction;
import at.jku.cps.travart.dopler.decision.model.impl.Not;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;
import at.jku.cps.travart.dopler.decision.model.impl.SelectDecisionAction;
import at.jku.cps.travart.dopler.decision.model.impl.SetValueAction;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import de.vill.model.Group.GroupType;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ImplicationConstraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;

class TransformFMtoDMUtilTest {
	DecisionModelFactory factory;
	IDecisionModel dm;
	FeatureModel fm;

	@BeforeEach
	void setUp() throws Exception {
		factory = new DecisionModelFactory();
		dm = factory.create();
		fm = new FeatureModel();
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
	void testConvertConstraintRecSingleExcludes() throws NotSupportedVariabilityTypeException, CircleInConditionException, ConditionCreationException {
		String root = "root";
		String childA = "childA";
		String childB = "childB";
		Feature rootFeature= new Feature(root);
		Feature childAFeature= new Feature(childA);
		Feature childBFeature= new Feature(childB);
		Group orGroup= new Group(GroupType.OR);
		orGroup.getFeatures().add(childAFeature);
		orGroup.getFeatures().add(childBFeature);
		rootFeature.getChildren().add(orGroup);
		fm.setRootFeature(rootFeature);
		Constraint implConstraint=new ImplicationConstraint(new LiteralConstraint(childA),new NotConstraint(new LiteralConstraint(childB)));
		fm.getConstraints().add(implConstraint);
		TransformFMtoDMUtil.convertFeature(factory, dm, rootFeature);
		TransformFMtoDMUtil.convertConstraintRec(factory, dm, fm, implConstraint);
		assertFalse(dm.get(childA).getRules().isEmpty());
		assertEquals(new Rule(new Not(new IsSelectedFunction(dm.get(childA))),new DeSelectDecisionAction((BooleanDecision)dm.get(childB))),dm.get(childA).getRules().iterator().next());
	}

	@Test
	void testDeriveUnidirectionalRules() {
		fail("Not yet implemented");
	}

	@Test
	void testConvertConstraint() {
		fail("Not yet implemented");
	}

	@Test
	void testDeriveRequiresRules() {
		fail("Not yet implemented");
	}

	@Test
	void testHasOptionalParent() {
		fail("Not yet implemented");
	}

	@Test
	void testHasVirtualParentFeatureModelIDecision() {
		fail("Not yet implemented");
	}

	@Test
	void testDeriveExcludeRules() {
		fail("Not yet implemented");
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
