/*******************************************************************************
 * TODO: explanation what the class does
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision.model;

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision.DecisionType;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ADecisionTest {

    /**
     * creates an anonymous Decision class with no id. This should not be allowed
     */
    @Test
    public void noNullIdTest() {
        assertThrows(NullPointerException.class,
                () -> createNoTypeDecision(null, new Cardinality(1, 1), DecisionType.BOOLEAN));
    }

    @Test
    public void noNullTypeTest() {
        assertThrows(NullPointerException.class, () -> createNoTypeDecision("someid", new Cardinality(1, 1), null));
    }

    @Test
    public void enumToStringTest() {
        assertEquals("Boolean", DecisionType.BOOLEAN.toString());
        assertEquals("Double", DecisionType.NUMBER.toString());
        assertEquals("String", DecisionType.STRING.toString());
        assertEquals("Enumeration", DecisionType.ENUM.toString());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void isSelectedTest() {
        AbstractDecision d = new StringDecision("test");
        d.setSelected(true);
        assertTrue(d.isSelected());
        d.setSelected(false);
        assertFalse(d.isSelected());
    }

    @Test
    public void containsRangeValueTest() {
        EnumerationDecision e = new EnumerationDecision("test");
        Range<String> eRange = new Range<>();
        AbstractRangeValue<String> f = new StringValue("First");
        eRange.add(f);
        e.setRange(eRange);
        assertTrue(e.getRange().contains(f));
        assertTrue(e.getRange().contains(new StringValue("First")));
        assertFalse(e.getRange().contains(new StringValue("NotContained")));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void getTypeTest() {
        AbstractDecision d = new StringDecision("test");
        assertEquals(DecisionType.STRING, d.getType());
        d = new EnumerationDecision("test");
        assertEquals(DecisionType.ENUM, d.getType());
        d = new NumberDecision("test");
        assertEquals(DecisionType.NUMBER, d.getType());
        d = new BooleanDecision("test");
        assertEquals(DecisionType.BOOLEAN, d.getType());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void getVisibilityTest() {
        AbstractDecision d = new StringDecision("test");
        d.setVisibility(ICondition.TRUE);
        assertEquals(ICondition.TRUE, d.getVisiblity());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void isVisibleTest() {
        AbstractDecision d = new StringDecision("test");
        d.setVisibility(ICondition.TRUE);
        assertTrue(d.isVisible());
        d.setVisibility(ICondition.FALSE);
        assertFalse(d.isVisible());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void toStringTest() {
        AbstractDecision d = new StringDecision("test");
        assertEquals("test", d.toString());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void equalsTest() {
        AbstractDecision d1 = new StringDecision("test");
        AbstractDecision d2 = new StringDecision("test");

        assertTrue(d1.equals(d1));
        assertTrue(d1.equals(d2));
        assertFalse(d1.equals(new EnumerationDecision("test")));
        assertFalse(d1.equals(null));
        assertFalse(d1.equals(new StringDecision("test2")));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void setRuleTest() {
        AbstractDecision d = new EnumerationDecision("test");
        AbstractDecision b = new BooleanDecision("test");
        Set<Rule> rules = new HashSet<>();
        Rule r1 = new Rule(new IsSelectedFunction(new BooleanDecision("test")),
                new AllowAction(b, BooleanValue.getTrue()));
        rules.add(r1);
        d.setRules(rules);
        assertEquals(rules, d.getRules());
    }

    @SuppressWarnings({"rawtypes"})
    @Test
    public void addRuleTest() {
        AbstractDecision d = new EnumerationDecision("test");
        AbstractDecision b = new BooleanDecision("test");
        Rule r1 = new Rule(new IsSelectedFunction(b), new AllowAction(b, BooleanValue.getTrue()));
        d.addRule(r1);
        assertTrue(d.getRules().contains(r1));
        Rule r2 = new Rule(new IsSelectedFunction(b), new SetValueAction(b, BooleanValue.getTrue()));
        d.addRule(r2);
        assertTrue(d.getRules().contains(r2));
    }

    @SuppressWarnings({"rawtypes"})
    @Test
    public void removeRuleTest() {
        AbstractDecision d = new EnumerationDecision("test");
        AbstractDecision b = new BooleanDecision("test");
        Rule r1 = new Rule(new IsSelectedFunction(b), new AllowAction(b, BooleanValue.getTrue()));
        d.addRule(r1);
        assertTrue(d.getRules().contains(r1));
        d.removeRule(r1);
        assertFalse(d.getRules().contains(r1));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void executeRulesTest() throws RangeValueException {
        AbstractDecision d = new EnumerationDecision("test");
        AbstractDecision req = new BooleanDecision("isSet");
        AbstractDecision reqfalse = new BooleanDecision("isSet");
        AbstractDecision b = new BooleanDecision("test");

        reqfalse.setValue(false);
        req.setValue(true);
        b.setValue(false);
        Rule r1 = new Rule(new IsSelectedFunction(req), new AllowAction(b, BooleanValue.getTrue()));
        d.addRule(r1);
        assertTrue(d.getRules().contains(r1));
        Rule r2 = new Rule(new IsSelectedFunction(req), new SetValueAction(b, BooleanValue.getTrue()));
        d.addRule(r2);
        Rule r3 = new Rule(new IsSelectedFunction(reqfalse), new SetValueAction(b, BooleanValue.getTrue()));
        d.addRule(r3);

        //		try {
        //			d.executeRules();
        //		} catch (ActionExecutionException e) {
        //			// TODO Auto-generated catch block
        //			e.printStackTrace();
        //		}
        assertTrue(b.getValue().equals(BooleanValue.getTrue()));
    }

    private AbstractDecision<Object> createNoTypeDecision(final String id, final Cardinality c, final DecisionType t) {
        return new AbstractDecision<>(id, t) {
            @Override
            public Range<Object> getRange() {
                return null;
            }

            @Override
            public AbstractRangeValue<Object> getRangeValue(final Object value) {
                return null;
            }

            @Override
            public AbstractRangeValue<Object> getRangeValue(final String value) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void setRange(final Range<Object> range) {
            }

            @Override
            public void reset() throws RangeValueException {
            }

            @Override
            public AbstractRangeValue<Object> getValue() {
                return null;
            }

            @Override
            public void setValue(final Object value) throws RangeValueException {

            }
        };
    }
}
