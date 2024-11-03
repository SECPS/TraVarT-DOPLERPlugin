package at.jku.cps.travart.dopler.printer;

import at.jku.cps.travart.core.common.IPrettyPrinter;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.impl.DMCSVHeader;
import at.jku.cps.travart.dopler.io.DecisionModelSerializer;
import at.jku.cps.travart.dopler.io.DecisionModelSerializer.Record;

import java.util.List;

public class DoplerPrettyPrinter implements IPrettyPrinter<IDecisionModel> {

    private DecisionModelSerializer serializer;

    public DoplerPrettyPrinter(DecisionModelSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public String toText(IDecisionModel model) throws NotSupportedVariabilityTypeException {
        return serializer.serialize(model);
    }

    @Override
    public List<REPRESENTATION> representations() {
        return List.of(REPRESENTATION.TABLE, REPRESENTATION.TEXT);
    }

    @Override
    public String[][] toTable(IDecisionModel model) throws NotSupportedVariabilityTypeException {
        String[] header = DMCSVHeader.stringArray();
        List<Record> records = serializer.toRecords(model);

        String[][] table = new String[records.size() + 1][header.length];
        table[0] = header;
        for (int i = 0; i < records.size(); i++) {
            Record record = records.get(i);
            String[] row = {record.id, record.question, record.type.toString(), record.rangeSetString,
                    record.cardinalityString, record.rulesSetString, record.visibility.toString()};
            table[i + 1] = row;
        }
        return table;
    }

    @Override
    public String toSvg(IDecisionModel model) throws NotSupportedVariabilityTypeException {
        throw new NotSupportedVariabilityTypeException(
                "The dopler pretty printer does not support svg representation.");
    }
}
