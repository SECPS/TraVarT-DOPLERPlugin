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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.core.helpers.TraVarTUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.ParserException;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.io.DecisionModelReader;

public class RulesParserTest {
	RulesParser rp;
	IDecisionModel dm;

	@BeforeEach
	public void setUp() throws Exception {
		Path toRead = Paths
				.get(new RulesParserTest().getClass().getClassLoader().getResource("DOPLERToolsDM.csv").toURI());
		try {
			dm = (new DecisionModelReader()).read(toRead);
			rp = new RulesParser(dm);
		} catch (IOException | NotSupportedVariabilityTypeException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testRulesParser() {
		assertThrows(NullPointerException.class, () -> new RulesParser(null));
	}

	@Test
	public void testParseNoNullDecision() {
		String[] csvRules = { "test", "test1", "test2" };
		assertThrows(NullPointerException.class, () -> rp.parse(null, csvRules));
	}

	@Test
	public void testParseNoNullCSVRule() {
		assertThrows(NullPointerException.class, () -> rp.parse(new BooleanDecision("test"), null));
	}

	@Test
	public void testParseParserException() {
		String csvRules = "if (ALL) { test = true; }";
		String[] CSVruleSplit = TraVarTUtils.splitString(csvRules, "if");
		for (IDecision<?> d : dm.getDecisions()) {
			assertThrows(ParserException.class, () -> rp.parse(d, CSVruleSplit));
		}
	}

	@Test
	public void testParse() {
		String csvRules = "if (!ALL) { ALL = true; }";
		String[] CSVruleSplit = TraVarTUtils.splitString(csvRules, "if");
		for (IDecision<?> d : dm.getDecisions()) {
			rp.parse(d, CSVruleSplit).isEmpty();
		}
	}

}
