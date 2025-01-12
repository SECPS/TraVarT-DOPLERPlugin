package edu.kit.dopler.plugin;

import at.jku.cps.travart.core.common.Format;
import at.jku.cps.travart.core.common.IDeserializer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import edu.kit.dopler.io.DecisionModelReader;
import edu.kit.dopler.model.Dopler;

import java.io.IOException;
import java.util.List;

/**
 * Implementation of {@link IDeserializer}  with the {@link Dopler} model as type variable.
 */
public class DoplerDeserializer implements IDeserializer<Dopler> {

    @Override
    public Dopler deserialize(String s, Format format) throws NotSupportedVariabilityTypeException {
        if (!format.equals(new CsvFormat())) {
            throw new NotSupportedVariabilityTypeException("Unsupported format!");
        }

        try {
            return new DecisionModelReader().read(s, "MODEL_NAME");
        } catch (IOException | edu.kit.dopler.exceptions.NotSupportedVariabilityTypeException e) {
            throw new NotSupportedVariabilityTypeException(e);
        }
    }

    @Override
    public Iterable<Format> supportedFormats() {
        return List.of(new CsvFormat());
    }
}
