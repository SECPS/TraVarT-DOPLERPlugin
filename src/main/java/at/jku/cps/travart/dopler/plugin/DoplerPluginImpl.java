/**
 * TODO: explanation what the class does
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
import at.jku.cps.travart.core.common.IReader;
import at.jku.cps.travart.core.common.IStatistics;
import at.jku.cps.travart.core.common.IWriter;
import at.jku.cps.travart.dopler.io.DecisionModelReader;
import at.jku.cps.travart.dopler.io.DecisionModelWriter;
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
	public IReader getReader() {
		return new DecisionModelReader();
	}

	@Override
	public IStatistics getStatistics() {
		return null;
	}

	@Override
	public IWriter getWriter() {
		return new DecisionModelWriter();
	}

	@Override
	public String getName() {
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
		return Collections.unmodifiableList(List.of(".csv"));
	}

}
