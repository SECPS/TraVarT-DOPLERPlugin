/**
 * Provides the actual plugin information for DOPLER.
 *
 * @author Kevin Feichtinger
 * <p>
 * Copyright 2023 Johannes Kepler University Linz LIT Cyber-Physical Systems Lab All rights reserved
 */
package edu.kit.dopler.plugin;

import at.jku.cps.travart.core.common.*;
import edu.kit.dopler.injection.Injector;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.Transformer;
import org.pf4j.Extension;

import java.util.List;

/**
 * Implementation of {@link IPlugin} for the {@link Dopler} model.
 */
@Extension
public class DoplerPluginImpl implements IPlugin<Dopler> {

    private static final String CSV = ".csv";
    public static final Format CSV_FORMAT = new Format("csv", CSV, true, true);

    private static final String ID = "dopler-decision-plugin";

    private final Injector injector;

    public DoplerPluginImpl() {
        injector = new Injector();
    }

    @Override
    public IModelTransformer<Dopler> getTransformer() {
        return injector.getInstance(Transformer.class);
    }

    @Override
    public IDeserializer<Dopler> getDeserializer() {
        return injector.getInstance(DoplerDeserializer.class);
    }

    @Override
    public IStatistics<Dopler> getStatistics() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ISerializer<Dopler> getSerializer() {
        return injector.getInstance(DoplerSerializer.class);
    }

    @Override
    public IPrettyPrinter<Dopler> getPrinter() {
        return injector.getInstance(DoplerPrettyPrinter.class);
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