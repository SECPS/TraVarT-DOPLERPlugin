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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.io.DecisionModelDeserializer;

public class DMReaderTest {

	@Test
	public void testInternalAsserts() {
		IDecisionModel dm = null;
		DecisionModelDeserializer reader = new DecisionModelDeserializer();
		try {
			dm = reader.deserializeFromFile(Paths.get(DMReaderTest.class.getClassLoader().getResource("DOPLERToolsDM.csv").toURI()));
		} catch (IOException e) {
			fail("IO Exception occured while reading Decision Model");
			e.printStackTrace();
		} catch (NotSupportedVariabilityTypeException e) {
			fail("Variability type was not supported while reading Decision Model");
			e.printStackTrace();
		} catch (URISyntaxException e) {
			fail("Path to reading Decision Model was invalid");
			e.printStackTrace();
		}
		assertNotNull(dm);
	}

}
