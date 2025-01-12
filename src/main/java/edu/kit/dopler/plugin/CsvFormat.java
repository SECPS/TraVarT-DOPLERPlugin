package edu.kit.dopler.plugin;

import at.jku.cps.travart.core.common.Format;

/** Singleton that stores the used csv format. */
public final class CsvFormat extends Format {

    private static final String CSV = ".csv";
    private static final CsvFormat instance = new CsvFormat();

    private CsvFormat() {
        super("csv", CSV, true, true);
    }

    /**
     * @return An instance of the csv {@link Format}
     */
    public static Format getInstance() {
        return instance;
    }
}
