/**
 * Provides the actual plugin information for DOPLER.
 *
 * @author Kevin Feichtinger
 * <p>
 * Copyright 2023 Johannes Kepler University Linz LIT Cyber-Physical Systems Lab All rights reserved
 */
package edu.kit.dopler.plugin;

import at.jku.cps.travart.core.common.*;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.Transformer;
import org.pf4j.Extension;

import java.util.List;

@Extension
public class DoplerPluginImpl implements IPlugin<Dopler> {

    private static final String CSV = ".csv";
    static final Format CSV_FORMAT = new Format("csv", CSV, true, true);

    private static final String ID = "dopler-decision-plugin";

    @Override
    public IModelTransformer<Dopler> getTransformer() {
        return new Transformer();
    }

    @Override
    public IDeserializer<Dopler> getDeserializer() {
        return new DoplerDeserializer();
    }

    @Override
    public IStatistics<Dopler> getStatistics() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ISerializer<Dopler> getSerializer() {
        return new DoplerSerializer();
    }

    @Override
    public IPrettyPrinter<Dopler> getPrinter() {
        return new DoplerPrettyPrinter(getSerializer());
    }

    @Override
    public String getName() {
        return "Decision-Oriented Product Line Engineering for effective Reuse";
    }

    @Override
    public String getAbbreviation() {
        return "DOPLER";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<String> getSupportedFileExtensions() {
        return List.of(CSV);
    }
}