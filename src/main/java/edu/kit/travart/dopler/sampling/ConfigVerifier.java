package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;
import edu.kit.dopler.model.Dopler;

import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

public class ConfigVerifier {

    private final Z3Runner z3Runner;

    public ConfigVerifier() {
        z3Runner = new Z3Runner();
    }

    public boolean verify(Dopler dopler, Map<IConfigurable, Boolean> map) {
        return false;
    }

    /**
     * Gets the smt stream of the dopler model and adds the comment (check-sat) and then calls the satSolver with the
     * stream
     *
     * @param builder Stream Builder of the DOPLER MODEL which should be fed into the solver
     *
     * @return True if the encoding is sat or false if the encoding is unsat
     *
     * @throws Exception
     */
    boolean isSatisfiable(Stream.Builder<String> builder) {
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

        throw new SatException();
    }
}
