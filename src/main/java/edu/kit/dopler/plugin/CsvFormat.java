package edu.kit.dopler.plugin;

import at.jku.cps.travart.core.common.Format;

/** Singleton that stores the used csv format. */
public final class CsvFormat extends Format {

    /**
     * Constructor of {@link CsvFormat}.
     */
    public CsvFormat() {
        super("csv", ".csv", true, true);
    }
}
