/*******************************************************************************
 * TODO: explanation what the class does
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.io;

import at.jku.cps.travart.core.common.Format;
import at.jku.cps.travart.core.common.ISerializer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.common.DecisionModelUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision.DecisionType;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.IValue;
import at.jku.cps.travart.dopler.decision.model.impl.EnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DecisionModelSerializer implements ISerializer<IDecisionModel> {

    public class Record {

        public String id;
        public String question;
        public DecisionType type;
        public String rangeSetString;
        public String cardinalityString;
        public String rulesSetString;
        public ICondition visibility;

        public Record(String id, String question, DecisionType type, String rangeSetString, String cardinalityString,
                      String rulesSetString, ICondition visibility) {
            this.id = id;
            this.question = question;
            this.type = type;
            this.rangeSetString = rangeSetString;
            this.cardinalityString = cardinalityString;
            this.rulesSetString = rulesSetString;
            this.visibility = visibility;
        }
    }

    public List<Record> toRecords(final IDecisionModel dm) {
        List<Record> records = new ArrayList<Record>();

        for (IDecision decision : dm.getDecisions()) {
            Set<IValue> rangeSet = decision.getRange();
            StringBuilder rangeSetBuilder = new StringBuilder();
            if (rangeSet != null) {
                int i = 1;
                for (IValue value : rangeSet) {
                    rangeSetBuilder.append(value);
                    if (i != rangeSet.size()) {
                        rangeSetBuilder.append(" | ");
                    }
                    i++;
                }
            }

            Set<Rule> rulesSet = decision.getRules();
            StringBuilder rulesSetBuilder = new StringBuilder();
            for (Rule rule : rulesSet) {
                rulesSetBuilder.append(rule);
            }

            String cardinalityString = decision instanceof EnumerationDecision ?
                    ((EnumerationDecision) decision).getCardinality().toString() : "";
            Record record =
                    new Record(decision.getId(), decision.getQuestion(), decision.getType(), rangeSetBuilder.toString(),
                            cardinalityString, rulesSetBuilder.toString(), decision.getVisiblity());

            records.add(record);
        }
        return records;
    }

    public void serializeToWriter(final IDecisionModel dm, final Appendable writer) throws IOException {
        Objects.requireNonNull(dm);
        CSVFormat dmFormat = DecisionModelUtils.createCSVFormat(false);
        try (CSVPrinter printer = new CSVPrinter(writer, dmFormat)) {
            for (Record record : this.toRecords(dm)) {
                printer.printRecord(record.id, record.question, record.type, record.rangeSetString,
                        record.cardinalityString, record.rulesSetString, record.visibility);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void serializeToFile(final IDecisionModel dm, final Path path) throws IOException {
        Objects.requireNonNull(path);
        try (FileWriter out = new FileWriter(path.toFile(), StandardCharsets.UTF_8)) {
            serializeToWriter(dm, out);
        }
    }

    @Override
    public String serialize(final IDecisionModel model) throws NotSupportedVariabilityTypeException {
        StringBuffer buf = new StringBuffer();
        try {
            serializeToWriter(model, buf);
            return buf.toString();
        } catch (IOException e) {
            // cannot happen as we are not actually doing any IO
            return null;
        }
    }

    @Override
    public Format getFormat() {
        return DecisionModelDeserializer.CSV_FORMAT;
    }
}
