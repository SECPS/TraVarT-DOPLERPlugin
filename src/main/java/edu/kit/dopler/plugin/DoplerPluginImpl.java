/**
 * Provides the actual plugin information for DOPLER.
 *
 * @author Kevin Feichtinger
 * <p>
 * Copyright 2023 Johannes Kepler University Linz LIT Cyber-Physical Systems Lab All rights reserved
 */
package edu.kit.dopler.plugin;

import at.jku.cps.travart.core.common.*;
import edu.kit.dopler.transformation.OneWayTransformer;
import org.pf4j.Extension;

import java.util.List;

@Extension
@SuppressWarnings("rawtypes")
public class DoplerPluginImpl implements IPlugin {

    public static final String ID = "dopler-decision-plugin";

    @Override
    public IModelTransformer getTransformer() {
        return new OneWayTransformer();
    }

    @Override
    public IDeserializer getDeserializer() {
        //return new DecisionModelDeserializer();
        return null;
    }

    @Override
    public IStatistics getStatistics() {
        return null;
    }

    @Override
    public ISerializer getSerializer() {
        //return new DecisionModelSerializer();
        return null;
    }

    @Override
    public IPrettyPrinter getPrinter() {
       // return new DoplerPrettyPrinter(new DecisionModelSerializer());
        return null;
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
    public List getSupportedFileExtensions() {
        //return Collections.unmodifiableList(List.of(DecisionModelDeserializer.FILE_EXTENSION_CSV));
        return null;
    }
}
