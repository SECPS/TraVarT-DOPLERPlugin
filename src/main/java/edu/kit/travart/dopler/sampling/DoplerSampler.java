package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;
import at.jku.cps.travart.core.common.ISampler;
import edu.kit.dopler.model.Dopler;

import java.util.Map;
import java.util.Set;

/** Implementation of {@link ISampler} with the {@link Dopler} model. */
public class DoplerSampler implements ISampler<Dopler> {

    private final ValidConfigFinder validConfigFinder;
    private final InvalidConfigFinder invalidConfigFinder;
    private final ConfigVerifier configVerifier;

    public DoplerSampler() {
        Z3RunnerImpl z3Runner = new Z3RunnerImpl();
        Z3OutputParserImpl z3OutputParser = new Z3OutputParserImpl();
        validConfigFinder = new ValidConfigFinderImpl(z3Runner, z3OutputParser);
        invalidConfigFinder = new InvalidConfigFinderImpl(z3Runner);
        configVerifier = new ConfigVerifierImpl(z3Runner);
    }

    @Override
    public Set<Map<IConfigurable, Boolean>> sampleValidConfigurations(Dopler dopler) {
        return validConfigFinder.getValidConfigs(dopler, Long.MAX_VALUE);
    }

    @Override
    public Set<Map<IConfigurable, Boolean>> sampleValidConfigurations(Dopler dopler, long l) {
        return validConfigFinder.getValidConfigs(dopler, l);
    }

    @Override
    public Set<Map<IConfigurable, Boolean>> sampleInvalidConfigurations(Dopler dopler) {
        return invalidConfigFinder.findInvalidConfigs(dopler, Long.MAX_VALUE);
    }

    @Override
    public Set<Map<IConfigurable, Boolean>> sampleInvalidConfigurations(Dopler dopler, long l) {
        return invalidConfigFinder.findInvalidConfigs(dopler, l);
    }

    @Override
    public boolean verifySampleAs(Dopler dopler, Map<IConfigurable, Boolean> map) {
        return configVerifier.verify(dopler, map);
    }
}
