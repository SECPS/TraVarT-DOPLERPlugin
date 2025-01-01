/**
 * The class implementing the Plugin class to enable code injection.
 *
 * @author Kevin Feichtinger
 * <p>
 * Copyright 2023 Johannes Kepler University Linz LIT Cyber-Physical Systems Lab All rights reserved
 */
package edu.kit.dopler.plugin;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

public class DoplerPlugin extends Plugin {

    public DoplerPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }
}
