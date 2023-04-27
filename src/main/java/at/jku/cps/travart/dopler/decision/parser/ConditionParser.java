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

import java.util.Arrays;
import java.util.Objects;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.ParserException;
import at.jku.cps.travart.dopler.decision.model.AFunction;
import at.jku.cps.travart.dopler.decision.model.ARangeValue;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.And;
import at.jku.cps.travart.dopler.decision.model.impl.DecisionValueCondition;
import at.jku.cps.travart.dopler.decision.model.impl.DoubleValue;
import at.jku.cps.travart.dopler.decision.model.impl.Equals;
import at.jku.cps.travart.dopler.decision.model.impl.GetValueFunction;
import at.jku.cps.travart.dopler.decision.model.impl.Greater;
import at.jku.cps.travart.dopler.decision.model.impl.GreaterEquals;
import at.jku.cps.travart.dopler.decision.model.impl.IsSelectedFunction;
import at.jku.cps.travart.dopler.decision.model.impl.IsTakenFunction;
import at.jku.cps.travart.dopler.decision.model.impl.Less;
import at.jku.cps.travart.dopler.decision.model.impl.LessEquals;
import at.jku.cps.travart.dopler.decision.model.impl.Not;
import at.jku.cps.travart.dopler.decision.model.impl.Or;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;

@SuppressWarnings("rawtypes")
public class ConditionParser {

	private static final String REGEX = "(?<=\\.)|(?=\\.)|((?<=\\=)|(?=\\=)|(?<=\\<)|(?=\\<)|(?<=\\>)|(?=\\>))|((?<=\\|\\|)|(?=\\|\\|))|((?<=&&)|(?=&&))|((?<=!)|(?=!))|((?<=\\()|(?=\\())|((?<=\\))|(?=\\)))";

	private static final String EOF = "EOF";

	private static final String NOT = "!";
	private static final String OR = "||";
	private static final String AND = "&&";
	private static final String GREATER = ">";
	private static final String LESS = "<";
	private static final String EQUAL = "=";
	private static final String OPENING_PARENTHESE = "(";
	private static final Object CLOSING_PARENTHESE = ")";
	private static final String CLOSING_CURRLY_PARENTHESE = "}";
	private static final String DECISION_VALUE_DELIMITER = ".";

	private static final String TRUE = "true";
	private static final String FALSE = "false";

	private String[] input;
	private int index = 0;
	private String symbol;

	private boolean isTaken = false;
	private boolean isSelected = false;

	private final IDecisionModel dm;

	public ConditionParser(final IDecisionModel dm) {
		this.dm = Objects.requireNonNull(dm);
	}

	public ICondition parse(final String str) throws ParserException {
		Objects.requireNonNull(str);
		index = 0;
		input =Arrays.stream(str.split(REGEX)).map(String::trim).filter(s -> !s.isEmpty() && !s.isBlank())
				.toArray(String[]::new);
		if (input.length > 0) {
			return parseCondition();
		}
		return ICondition.TRUE;
	}

	private ICondition parseCondition() {
		ICondition v = term();
		while (symbol.equals(OR)) {
			ICondition r = term();
			v = new Or(v, r);
		}
		return v;
	}

	private ICondition term() {
		ICondition v = comperator();
		while (symbol.equals(AND)) {
			ICondition r = comperator();
			v = new And(v, r);
		}
		return v;
	}

