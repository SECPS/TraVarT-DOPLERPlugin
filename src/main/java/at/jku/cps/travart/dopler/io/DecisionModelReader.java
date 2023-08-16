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

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import at.jku.cps.travart.core.common.IReader;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.common.DecisionModelUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.factory.impl.DecisionModelFactory;
import at.jku.cps.travart.dopler.decision.impl.DMCSVHeader;
import at.jku.cps.travart.dopler.decision.impl.DecisionModel;
import at.jku.cps.travart.dopler.decision.model.ADecision;
import at.jku.cps.travart.dopler.decision.model.ADecision.DecisionType;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Cardinality;
import at.jku.cps.travart.dopler.decision.model.impl.EnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;
import at.jku.cps.travart.dopler.decision.parser.ConditionParser;
import at.jku.cps.travart.dopler.decision.parser.RulesParser;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DecisionModelReader implements IReader<IDecisionModel> {

	public static final String FILE_EXTENSION_CSV = ".csv";

	private static final String CARDINALITY_NOT_SUPPORTED_ERROR = "Cardinality %s not supported for decision of type %s";

	private final DecisionModelFactory factory;

	public DecisionModelReader() {
		factory = DecisionModelFactory.getInstance();
	}

	@Override
	public IDecisionModel read(final Path path) throws IOException, NotSupportedVariabilityTypeException {
		Objects.requireNonNull(path);
		DecisionModel dm = factory.create();
		dm.setName(path.getFileName().toString());
		dm.setSourceFile(path.toAbsolutePath().toString());
		CSVFormat dmFormat = DecisionModelUtils.createCSVFormat();

		try (Reader in = new FileReader(path.toFile(), StandardCharsets.UTF_8)) {
			Iterable<CSVRecord> records = dmFormat.parse(in);
			for (CSVRecord record : records) {
				String id = record.get(DMCSVHeader.ID).trim();
				String typeString = record.get(DMCSVHeader.TYPE).trim();
				ADecision decision = null;
				if (DecisionType.BOOLEAN.equalString(typeString)) {
					decision = factory.createBooleanDecision(id);
				} else if (DecisionType.ENUM.equalString(typeString)) {
					decision = factory.createEnumDecision(id);
				} else if (DecisionType.NUMBER.equalString(typeString)) {
					decision = factory.createNumberDecision(id);
				} else if (DecisionType.STRING.equalString(typeString)) {
					decision = factory.createStringDecision(id);
				} else {
					throw new NotSupportedVariabilityTypeException(typeString);
				}

				assert decision != null;
				decision.setQuestion(record.get(DMCSVHeader.QUESTION).trim());

				String range = record.get(DMCSVHeader.RANGE);
				if (!range.isBlank()) {
					if (decision.getType() == DecisionType.NUMBER) {
						String[] ranges = Arrays.stream(range.split("-")).map(String::trim)
								.filter(s -> !s.isEmpty() && !s.isBlank()).toArray(String[]::new);
						Range<Double> valueRange = factory.createNumberValueRange(ranges);
						decision.setRange(valueRange);
					} else {
						String[] options = Arrays.stream(range.split("\\|")).map(String::trim)
								.filter(s -> !s.isEmpty() && !s.isBlank()).toArray(String[]::new);
						Range<String> valueOptions = factory.createEnumValueOptions(options);
						decision.setRange(valueOptions);
					}
				}

				String cardinality = record.get(DMCSVHeader.CARDINALITY).trim();
				if (!cardinality.isEmpty()) {
					String[] values = Arrays.stream(cardinality.split(":")).map(String::trim)
							.filter(s -> !s.isEmpty() && !s.isBlank()).toArray(String[]::new);
					if (!(decision instanceof EnumerationDecision) || !(values.length == 2)) {
						throw new NotSupportedVariabilityTypeException(String.format(CARDINALITY_NOT_SUPPORTED_ERROR,
								cardinality, decision.getClass().getCanonicalName()));
					}
					Cardinality c = factory.createCardinality(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
					((EnumerationDecision) decision).setCardinality(c);
				}
				dm.add(decision);
			}
		}

		try (Reader secondIn = new FileReader(path.toFile(), StandardCharsets.UTF_8)) {
			Iterable<CSVRecord> secondParse = dmFormat.parse(secondIn);
			ConditionParser vParser = new ConditionParser(dm);
			RulesParser rParser = new RulesParser(dm);

			for (CSVRecord record : secondParse) {
				String id = record.get(DMCSVHeader.ID);
				IDecision decision = dm.get(id);

				String csvRules = record.get(DMCSVHeader.RULES);
				if (!csvRules.isEmpty()) {
					// TODO: Find better way to spit the string of rules (decision names may have
					// "if" as part of their names)
					String[] CSVruleSplit = Arrays.stream(csvRules.split("if")).map(String::trim)
							.filter(s -> !s.isEmpty() && !s.isBlank()).toArray(String[]::new);
					Set<Rule> rules = rParser.parse(decision, CSVruleSplit);
					decision.addRules(rules);
				}

				String visiblity = record.get(DMCSVHeader.VISIBLITY);
				ICondition v = vParser.parse(visiblity);
				decision.setVisibility(v);
			}
		}
		return dm;
	}

	@Override
	public Iterable<String> fileExtensions() {
		return Collections.unmodifiableList(List.of(FILE_EXTENSION_CSV));
	}
}
