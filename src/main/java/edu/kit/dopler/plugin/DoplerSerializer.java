package edu.kit.dopler.plugin;

import at.jku.cps.travart.core.common.Format;
import at.jku.cps.travart.core.common.ISerializer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import edu.kit.dopler.io.DecisionModelWriter;
import edu.kit.dopler.model.Dopler;

import java.io.IOException;

import static edu.kit.dopler.plugin.DoplerPluginImpl.CSV_FORMAT;

public class DoplerSerializer implements ISerializer<Dopler> {

    @Override
    public String serialize(Dopler model) throws NotSupportedVariabilityTypeException {
        try {
            return new DecisionModelWriter().write(model);
        } catch (IOException e) {
            throw new NotSupportedVariabilityTypeException(e);
        }
    }

    @Override
    public Format getFormat() {
        return CSV_FORMAT;
    }
}
