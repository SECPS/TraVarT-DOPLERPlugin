package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;
import edu.kit.dopler.model.Dopler;

import java.util.Map;

/** This interface is responsible for checking if a config of a {@link Dopler} model is valid. */
interface ConfigVerifier {

    /**
     * Checks of the given configuration is a valid configuration of the given {@link Dopler} model
     *
     * @param dopler {@link Dopler} model to check the configuration against
     * @param map    Configuration to check for validity
     *
     * @return true if the configuration is valid, false otherwise
     */
    boolean verify(Dopler dopler, Map<IConfigurable, Boolean> map);
}
