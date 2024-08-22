/**
 * Provides the actual plugin information for DOPLER.
 *
 * @author Kevin Feichtinger
*
* Copyright 2023 Johannes Kepler University Linz
* LIT Cyber-Physical Systems Lab
* All rights reserved
 */
package at.jku.cps.travart.dopler.plugin;

import java.util.Collections;
import java.util.List;

import org.pf4j.Extension;

import at.jku.cps.travart.core.common.IModelTransformer;
import at.jku.cps.travart.core.common.IPlugin;
import at.jku.cps.travart.core.common.IPrettyPrinter;
import at.jku.cps.travart.core.common.IDeserializer;
import at.jku.cps.travart.core.common.IStatistics;
import at.jku.cps.travart.core.common.ISerializer;
import at.jku.cps.travart.dopler.io.DecisionModelDeserializer;
import at.jku.cps.travart.dopler.io.DecisionModelSerializer;
import at.jku.cps.travart.dopler.printer.DoplerPrettyPrinter;
import at.jku.cps.travart.dopler.transformation.DecisionModelTransformer;

@Extension
@SuppressWarnings("rawtypes")
public class DoplerPluginImpl implements IPlugin {

	public static final String ID = "dopler-decision-plugin";

	@Override
	public IModelTransformer getTransformer() {
		return new DecisionModelTransformer();
	}

	@Override
	public IDeserializer getDeserializer() {
		return new DecisionModelDeserializer();
	}

	@Override
	public IStatistics getStatistics() {
		return null;
	}

	@Override
	public ISerializer getSerializer() {
		return new DecisionModelSerializer();
	}

	@Override
	public IPrettyPrinter getPrinter() {
		return new DoplerPrettyPrinter(new DecisionModelSerializer());
	}

	@Override
	public String getName() {
		return "Decision-Oriented Product Line Engineering for effective Reuse";
	}

	@Override
	public String getAbbreviation() {
		return "DOPLER";
	}

	@Override
	public String getVersion() {
		return "0.0.1";
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public List getSupportedFileExtensions() {
		return Collections.unmodifiableList(List.of(DecisionModelDeserializer.FILE_EXTENSION_CSV));
	}

}
