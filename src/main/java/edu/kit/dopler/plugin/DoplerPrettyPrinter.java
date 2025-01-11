package edu.kit.dopler.plugin;

import at.jku.cps.travart.core.common.IPrettyPrinter;
import at.jku.cps.travart.core.common.ISerializer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import edu.kit.dopler.model.Dopler;

import java.util.List;

/** Implementation of IPrettyPrinter */
public class DoplerPrettyPrinter implements IPrettyPrinter<Dopler> {

    private final ISerializer<Dopler> serializer;

    DoplerPrettyPrinter(ISerializer<Dopler> serializer) {
        this.serializer = serializer;
    }

    @Override
    public String toText(Dopler model) throws NotSupportedVariabilityTypeException {
        return serializer.serialize(model);
    }

    @Override
    public List<REPRESENTATION> representations() {
        return List.of(REPRESENTATION.TEXT);
    }

    @Override
    public String[][] toTable(Dopler model) throws NotSupportedVariabilityTypeException {
        throw new NotSupportedVariabilityTypeException(
                "The dopler pretty printer does not support table representation.");
    }

    @Override
    public String toSvg(Dopler model) throws NotSupportedVariabilityTypeException {
        throw new NotSupportedVariabilityTypeException(
                "The dopler pretty printer does not support svg representation.");
    }
}