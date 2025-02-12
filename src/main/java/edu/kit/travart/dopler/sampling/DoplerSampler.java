package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;
import at.jku.cps.travart.core.common.ISampler;
import edu.kit.dopler.model.Dopler;

import java.util.Map;
import java.util.Set;

public class DoplerSampler implements ISampler<Dopler> {

    private final ValidConfigFinder validConfigFinder;
    private final InvalidConfigFinder invalidConfigFinder;
    private final ConfigVerifier configVerifier;

    public DoplerSampler() {
        validConfigFinder = new ValidConfigFinder();
        invalidConfigFinder = new InvalidConfigFinder();
        configVerifier = new ConfigVerifier();
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
