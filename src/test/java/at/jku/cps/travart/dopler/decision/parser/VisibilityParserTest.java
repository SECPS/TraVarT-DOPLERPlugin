/*******************************************************************************
 * TODO: explanation what the class does
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.common.DecisionModelUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.impl.DMCSVHeader;
import at.jku.cps.travart.dopler.io.DecisionModelDeserializer;

public class VisibilityParserTest {
	ConditionParser vp;
	IDecisionModel dm;
	CSVFormat dmFormat;

	@BeforeEach
	public void setUp() throws Exception {
		dmFormat = DecisionModelUtils.createCSVFormat(true);
		Path toRead = Paths
				.get(new VisibilityParserTest().getClass().getClassLoader().getResource("DOPLERToolsDM.csv").toURI());
		DecisionModelDeserializer reader = new DecisionModelDeserializer();
		try {
			dm = reader.deserializeFromFile(toRead);
			vp = new ConditionParser(dm);
		} catch (IOException | NotSupportedVariabilityTypeException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testVisibilityParserNoNull() {
		assertThrows(NullPointerException.class, () -> vp = new ConditionParser(null));
	}

	@Test
	public void testParseNoNull() {
		assertThrows(NullPointerException.class, () -> vp.parse(null));
	}

	@Test
	public void testParse() throws FileNotFoundException, IOException, URISyntaxException {
		Iterable<CSVRecord> parse = dmFormat.parse(new FileReader(new File(
				new VisibilityParserTest().getClass().getClassLoader().getResource("DOPLERToolsDM.csv").toURI())));
		for (CSVRecord record : parse) {
			String visiblity = record.get(DMCSVHeader.VISIBLITY);
			vp.parse(visiblity);
		}
	}

}
