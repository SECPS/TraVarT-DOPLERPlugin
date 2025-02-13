package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;
import edu.kit.dopler.model.Dopler;

import java.util.Map;
import java.util.Set;

/** This interface is responsible for finding valid configurations of a {@link Dopler} model. */
public interface ValidConfigFinder {

    /**
     * Searches for valid configurations of the given {@link Dopler} model.
     *
     * @param dopler             {@link Dopler} model for which valid configurations are searched
     * @param maxAmountOfConfigs Maximal number of configurations that are searched
     *
     * @return Set of configurations. A single configuration is represented by a map of {@link IConfigurable}s as keys
     * and {@link Boolean}s as values.
     */
    Set<Map<IConfigurable, Boolean>> getValidConfigs(Dopler dopler, long maxAmountOfConfigs);
}
