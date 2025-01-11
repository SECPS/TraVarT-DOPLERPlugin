/*******************************************************************************
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 *
 * Contributors: 
 *    @author Fabian Eger
 *    @author Kevin Feichtinger
 *
 * Copyright 2024 Karlsruhe Institute of Technology (KIT)
 * KASTEL - Dependability of Software-intensive Systems
 * All rights reserved
 *******************************************************************************/
package edu.kit.dopler.io.parser;

import edu.kit.dopler.common.DoplerUtils;
import edu.kit.dopler.exceptions.NoActionInRuleException;
import edu.kit.dopler.exceptions.ParserException;
import edu.kit.dopler.model.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class RulesParser {

    private static final String NO_ACTION_IN_RULE = "A rule must have at least one action to perform!";
    private static final Pattern PATTERN = Pattern.compile(";|}\"?");

    private final Dopler dm;

    public RulesParser(Dopler decisions) {
        this.dm = Objects.requireNonNull(decisions);
    }

    static boolean isDoubleRangeValue(final String symbol) {
        try {
            Double.parseDouble(symbol);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    static boolean isStringRangeValue(Dopler dm, String symbol) {
        for (IDecision<?> decision : DoplerUtils.getEnumerationDecisions(dm)) {
            EnumerationDecision enumDecision = (EnumerationDecision) decision;
            // add the value for a enum decision
            for (EnumerationLiteral literal : enumDecision.getEnumeration().getEnumerationLiterals()) {
                if (literal.getValue().equals(symbol)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<Rule> parse(IDecision<?> decision, String[] csvRuleSplit) throws ParserException {
        Objects.requireNonNull(decision);
        Objects.requireNonNull(csvRuleSplit);
        Set<Rule> rules = new HashSet<>();

        if (0 == csvRuleSplit.length) {
            throw new AssertionError();
        }

        for (String csvRule : csvRuleSplit) {
            // Split to condition and actions
            String[] ruleParts =
                    Arrays.stream(csvRule.split("\\{")).map(String::trim).filter(s -> !s.isEmpty() && !s.isBlank())
                            .toArray(String[]::new);
            if (2 > ruleParts.length) {
                throw new NoActionInRuleException(NO_ACTION_IN_RULE);
            }
            // get condition from first part
            ConditionParser conditionParser = new ConditionParser(dm);
            IExpression condition = conditionParser.parse(ruleParts[0]);
            // derive actions from the second part
            String[] csvActions = Arrays.stream(PATTERN.split(ruleParts[1])).map(String::trim)
                    .filter(s -> !s.isEmpty() && !s.isBlank()).toArray(String[]::new);
            Set<IAction> actions = new HashSet<>();
            for (String csvAction : csvActions) {
                ActionParser actionParser = new ActionParser(dm);
                IAction action = actionParser.parse(csvAction);
                actions.add(action);
            }
            Rule rule = new Rule(condition, actions);
            rules.add(rule);
        }
        return rules;
    }
}
