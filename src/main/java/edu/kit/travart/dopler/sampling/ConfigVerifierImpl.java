package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;
import edu.kit.dopler.model.Dopler;

import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

/** Implementation of {@link ConfigVerifier}. */
public class ConfigVerifierImpl implements ConfigVerifier {

    private final Z3Runner z3Runner;

    /**
     * Constructor of {@link ConfigVerifierImpl}.
     *
     * @param z3Runner {@link Z3Runner}
     */
    public ConfigVerifierImpl(Z3Runner z3Runner) {
        this.z3Runner = z3Runner;
    }

    @Override
    public boolean verify(Dopler dopler, Map<IConfigurable, Boolean> map) {
        Stream.Builder<String> builder = dopler.toSMTStream();
        dopler.createGetValueOFEndConstants(builder);

        StringBuilder assertion = new StringBuilder();
        assertion.append("(assert (and");
        for (Map.Entry<IConfigurable, Boolean> pair : map.entrySet()) {
            assertion.append("(= %s %b)".formatted(pair.getKey().getName(), pair.getValue()));
        }
        assertion.append("))");

        builder.add(assertion.toString());
        return isSatisfiable(builder);
    }

    /**
     * Gets the smt stream of the dopler model and adds the comment (check-sat) and then calls the satSolver with the
     * stream
     *
     * @param builder Stream Builder of the DOPLER MODEL which should be fed into the solver
     *
     * @return True if the encoding is sat or false if the encoding is unsat
     */
    private boolean isSatisfiable(Stream.Builder<String> builder) {
        builder.add("(check-sat)");
        builder.add("(exit)");
        Stream<String> stream = builder.build();
        Scanner scanner = z3Runner.runZ3(stream);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if ("sat".equals(line)) {
                return true;
            } else if ("unsat".equals(line)) {
                return false;
            }
        }

        throw new SatException("Answer was neither sat or unsat");
    }
}