	private ICondition comperator() {
		ICondition v = valueDelimiter();
		while (symbol.equals(EQUAL) || symbol.equals(GREATER) || symbol.equals(LESS)) {
			String first = symbol;
			nextSymbol();
			String second = symbol;
			if (first.equals(EQUAL) && second.equals(EQUAL)) {
				ICondition r = valueDelimiter();
				v = new Equals(v, r);
			} else if (first.equals(GREATER) && second.equals(EQUAL)) {
				ICondition r = valueDelimiter();
				v = new GreaterEquals(v, r);
			} else if (first.equals(LESS) && second.equals(EQUAL)) {
				nextSymbol();
				ICondition r = valueDelimiter();
				v = new LessEquals(v, r);
			} else if (first.equals(GREATER)) {
				// second is operand
				ARangeValue value = null;
				if (RulesParser.isDoubleRangeValue(symbol)) {
					value = new DoubleValue(Double.parseDouble(symbol));
				} else if (RulesParser.isStringRangeValue(dm, symbol)) {
					value = new StringValue(symbol);
				} else {
					throw new ParserException(
							"The value of comparison \"" + v + " > " + symbol + "\" could not be parsed.");
				}
				v = new Greater(v, value);
			} else if (first.equals(LESS)) {
				// second is operand
				ARangeValue value = null;
				if (RulesParser.isDoubleRangeValue(symbol)) {
					value = new DoubleValue(Double.parseDouble(symbol));
				} else if (RulesParser.isStringRangeValue(dm, symbol)) {
					value = new StringValue(symbol);
				} else {
					throw new ParserException(
							"The value of comparison \"" + v + " < " + symbol + "\" could not be parsed.");
				}
				v = new Less(v, value);
			}
		}
		return v;
	}

	private ICondition valueDelimiter() {
		ICondition v = factor();
		if (symbol.equals(DECISION_VALUE_DELIMITER)) {
			ICondition value = factor();
			if (!(v instanceof IDecision) && !(value instanceof ARangeValue)) {
				throw new IllegalStateException(new ParserException("Expected a " + DecisionValueCondition.class));
			}
			v = new DecisionValueCondition((IDecision) v, (ARangeValue) value);
		}
		return v;
	}

	private ICondition factor() {
		nextSymbol();
		ICondition v = null;
		if (symbol.equals(CLOSING_CURRLY_PARENTHESE)) {
			nextSymbol();
		}
		if (symbol.equals(EOF)) {
			v = ICondition.TRUE;
		} else if (symbol.equals(NOT)) {
			ICondition f = factor();
			v = new Not(f);
		} else if (symbol.equals(OPENING_PARENTHESE)) {
			v = parseCondition();
			nextSymbol(); // we don't care about )
		} else if (symbol.equals(IsTakenFunction.FUNCTION_NAME)) {
			nextSymbol();
			isTaken = true;
			if (symbol.equals(OPENING_PARENTHESE)) {
				v = parseCondition();
				nextSymbol(); // we don't care about )
			}
		} else if (symbol.equals(IsSelectedFunction.FUNCTION_NAME)) {
			nextSymbol();
			isSelected = true;
			if (symbol.equals(OPENING_PARENTHESE)) {
				v = parseCondition();
				nextSymbol(); // we don't care about )
			}
		} else if (symbol.equals(GetValueFunction.FUNCTION_NAME)) {
			nextSymbol();
			if (symbol.equals(OPENING_PARENTHESE)) {
				ICondition cond = parseCondition();
				if (cond instanceof AFunction) {
					v = new GetValueFunction((IDecision) ((AFunction) cond).getParameters().get(0));
				}
				nextSymbol(); // we don't care about )
			}
		} else if (symbol.toLowerCase().equals(TRUE)) {
			v = ICondition.TRUE;
		} else if (symbol.toLowerCase().equals(FALSE)) {
			v = ICondition.FALSE;
		} else if (RulesParser.isDoubleRangeValue(symbol)) {
			v = new DoubleValue(Double.parseDouble(symbol));
			nextSymbol();
		} else if (RulesParser.isStringRangeValue(dm, symbol)) {
			v = new StringValue(symbol);
			nextSymbol();
		} else { // decision
			IDecision d = dm.get(symbol);
			nextSymbol();
			if (symbol.equals(DECISION_VALUE_DELIMITER)) {
				ICondition value = factor();
				v = new DecisionValueCondition(d, (ARangeValue) value);
			} else if (isTaken) {
				v = new IsTakenFunction(d);
			} else if (isSelected || symbol.equals(CLOSING_PARENTHESE)) {
				v = new IsSelectedFunction(d);
			} else if (d != null) {
				v = new IsSelectedFunction(d);
			} else {
				throw new ParserException("unknown function/decision for symbol " + symbol);
			}
		}
		return v;
	}

	private void nextSymbol() {
		if (index == input.length) {
			symbol = EOF;
		} else {
			symbol = input[index++];
		}
	}
}
