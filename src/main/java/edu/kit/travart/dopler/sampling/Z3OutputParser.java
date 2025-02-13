package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;

import java.util.Map;

/** This interface is responsible for parsing the output of the z3 sat solver. */
public interface Z3OutputParser {

    /**
     * Parse the answer of the z3 sat solver and converts it into a map of {@link IConfigurable}.
     *
     * @param z3Answer Answer from the z3 sat solver that should be parsed
     *
     * @return Map of {@link IConfigurable}s as keys and {@link Boolean}s as values
     */
    Map<IConfigurable, Boolean> parseSatAnswer(String z3Answer);
}
