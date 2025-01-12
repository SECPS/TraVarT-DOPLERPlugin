package edu.kit.dopler.plugin;

import at.jku.cps.travart.core.common.IPrettyPrinter;
import at.jku.cps.travart.core.common.ISerializer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import edu.kit.dopler.io.CSVUtils;
import edu.kit.dopler.model.Dopler;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
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
        return List.of(REPRESENTATION.TEXT, REPRESENTATION.TABLE);
    }

    @Override
    public String[][] toTable(Dopler model) throws NotSupportedVariabilityTypeException {
        String[][] modelAsTable = new String[model.getDecisions().size()][7];
        String modelAsString = toText(model);

        List<CSVRecord> records;
        try {
            records = CSVUtils.createCSVFormat(true).parse(new StringReader(modelAsString)).getRecords();
        } catch (IOException e) {
            throw new NotSupportedVariabilityTypeException(e);
        }

        for (int i = 0; i < records.size(); i++) {
            CSVRecord record = records.get(i);
            modelAsTable[i][0] = record.get(0);
            modelAsTable[i][1] = record.get(1);
            modelAsTable[i][2] = record.get(2);
            modelAsTable[i][3] = record.get(3);
            modelAsTable[i][4] = record.get(4);
            modelAsTable[i][5] = record.get(5);
            modelAsTable[i][6] = record.get(6);
        }

        return modelAsTable;
    }

    @Override
    public String toSvg(Dopler model) throws NotSupportedVariabilityTypeException {
        throw new NotSupportedVariabilityTypeException(
                "The dopler pretty printer does not support svg representation.");
    }
}