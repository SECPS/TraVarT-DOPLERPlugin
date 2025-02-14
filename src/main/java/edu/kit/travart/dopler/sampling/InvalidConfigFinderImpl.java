package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;
import com.google.common.collect.Sets;
import edu.kit.dopler.model.Dopler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Implementation of {@link InvalidConfigFinder}. */
public class InvalidConfigFinderImpl implements InvalidConfigFinder {

    private final ValidConfigFinder validConfigFinder;
    private final ConfigVerifier configVerifier;

    /**
     * Constructor of {@link InvalidConfigFinderImpl}.
     *
     * @param validConfigFinder {@link ValidConfigFinder}
     * @param configVerifier    {@link ConfigVerifier}
     */
    public InvalidConfigFinderImpl(ValidConfigFinder validConfigFinder, ConfigVerifier configVerifier) {
        this.validConfigFinder = validConfigFinder;
        this.configVerifier = configVerifier;
    }

    @Override
    public Set<Map<IConfigurable, Boolean>> findInvalidConfigs(Dopler dopler, long maxAmountOfConfigs) {
        Set<Map<IConfigurable, Boolean>> validConfigs = validConfigFinder.getValidConfigs(dopler, 1L);
        if (validConfigs.isEmpty()) {
            return new HashSet<>();
        }
        List<String> variableNames =
                validConfigs.stream().findFirst().get().keySet().stream().map(IConfigurable::getName).toList();

        //There are 2^|variables| configs
        long theoreticMax = (long) StrictMath.pow(2, variableNames.size());

        //Create all possible configs and check if they are valid. When not, then add them to the set.
        Set<Map<IConfigurable, Boolean>> allConfigs = Sets.newConcurrentHashSet();
        for (long l = 0L; l < theoreticMax; l++) {
            Map<IConfigurable, Boolean> newConfig = createNewConfig(variableNames, l);
            if (!configVerifier.verify(dopler, newConfig)) {
                allConfigs.add(newConfig);
            }

            if (allConfigs.size() >= maxAmountOfConfigs) {
                break;
            }
        }

        return allConfigs;
    }

    private static Map<IConfigurable, Boolean> createNewConfig(List<String> variables, long l) {
        Map<IConfigurable, Boolean> newConfig = new HashMap<>();
        for (int i = 0; i < variables.size(); i++) {
            String name = variables.get(i);
            boolean value = 0L != (l & (1L << i));
            newConfig.put(new DoplerConfigurable(name, value), value);
        }
        return newConfig;
    }
}
