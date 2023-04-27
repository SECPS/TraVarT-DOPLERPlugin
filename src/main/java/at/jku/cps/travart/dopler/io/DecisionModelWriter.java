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

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import at.jku.cps.travart.core.common.IWriter;
import at.jku.cps.travart.dopler.common.DecisionModelUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.IValue;
import at.jku.cps.travart.dopler.decision.model.impl.EnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DecisionModelWriter implements IWriter<IDecisionModel> {

	@Override
	public void write(final IDecisionModel dm, final Path path) throws IOException {
		Objects.requireNonNull(dm);
		Objects.requireNonNull(path);
		CSVFormat dmFormat = DecisionModelUtils.createCSVFormat();
		try (FileWriter out = new FileWriter(path.toFile(), StandardCharsets.UTF_8);
				CSVPrinter printer = new CSVPrinter(out, dmFormat)) {
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

				String cardinalityString = decision instanceof EnumerationDecision
						? ((EnumerationDecision) decision).getCardinality().toString()
						: "";
				printer.printRecord(decision.getId(), decision.getQuestion(), decision.getType(),
						rangeSetBuilder.toString(), cardinalityString, rulesSetBuilder.toString(),
						decision.getVisiblity());
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
