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

    /**
     * Constructor of {@link DoplerPrettyPrinter}.
     *
     * @param serializer {@link ISerializer} with the {@link Dopler} model as type variable
     */
    public DoplerPrettyPrinter(ISerializer<Dopler> serializer) {
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
            CSVRecord csvRecord = records.get(i);
            modelAsTable[i][0] = csvRecord.get(0);
            modelAsTable[i][1] = csvRecord.get(1);
            modelAsTable[i][2] = csvRecord.get(2);
            modelAsTable[i][3] = csvRecord.get(3);
            modelAsTable[i][4] = csvRecord.get(4);
            modelAsTable[i][5] = csvRecord.get(5);
            modelAsTable[i][6] = csvRecord.get(6);
        }

        return modelAsTable;
    }

    @Override
    public String toSvg(Dopler model) throws NotSupportedVariabilityTypeException {
        throw new NotSupportedVariabilityTypeException(
                "The dopler pretty printer does not support svg representation.");
    }
}