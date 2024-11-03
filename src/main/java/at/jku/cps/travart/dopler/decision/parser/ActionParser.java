/*******************************************************************************
 * TODO: explanation what the class does
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision.parser;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.InvalidActionException;
import at.jku.cps.travart.dopler.decision.exc.ParserException;
import at.jku.cps.travart.dopler.decision.model.AbstractRangeValue;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class ActionParser {

    private static final int NECESSARY_ELEMENTS_FOR_ACTION = 2;

    private static final String REGEX = "(?<=\\.)|(?=\\.)|(?<=\\=)|(?=\\=)|((?<=\\()|(?=\\())|((?<=\\))|(?=\\)))";

    private static final String EOF = "EOF";

    private static final String ASSIGN = "=";
    private static final String DECISION_VALUE_DELIMITER = ".";
    private static final String OPEN_PARENTHESE = "(";
    private static final String CLOSING_PARENTHESE = ")";

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private String[] input;
    private int index = 0;
    private String symbol;

    private final IDecisionModel dm;
    private final Queue<Object> actionElements = new LinkedList<>();

    public ActionParser(final IDecisionModel decisions) {
        dm = Objects.requireNonNull(decisions);
    }

    public IAction parse(final String str) throws ParserException {
        Objects.requireNonNull(str);
        index = 0;
        input = Arrays.stream(str.split(REGEX)).map(String::trim).filter(s -> !s.isEmpty() && !s.isBlank())
                .toArray(String[]::new);
        if (input.length > 0) {
            return parseAction();
        }
        throw new ParserException("No action found in String " + str);
    }

    @SuppressWarnings("rawtypes")
    private IAction parseAction() {
        IAction action = null;
        boolean isAssign = false;
        boolean isAllowFunction = false;
        boolean isDisAllowFunction = false;
        nextSymbol();
        while (!symbol.equals(EOF)) {
            if (symbol.equals(OPEN_PARENTHESE) || symbol.equals(CLOSING_PARENTHESE) ||
                    symbol.equals(DECISION_VALUE_DELIMITER)) {
                nextSymbol();
                continue;
            }
            if (symbol.equals(TRUE)) {
                actionElements.add(ICondition.TRUE);
            } else if (symbol.equals(FALSE)) {
                actionElements.add(ICondition.FALSE);
            } else if (symbol.equals(ASSIGN)) {
                isAssign = true;
                Object left = actionElements.remove();
                if (left instanceof StringValue) {
                    IDecision d = dm.get(((StringValue) left).getValue());
                    if (Objects.isNull(d)) {
                        actionElements.add(left);
                    } else {
                        actionElements.add(d);
                    }
                } else {
                    actionElements.add(left);
                }
            } else if (symbol.equals(AllowAction.FUNCTION_NAME)) {
                isAllowFunction = true;
            } else if (symbol.equals(DisAllowAction.FUNCTION_NAME)) {
                isDisAllowFunction = true;
            } else if (symbol.equals("enforce")) {
                isAssign = true;
            } else if (RulesParser.isDoubleRangeValue(symbol)) {
                actionElements.add(new DoubleValue(Double.parseDouble(symbol)));
            } else if (RulesParser.isStringRangeValue(dm, symbol)) {
                actionElements.add(new StringValue(symbol));
            } else { // decision
                IDecision d = dm.get(symbol);
                actionElements.add(d);
            }
            if (actionElements.size() == NECESSARY_ELEMENTS_FOR_ACTION) {
                if (isAssign) {
                    Object left = actionElements.remove();
                    Object right = actionElements.remove();
                    IDecision aLeftDecision = null;
                    if (left instanceof IDecision) {
                        aLeftDecision = (IDecision) left;
                    } else {
                        aLeftDecision = dm.get(left.toString());
                    }

                    if (Objects.isNull(aLeftDecision)) {
                        throw new InvalidActionException("Lefthand operand is not a valid decision.");
                    }
                    if (right == ICondition.TRUE) {
                        action = new SelectDecisionAction((BooleanDecision) left);
                    } else if (right == ICondition.FALSE) {
                        action = new DeSelectDecisionAction((BooleanDecision) left);
                    } else if (right instanceof AbstractRangeValue) {
                        action = new SetValueAction((IDecision) left, (AbstractRangeValue) right);
                    }
                } else if (isAllowFunction) {
                    Object left = actionElements.remove();
                    Object right = actionElements.remove();
                    if (left instanceof IDecision && right instanceof AbstractRangeValue) {
                        action = new AllowAction((IDecision) left, (AbstractRangeValue) right);
                    }
                } else if (isDisAllowFunction) {
                    Object left = actionElements.remove();
                    Object right = actionElements.remove();
                    if (left instanceof IDecision && right instanceof AbstractRangeValue) {
                        action = new DisAllowAction((IDecision) left, (AbstractRangeValue) right);
                    }
                }
            }
            nextSymbol();
        }
        return action;
    }

    private void nextSymbol() {
        if (index == input.length) {
            symbol = EOF;
        } else {
            symbol = input[index++];
        }
    }
}