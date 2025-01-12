package edu.kit.dopler.plugin;

import at.jku.cps.travart.core.common.Format;
import at.jku.cps.travart.core.common.IDeserializer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import edu.kit.dopler.io.DecisionModelReader;
import edu.kit.dopler.model.Dopler;

import java.io.IOException;
import java.util.List;

import static edu.kit.dopler.plugin.DoplerPluginImpl.CSV_FORMAT;

class DoplerDeserializer implements IDeserializer<Dopler> {

    @Override
    public Dopler deserialize(String serial, Format format) throws NotSupportedVariabilityTypeException {
        if (!format.equals(CSV_FORMAT)) {
            throw new NotSupportedVariabilityTypeException("Unsupported format!");
        }

        try {
            return new DecisionModelReader().read(serial, "MODEL_NAME");
        } catch (IOException | edu.kit.dopler.exceptions.NotSupportedVariabilityTypeException e) {
            throw new NotSupportedVariabilityTypeException(e);
        }
    }

    @Override
    public Iterable<Format> supportedFormats() {
        return List.of(CSV_FORMAT);
    }
}
