package at.jku.cps.travart.dopler.transformation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.CircleInConditionException;
import at.jku.cps.travart.dopler.decision.factory.impl.DecisionModelFactory;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Cardinality;
import at.jku.cps.travart.dopler.decision.model.impl.DoubleValue;
import at.jku.cps.travart.dopler.decision.model.impl.EnumDecision;
import at.jku.cps.travart.dopler.decision.model.impl.NumberDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.StringDecision;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;
import at.jku.cps.travart.dopler.transformation.roundtrip.TransformDMtoFMUtil;
import de.vill.model.FeatureModel;

class TransformDMtoFMUtilTest {
	DecisionModelFactory factory;
	IDecisionModel dm;
	BooleanDecision cream;
	EnumDecision holder;
	NumberDecision portions;
	EnumDecision flavors;
	StringDecision name;
	EnumDecision toppings;
	
	FeatureModel fm;

	@BeforeEach
	void setUp() throws Exception {
		dm=factory.getInstance().create();
		cream = new BooleanDecision("whipped cream?");
		holder= new EnumDecision("What kind of holder?");
		Range<String> holderRange= new Range<>();
		holderRange.add(new StringValue("Edible cup"));
		holderRange.add(new StringValue("plastic cup"));
		holderRange.add(new StringValue("paper cup"));
		holder.setRange(holderRange);
		holder.setCardinality(new Cardinality(1,1));
		portions= new NumberDecision("how many portions?");
		Range<Double> portionRange= new Range<>();
		portionRange.add(new DoubleValue(1));
		portionRange.add(new DoubleValue(2));
		portionRange.add(new DoubleValue(3));
		portionRange.add(new DoubleValue(4));
		portions.setRange(portionRange);
		flavors= new EnumDecision("Flavors?");
		flavors.setCardinality(new Cardinality(1,4));
		Range<String> flavorRange=new Range<>();
		flavorRange.add(new StringValue("vanilla"));
		flavorRange.add(new StringValue("strawberry"));
		flavorRange.add(new StringValue("chocolate"));
		flavorRange.add(new StringValue("hazelnut"));
		flavorRange.add(new StringValue("banana"));
		flavorRange.add(new StringValue("bubblegum"));
		flavors.setRange(flavorRange);
		toppings= new EnumDecision("Toppings?");
		Range<String> topRange= new Range<>();
		topRange.add(new StringValue("rainbow sprinkles"));
		topRange.add(new StringValue("cocoa"));
		topRange.add(new StringValue("Smarties"));
		topRange.add(new StringValue("gold flakes"));
		topRange.add(toppings.getNoneOption());
		toppings.setRange(topRange);
		toppings.setCardinality(new Cardinality(0,4));
		name=new StringDecision("your name?");	
		dm.add(cream);
		dm.add(holder);
		dm.add(portions);
		dm.add(flavors);
		dm.add(name);
		dm.add(toppings);
		fm= new FeatureModel();		
	}

	@Test
	void testCreateFeatures() throws CircleInConditionException {
		TransformDMtoFMUtil.createFeatures(fm,dm);
	}

	@Test
	void testAddConstraintIfEligible() {
		fail("Not yet implemented");
	}

	@Test
	void testIsInItSelfConstraint() {
		fail("Not yet implemented");
	}

	@Test
	void testCreateFeatureTree() {
		fail("Not yet implemented");
	}

	@Test
	void testConsumeToBinaryCondition() {
		fail("Not yet implemented");
	}

	@Test
	void testRetriveFeatureName() {
		fail("Not yet implemented");
	}

	@Test
	void testCreateConstraints() {
		fail("Not yet implemented");
	}

	@Test
	void testDeriveCompareConstraints() {
		fail("Not yet implemented");
	}

	@Test
	void testCreateExcludesConstraint() {
		fail("Not yet implemented");
	}

	@Test
	void testDeriveConstraintFeatureModelIDecisionModelIDecisionIConditionIAction() {
		fail("Not yet implemented");
	}

	@Test
	void testHasNegativeLiteral() {
		fail("Not yet implemented");
	}

	@Test
	void testConsumeToGroup() {
		fail("Not yet implemented");
	}

	@Test
	void testDeriveConditionConstraint() {
		fail("Not yet implemented");
	}

	@Test
	void testDeriveConstraintRecursive() {
		fail("Not yet implemented");
	}

	@Test
	void testDeriveConstraintFeatureModelICondition() {
		fail("Not yet implemented");
	}

}
