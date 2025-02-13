package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;
import edu.kit.dopler.model.Dopler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** This interface is responsible for finding valid configurations of a {@link Dopler} model. */
public class InvalidConfigFinderImpl implements InvalidConfigFinder {

    private final Z3Runner z3Runner;

    public InvalidConfigFinderImpl(Z3Runner z3Runner) {
        this.z3Runner = z3Runner;
    }

    @Override
    public Set<Map<IConfigurable, Boolean>> findInvalidConfigs(Dopler dopler, long maxAmountOfConfigs) {
        //Do something with the z3Runner here to get all invalid configs
        return new HashSet<>();
    }
}
