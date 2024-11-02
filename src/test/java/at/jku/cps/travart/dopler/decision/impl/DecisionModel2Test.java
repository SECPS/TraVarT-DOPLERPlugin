/*******************************************************************************
 * TODO: explanation what the class does
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision.impl;

import at.jku.cps.travart.core.common.IConfigurable;
import at.jku.cps.travart.dopler.decision.exc.ActionExecutionException;
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.factory.impl.DecisionModelFactory;
import at.jku.cps.travart.dopler.decision.model.AbstractRangeValue;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DecisionModel2Test {

    DecisionModel dm;
    BooleanDecision bd;
    NumberDecision nd;
    EnumerationDecision ed;
    StringDecision sd;
    private static final String factoryId = "myFactoryId";
    private static final String modelName = "DecisionModel";

    @BeforeEach
    public void setUp() throws Exception {
        dm = DecisionModelFactory.getInstance().create();
        bd = new BooleanDecision("testBool");
        nd = new NumberDecision("testNumber");
        ed = new EnumerationDecision("testEnum");
        sd = new StringDecision("testString");
        dm.add(bd);
        dm.add(nd);
        dm.add(ed);
        dm.add(sd);
    }

    @Test
    public void testDecisionModelString() {
        dm = new DecisionModel(factoryId);
        assertEquals(factoryId, dm.getFactoryId());
        assertEquals(modelName, dm.getName());
    }

    @Test
    public void testDecisionModelStringNull() {
        assertThrows(NullPointerException.class, () -> {
            dm = new DecisionModel(null);
        });
    }

    @Test
    public void testDecisionModelStringStringBoolean() {
        dm = new DecisionModel(factoryId, modelName);
        assertEquals(factoryId, dm.getFactoryId());
        assertEquals(modelName, dm.getName());
    }

    @Test
    public void testDecisionModelStringNullStringBoolean() {
        assertThrows(NullPointerException.class, () -> {
            dm = new DecisionModel(null, modelName);
        });
    }

    @Test
    public void testDecisionModelStringStringNullBoolean() {
        assertThrows(NullPointerException.class, () -> {
            dm = new DecisionModel(factoryId, null);
        });
    }

    @Test
    public void testAfterValueSelection() throws RangeValueException {
        StringValue s1 = new StringValue("svalue1");
        StringValue s2 = new StringValue("svalue2");

        ed.getRange().add(s1);
        ed.getRange().add(s2);

        EnumerationDecision ed2 = new EnumerationDecision("svalue1");
        StringValue sv1 = new StringValue("TheChoseValue1");
        ed2.getRange().add(sv1);
        ed2.setValue(sv1.getValue());
        dm.add(ed2);
        assertEquals(sv1, ed2.getValue());

        EnumerationDecision ed3 = new EnumerationDecision("svalue2");
        StringValue sv2 = new StringValue("TheChoseValue2");
        ed3.getRange().add(sv2);
        ed3.setValue(sv2.getValue());
        dm.add(ed3);
        assertEquals(sv2, ed3.getValue());

        assertEquals(new StringValue(" "), ed.getValue());
        assertNotEquals(s1, ed.getValue());
        assertNotEquals(s2, ed.getValue());
    }

    @Test
    public void testExecuteRules() throws ActionExecutionException {
        StringValue sv = new StringValue("aValue");
        ed.getRange().add(sv);
        bd.addRule(new Rule(ICondition.TRUE, new SetValueAction(ed, sv)));
        bd.setSelected(true);
        assertEquals(sv, ed.getValue());
    }

    @Test
    public void testFindPrefix() {
        assertEquals(bd, dm.get(bd.getId()));
    }

    @Test
    public void testFindNoPrefix() {
        assertEquals(bd, dm.get(bd.getId()));
    }

    @Test
    public void testFindWithRangeValue() {
        StringValue sv = new StringValue("value");
        ed.getRange().add(sv);
        EnumerationDecision ed2 = new EnumerationDecision("anotherEnum");
        dm.add(ed2);
        ed.getRange().add(new StringValue("anotherEnum"));
        assertTrue(dm.findWithRangeValue(ed2).contains(ed));
    }

    @Test
    public void testFindWithVisibility() {
        ed.setVisibility(new IsSelectedFunction(bd));
        assertTrue(dm.findWithVisibility(bd).contains(ed));
    }

    @Test
    public void testFindWithVisibilityRequiredVisible1() throws RangeValueException {
        BooleanDecision bd1 = new BooleanDecision("bd1");
        bd1.setValue(false);
        And and = new And(new IsSelectedFunction(bd1), ICondition.FALSE);
        ed.setVisibility(and);
        assertTrue(dm.findWithVisibility(bd1).contains(ed));
    }

    @Test
    public void testFindWithVisibilityRequiredVisible2() throws RangeValueException {
        BooleanDecision bd1 = new BooleanDecision("bd1");
        bd1.setValue(false);
        And and = new And(ICondition.FALSE, new IsSelectedFunction(bd1));
        ed.setVisibility(and);
        assertTrue(dm.findWithVisibility(bd1).contains(ed));
    }

    @Test
    public void testFindWithVisibilityRequiredVisible3() throws RangeValueException {
        BooleanDecision bd1 = new BooleanDecision("bd1");
        bd1.setValue(false);
        And and = new And(ICondition.FALSE, new IsSelectedFunction(bd1));
        ed.setVisibility(and);
        assertTrue(dm.findWithVisibility(sd).isEmpty());
    }

    @Test
    public void testGetFactoryId() {
        assertEquals("at.jku.cps.travart.dopler.decision.factory.DecisionModelFactory", dm.getFactoryId());
    }

    @Test
    public void testGetName() {
        assertEquals(modelName, dm.getName());
    }

    @Test
    public void testGetSourceFile() {
        assertNull(dm.getSourceFile());
    }

    @Test
    public void testIsValidNoSelectedDecisions() {
        assertFalse(dm.isValid());
    }

    @Test
    public void testIsValid1() {
        bd.setSelected(true);
        ed.setSelected(true);
        nd.setSelected(true);
        sd.setSelected(true);
        assertFalse(dm.isValid(), "EnumDecision has no values in Range yet, so invalid.");
        ed.getRange().add(ed.getNoneOption());
        assertTrue(dm.isValid(), "EnumDecision now has a value in its Range so it's valid.");
    }

    @Test
    public void testIsValidSelectedDecisionNotVisible() throws RangeValueException {
        bd.setSelected(true);
        ed.setSelected(true);
        nd.setSelected(true);
        sd.setSelected(true);
        ed.getRange().add(ed.getNoneOption());
        BooleanDecision bd1 = new BooleanDecision("bd1");
        bd1.setValue(false);
        And and = new And(ICondition.FALSE, ICondition.FALSE);
        ed.setVisibility(and);
        assertFalse(dm.isValid(), "EnumDecision is selected, but not visible and not mandatory, so should be invalid");
    }

    @Test
    public void testIsValidUnselectedVisibilityDecision() throws RangeValueException {
        bd.setSelected(true);
        ed.setSelected(true);
        nd.setSelected(true);
        sd.setSelected(true);
        ed.getRange().add(ed.getNoneOption());
        BooleanDecision bd1 = new BooleanDecision("bd1");
        bd1.setValue(false);
        bd1.setSelected(false);
        And and = new And(new IsSelectedFunction(bd1), ICondition.FALSE);
        ed.setVisibility(and);
        assertFalse(dm.isValid(), "A decision required for a visibility is not selected, therefore false");
    }

    @Test
    public void testIsValidUnperformedDisallowActionEnumDecision() throws RangeValueException {
        bd.setSelected(true);
        ed.getRange().add(ed.getNoneOption());
        bd.addRule(new Rule(ICondition.TRUE, new DisAllowAction(ed, ed.getNoneOption())));

        assertFalse(dm.isValid(), "A Rule's condition is met, but its Action is not satisfied.");
    }

    @Test
    public void testIsValidDisallowActionEnumDecision() throws RangeValueException {
        bd.setSelected(true);
        AbstractRangeValue<String> ev = ed.getNoneOption();
        StringValue sv = new StringValue("aValue");
        ed.getRange().add(ev);
        ed.getRange().add(sv);
        ed.setValue(sv.getValue());
        bd.addRule(new Rule(ICondition.TRUE, new DisAllowAction(ed, ed.getNoneOption())));

        assertTrue(dm.isValid(), "A Rule's condition is met, and its action is satisfied.");
    }

    @Test
    public void testIsValidUnperformedDisallowActionNumberDecision() throws RangeValueException {
        nd.setSelected(true);
        bd.setSelected(true);
        DoubleValue dv = new DoubleValue(3);
        nd.getRange().add(dv);
        bd.addRule(new Rule(ICondition.TRUE, new DisAllowAction(nd, dv)));

        assertFalse(dm.isValid(), "A Rule's condition is met, but its Action is not satisfied.");
    }

    @Test
    public void testIsValidDisallowActionNumberDecision() throws RangeValueException {
        nd.setSelected(true);
        bd.setSelected(true);
        DoubleValue dv = new DoubleValue(3);
        nd.getRange().add(dv);
        nd.setValue(dv.getValue());
        bd.addRule(new Rule(ICondition.TRUE, new DisAllowAction(nd, dv)));

        assertTrue(dm.isValid(), "A Rule's condition is met, but its Action is satisfied.");
    }

    @Test
    public void testIsValidDeSelectDecisionEnumDecision() throws RangeValueException {
        ed.setSelected(true);
        BooleanDecision bd2 = new BooleanDecision("bd2");
        bd2.setSelected(true);
        bd.setSelected(true);
        StringValue dv = new StringValue("val");
        ed.getRange().add(dv);
        ed.setValue(dv.getValue());
        bd.addRule(new Rule(ICondition.TRUE, new DeSelectDecisionAction(bd2)));

        assertFalse(dm.isValid(), "A Rule's condition is met, but its Action is not satisfied.");
    }

    @Test
    public void testSetName() {
        String newName = "newName";
        dm.setName(newName);
        assertEquals(newName, dm.getName());
    }

    @Test
    public void testSetSourceFile() {
        String file = "Just any string will do.";
        dm.setSourceFile(file);
        assertEquals(file, dm.getSourceFile());
    }

    @Test
    public void testGetCurrentConfigurationCorrect() {
        Map<IConfigurable, Boolean> controlMap = new HashMap<>();
        bd.setSelected(true);
        nd.setSelected(true);
        controlMap.put(bd, true);
        controlMap.put(nd, true);
        controlMap.put(ed, false);
        controlMap.put(sd, false);
        assertEquals(controlMap, dm.getCurrentConfiguration(),
                controlMap.toString() + " should equal " + dm.getCurrentConfiguration().toString());
        bd.setSelected(false);
        assertNotEquals(controlMap, dm.getCurrentConfiguration(),
                controlMap.toString() + " should not equal " + dm.getCurrentConfiguration().toString());
    }

    @Test
    public void testGetCurrentConfigurationIncorrect() {
        Map<IConfigurable, Boolean> controlMap = new HashMap<>();
        bd.setSelected(true);
        nd.setSelected(true);
        controlMap.put(bd, true);
        controlMap.put(nd, true);
        controlMap.put(ed, false);
        controlMap.put(sd, false);
        bd.setSelected(false);
        assertNotEquals(controlMap, dm.getCurrentConfiguration(),
                controlMap.toString() + " should not equal " + dm.getCurrentConfiguration().toString());
    }
}
