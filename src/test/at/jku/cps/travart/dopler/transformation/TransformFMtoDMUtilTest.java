package at.jku.cps.travart.dopler.transformation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.factory.impl.DecisionModelFactory;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.And;
import at.jku.cps.travart.dopler.decision.model.impl.IsSelectedFunction;
import at.jku.cps.travart.dopler.transformation.TransformFMtoDMUtil;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import de.vill.model.Group.GroupType;

class TransformFMtoDMUtilTest {
	DecisionModelFactory factory;
	IDecisionModel dm;
	FeatureModel fm;

	@BeforeEach
	void setUp() throws Exception {
		factory = new DecisionModelFactory();
		dm=factory.create();
		fm=new FeatureModel();
	}
	
	@Test
	void testConvertFeatureSingleMandatoryGroup() throws NotSupportedVariabilityTypeException {
		String root="root";
		String child="child";
		Feature rootFeature= new Feature(root);
		Group manGroup=new Group(GroupType.MANDATORY);
		rootFeature.getChildren().add(manGroup);
		manGroup.getFeatures().add(new Feature(child));
		TransformFMtoDMUtil.convertFeature(factory,dm,rootFeature);
		IDecision dmRoot=dm.get(root);
		IDecision dmChild=dm.get(child);
		assertNotNull(dmRoot);
		assertEquals(ICondition.TRUE,dmRoot.getVisiblity());
		assertNotNull(dmChild);
		assertEquals(new And(ICondition.FALSE,new IsSelectedFunction(dmRoot)),dmChild.getVisiblity());
	}
	
	@Test
	void testConvertFeatureSingleOptionalGroup() throws NotSupportedVariabilityTypeException {
		String root="root";
		String child="child";
		Feature rootFeature= new Feature(root);
		Group manGroup=new Group(GroupType.OPTIONAL);
		rootFeature.getChildren().add(manGroup);
		manGroup.getFeatures().add(new Feature(child));
		TransformFMtoDMUtil.convertFeature(factory,dm,rootFeature);
		assertNotNull(dm.get(root));
		assertNotNull(dm.get(child));
	}

	@Test
	void testHasVirtualParentFeature() {
		fail("Not yet implemented");
	}

	@Test
	void testCreateEnumDecisions() {
		fail("Not yet implemented");
	}

	@Test
	void testConvertConstraints() {
		fail("Not yet implemented");
	}

	@Test
	void testConvertConstraintRec() {
		fail("Not yet implemented");
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
