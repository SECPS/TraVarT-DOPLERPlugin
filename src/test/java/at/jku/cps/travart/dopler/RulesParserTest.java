/*******************************************************************************
 * TODO: explanation what the class does
 *  
 *  @author Kevin Feichtinger
 *  
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.core.helpers.TraVarTUtils;
import at.jku.cps.travart.dopler.common.DecisionModelUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.impl.DMCSVHeader;
import at.jku.cps.travart.dopler.decision.model.ADecision.DecisionType;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.EnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.NumberDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.StringDecision;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;
import at.jku.cps.travart.dopler.decision.parser.RulesParser;
import at.jku.cps.travart.dopler.io.DecisionModelReader;

public class RulesParserTest {

	Path filePath;
	CSVFormat dmFormat;

	@BeforeEach
	public void setUp() throws Exception {
		filePath = Paths.get(RulesParserTest.class.getClassLoader().getResource("DOPLERToolsDM.csv").toURI());
		dmFormat = DecisionModelUtils.createCSVFormat();
	}

	private IDecisionModel getDecisionModel() {
		IDecisionModel dm = null;
		DecisionModelReader reader = new DecisionModelReader();
		try {
			dm = reader.read(filePath);
		} catch (IOException e) {
			fail("IO Exception occured while reading Decision Model");
			e.printStackTrace();
		} catch (NotSupportedVariabilityTypeException e) {
			fail("Variability type was not supported while reading Decision Model");
			e.printStackTrace();
		}
		return dm;
	}

	@Test
	public void rulesParserNotNull() {
		RulesParser rp = new RulesParser(getDecisionModel());
		assertNotNull(rp);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void rulesNotEmpty() throws IOException, NotSupportedVariabilityTypeException {
		IDecisionModel dm = getDecisionModel();
		RulesParser rp = new RulesParser(dm);
		Reader secondIn = new FileReader(filePath.toFile());
		Iterable<CSVRecord> secondParse = dmFormat.parse(secondIn);
		for (CSVRecord c : secondParse) {
			String csvRules = c.get(DMCSVHeader.RULES);
			if (!csvRules.isBlank()) {
				String[] CSVruleSplit = TraVarTUtils.splitString(csvRules, "if");
				IDecision decision = null;
				String typeString = c.get(DMCSVHeader.TYPE);
				if (DecisionType.BOOLEAN.equalString(typeString)) {
					decision = new BooleanDecision("");
				} else if (DecisionType.ENUM.equalString(typeString)) {
					decision = new EnumerationDecision("");
				} else if (DecisionType.NUMBER.equalString(typeString)) {
					decision = new NumberDecision("");
				} else if (DecisionType.STRING.equalString(typeString)) {
					decision = new StringDecision("");
				} else {
					throw new NotSupportedVariabilityTypeException(typeString);
				}
				String range = c.get(DMCSVHeader.RANGE);
				String[] options = TraVarTUtils.splitString(range, "\\|");
				Range<String> valueOptions = createValueOptions(decision, options);
				decision.setRange(valueOptions);
				assert !rp.parse(decision, CSVruleSplit).isEmpty();
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Range<String> createValueOptions(final IDecision decision, final String[] options) {
		Range<String> range = new Range();
		for (String o : options) {
			range.add(new StringValue(o));
		}
		return range;
	}
}
