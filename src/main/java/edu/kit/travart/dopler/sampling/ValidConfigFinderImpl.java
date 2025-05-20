/*******************************************************************************
 * SPDX-License-Identifier: MPL-2.0
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 * <p>
 * Contributors:
 *    @author Yannick Kraml
 *    @author Kevin Feichtinger
 * <p>
 * Copyright 2024 Karlsruhe Institute of Technology (KIT)
 * KASTEL - Dependability of Software-intensive Systems
 *******************************************************************************/
package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;
import edu.kit.dopler.model.Dopler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Implementation of {@link ValidConfigFinder}. */
public class ValidConfigFinderImpl implements ValidConfigFinder {

    /** This pattern matches all parenthesis. */
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[()]");

    private final Z3Runner z3Runner;
    private final Z3OutputParser z3OutputParser;

    /**
     * Constructor of {@link ValidConfigFinder}.
     *
     * @param z3Runner       {@link Z3Runner}
     * @param z3OutputParser {@link Z3OutputParser}
     */
    public ValidConfigFinderImpl(Z3Runner z3Runner, Z3OutputParser z3OutputParser) {
        this.z3Runner = z3Runner;
        this.z3OutputParser = z3OutputParser;
    }

    /**
     * {@inheritDoc}
     * <p>
     * How it works: The dopler model is transformed into a smt stream. This stream is given to the z3 sat solver and
     * checked for satisfiability. If it is satisfiable, then the z3 sat solver returns a single config. This config is
     * saved. After that a new call to the z3 sat solver is prepared. This time an assertion is added so that the sat
     * solver cannot produce the same config again. These steps are repeated until the sat solver does not find a new
     * valid config.
     */
    @Override
    public Set<Map<IConfigurable, Boolean>> getValidConfigs(Dopler dopler, long maxAmountOfConfigs) {
        Set<Map<IConfigurable, Boolean>> configs = new HashSet<>();
        StringBuilder asserts = new StringBuilder();

        for (long l = 0L; l < maxAmountOfConfigs; l++) {
            Stream.Builder<String> smtStream = dopler.toSMTStream();
            smtStream.add(asserts.toString());
            smtStream.add("(check-sat)");
            dopler.createGetValueOFEndConstants(smtStream);
            String[] z3Output = convertOutputToArray(z3Runner.runZ3(smtStream.build()));

            if (z3Output[0].equals("unsat")) {
                //Line is unsat. There are no more configs. Break loop.
                break;
            }

            if (z3Output[0].equals("sat")) {
                //Line is sat. Create config and new assert.
                configs.add(createConfigFromAnswer(z3Output));
                asserts.append(createAssertFromAnswer(z3Output));
            }
        }

        return configs;
    }

    private String createAssertFromAnswer(String[] z3Output) {
        StringBuilder assertion = new StringBuilder();
        assertion.append("(assert (not (and");
        for (String line : z3Output) {
            assertion.append(createAssert(line));
        }
        assertion.append(")))");
        return assertion.toString();
    }

    private String createAssert(String line) {
        String[] result = SPLIT_PATTERN.split(line);
        if (3 == result.length) {
            if (result[2].contains("DECISION")) {
                return "(= %s)".formatted(result[2]);
            } else {
                return "(= %s (%s))".formatted(result[1], result[2]);
            }
        } else if (4 == result.length) {
            return "(= %s (%s(%s)))".formatted(result[1], result[2], result[3]);
        } else if (1 < result.length) {
            return "(= %s)".formatted(result[1]);
        }

        return "";
    }

    private String[] convertOutputToArray(Scanner scanner) {
        List<String> answer = new ArrayList<>();
        while (scanner.hasNextLine()) {
            answer.add(scanner.nextLine());
        }
        return answer.toArray(new String[1]);
    }

    private Map<IConfigurable, Boolean> createConfigFromAnswer(String[] answer) {
        return z3OutputParser.parseSatAnswer(String.join("", answer).replace("sat", ""));
    }
}
