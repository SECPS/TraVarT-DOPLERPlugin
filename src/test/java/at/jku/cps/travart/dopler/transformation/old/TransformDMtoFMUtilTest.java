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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.CircleInConditionException;
import at.jku.cps.travart.dopler.decision.factory.impl.DecisionModelFactory;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Cardinality;
import at.jku.cps.travart.dopler.decision.model.impl.DoubleValue;
import at.jku.cps.travart.dopler.decision.model.impl.EnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.NumberDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.StringDecision;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;
import de.vill.model.FeatureModel;

class TransformDMtoFMUtilTest {
	DecisionModelFactory factory;
	IDecisionModel dm;
	BooleanDecision cream;
	EnumerationDecision holder;
	NumberDecision portions;
	EnumerationDecision flavors;
	StringDecision name;
	EnumerationDecision toppings;
	Range<String> holderRange;
	Range<Double> portionRange;
	Range<String> flavorRange;
	Range<String> topRange;
	
	FeatureModel fm;

	@BeforeEach
	void setUp() throws Exception {
		dm=factory.getInstance().create();
		cream = new BooleanDecision("whipped_cream");
		holder= new EnumerationDecision("holder");
		holderRange= new Range<>();
		holderRange.add(new StringValue("edible_cup"));
		holderRange.add(new StringValue("plastic_cup"));
		holderRange.add(new StringValue("paper_cup"));
		holder.setRange(holderRange);
		holder.setCardinality(new Cardinality(1,1));
		portions= new NumberDecision("portions");
		portionRange= new Range<>();
		portionRange.add(new DoubleValue(1));
		portionRange.add(new DoubleValue(2));
		portionRange.add(new DoubleValue(3));
		portionRange.add(new DoubleValue(4));
		portions.setRange(portionRange);
		flavors= new EnumerationDecision("flavors");
		flavors.setCardinality(new Cardinality(1,4));
		flavorRange=new Range<>();
		flavorRange.add(new StringValue("vanilla"));
		flavorRange.add(new StringValue("strawberry"));
		flavorRange.add(new StringValue("chocolate"));
		flavorRange.add(new StringValue("hazelnut"));
		flavorRange.add(new StringValue("banana"));
		flavorRange.add(new StringValue("bubblegum"));
		flavors.setRange(flavorRange);
		toppings= new EnumerationDecision("toppings");
		topRange= new Range<>();
		topRange.add(new StringValue("rainbow_sprinkles"));
		topRange.add(new StringValue("cocoa"));
		topRange.add(new StringValue("smarties"));
		topRange.add(new StringValue("gold_flakes"));
//		topRange.add(toppings.getNoneOption());
		toppings.setRange(topRange);
		toppings.setCardinality(new Cardinality(0,4));
		name=new StringDecision("name");	
		dm.add(cream);
		dm.add(holder);
		dm.add(portions);
		dm.add(flavors);
		dm.add(toppings);
		dm.add(name);
		fm= new FeatureModel();
	}

	@Test
	void testCreateFeaturesAreAllFeatureTypesPresent() throws CircleInConditionException {
		TransformDMtoFMUtil.createFeatures(fm,dm);
		Set<String> featureNames= new HashSet<>();
		featureNames.addAll(Arrays.asList(cream.getName(),holder.getName(),flavors.getName(),toppings.getName(),
				name.getName()));
		featureNames.addAll(holderRange.stream().map(e->e.getValue()).collect(Collectors.toList()));
		featureNames.addAll(flavorRange.stream().map(e->e.getValue()).collect(Collectors.toList()));
		featureNames.addAll(topRange.stream().map(e->e.getValue()).collect(Collectors.toList()));
		assertTrue(fm.getFeatureMap().keySet().containsAll(featureNames));
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
	void testCreateFeatureTree() throws CircleInConditionException {
		TransformDMtoFMUtil.createFeatures(fm,dm);
		TransformDMtoFMUtil.createFeatureTree(fm, dm);
		System.out.println(fm.toString());
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
