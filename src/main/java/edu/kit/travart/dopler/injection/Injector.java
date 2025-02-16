/*******************************************************************************
 * SPDX-License-Identifier: MPL-2.0
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 * <p>
 * Contributors:
 *    @author Yannick Kraml
 *    @author Kevin Feichtinger
 * <p>
 * Copyright 2024 Karlsruhe Institute of Technology (KIT)
 * KASTEL - Dependability of Software-intensive Systems
 *******************************************************************************/
package edu.kit.travart.dopler.injection;

import edu.kit.travart.dopler.plugin.DoplerDeserializer;
import edu.kit.travart.dopler.plugin.DoplerPrettyPrinter;
import edu.kit.travart.dopler.plugin.DoplerSerializer;
import edu.kit.travart.dopler.sampling.ConfigVerifier;
import edu.kit.travart.dopler.sampling.ConfigVerifierImpl;
import edu.kit.travart.dopler.sampling.DoplerSampler;
import edu.kit.travart.dopler.sampling.InvalidConfigFinder;
import edu.kit.travart.dopler.sampling.InvalidConfigFinderImpl;
import edu.kit.travart.dopler.sampling.ValidConfigFinder;
import edu.kit.travart.dopler.sampling.ValidConfigFinderImpl;
import edu.kit.travart.dopler.sampling.Z3OutputParser;
import edu.kit.travart.dopler.sampling.Z3OutputParserImpl;
import edu.kit.travart.dopler.sampling.Z3Runner;
import edu.kit.travart.dopler.sampling.Z3RunnerImpl;
import edu.kit.travart.dopler.transformation.Transformer;
import edu.kit.travart.dopler.transformation.decision.to.feature.AttributeCreator;
import edu.kit.travart.dopler.transformation.decision.to.feature.AttributeCreatorImpl;
import edu.kit.travart.dopler.transformation.decision.to.feature.DmToFmTransformer;
import edu.kit.travart.dopler.transformation.decision.to.feature.DmToFmTransformerImpl;
import edu.kit.travart.dopler.transformation.decision.to.feature.ParentFinder;
import edu.kit.travart.dopler.transformation.decision.to.feature.ParentFinderImpl;
import edu.kit.travart.dopler.transformation.decision.to.feature.TreeBeautifier;
import edu.kit.travart.dopler.transformation.decision.to.feature.TreeBeautifierImpl;
import edu.kit.travart.dopler.transformation.decision.to.feature.TreeBuilder;
import edu.kit.travart.dopler.transformation.decision.to.feature.TreeBuilderImpl;
import edu.kit.travart.dopler.transformation.decision.to.feature.rules.ActionHandler;
import edu.kit.travart.dopler.transformation.decision.to.feature.rules.ActionHandlerImpl;
import edu.kit.travart.dopler.transformation.decision.to.feature.rules.ConditionHandler;
import edu.kit.travart.dopler.transformation.decision.to.feature.rules.ConditionHandlerImpl;
import edu.kit.travart.dopler.transformation.decision.to.feature.rules.ExpressionHandler;
import edu.kit.travart.dopler.transformation.decision.to.feature.rules.ExpressionHandlerImpl;
import edu.kit.travart.dopler.transformation.decision.to.feature.rules.RuleHandler;
import edu.kit.travart.dopler.transformation.decision.to.feature.rules.RuleHandlerImpl;
import edu.kit.travart.dopler.transformation.feature.to.decision.AttributeHandler;
import edu.kit.travart.dopler.transformation.feature.to.decision.AttributeHandlerImpl;
import edu.kit.travart.dopler.transformation.feature.to.decision.FeatureAndGroupHandler;
import edu.kit.travart.dopler.transformation.feature.to.decision.FeatureAndGroupHandlerImpl;
import edu.kit.travart.dopler.transformation.feature.to.decision.FmToDmTransformer;
import edu.kit.travart.dopler.transformation.feature.to.decision.FmToDmTransformerImpl;
import edu.kit.travart.dopler.transformation.feature.to.decision.IdHandler;
import edu.kit.travart.dopler.transformation.feature.to.decision.IdHandlerImpl;
import edu.kit.travart.dopler.transformation.feature.to.decision.VisibilityHandler;
import edu.kit.travart.dopler.transformation.feature.to.decision.VisibilityHandlerImpl;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.ActionCreator;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.ActionCreatorImpl;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.ConditionCreator;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.ConditionCreatorImpl;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.ConstraintHandler;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.ConstraintHandlerImpl;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.DnfAlwaysTrueAndFalseRemover;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.DnfAlwaysTrueAndFalseRemoverImpl;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.DnfSimplifier;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.DnfSimplifierImpl;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.DnfToTreeConverter;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.DnfToTreeConverterImpl;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.TreeToDnfConverter;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.TreeToDnfConverterImpl;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.UnwantedConstraintsReplacer;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.UnwantedConstraintsReplacerImpl;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.rule.DistributiveLeftDnfRule;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.rule.DistributiveRightDnfRule;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.rule.MorgenAndDnfRule;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.rule.MorgenOrDnfRule;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.rule.NotNotDnfRule;
import edu.kit.travart.dopler.transformation.util.DecisionFinder;
import edu.kit.travart.dopler.transformation.util.DecisionFinderImpl;
import edu.kit.travart.dopler.transformation.util.FeatureFinder;
import edu.kit.travart.dopler.transformation.util.FeatureFinderImpl;

import java.util.List;

/** Implementation of {@link AbstractInjector} */
public final class Injector extends AbstractInjector {

    protected void installInstances() {
        installPlugin();
        installTransformation();
        installSampling();
    }

    private void installPlugin() {
        install(DoplerSerializer.class, new DoplerSerializer());
        install(DoplerDeserializer.class, new DoplerDeserializer());
        install(DoplerPrettyPrinter.class, new DoplerPrettyPrinter(getInstance(DoplerSerializer.class)));
    }

    private void installSampling() {
        install(Z3Runner.class, new Z3RunnerImpl());
        install(Z3OutputParser.class, new Z3OutputParserImpl());
        install(ConfigVerifier.class, new ConfigVerifierImpl(getInstance(Z3Runner.class)));
        install(ValidConfigFinder.class,
                new ValidConfigFinderImpl(getInstance(Z3Runner.class), getInstance(Z3OutputParser.class)));
        install(InvalidConfigFinder.class,
                new InvalidConfigFinderImpl(getInstance(ValidConfigFinder.class), getInstance(ConfigVerifier.class)));
        install(ConfigVerifier.class, new ConfigVerifierImpl(getInstance(Z3Runner.class)));
        install(DoplerSampler.class,
                new DoplerSampler(getInstance(ValidConfigFinder.class), getInstance(InvalidConfigFinder.class),
                        getInstance(ConfigVerifier.class)));
    }

    private void installTransformation() {
        install(FeatureFinder.class, new FeatureFinderImpl());
        install(DecisionFinder.class, new DecisionFinderImpl());

        installFmToDm();
        installDmToFm();

        install(Transformer.class,
                new Transformer(getInstance(DmToFmTransformer.class), getInstance(FmToDmTransformer.class)));
    }

    private void installDmToFm() {
        install(ParentFinder.class, new ParentFinderImpl(getInstance(FeatureFinder.class)));
        install(TreeBeautifier.class, new TreeBeautifierImpl(getInstance(FeatureFinder.class)));
        install(AttributeCreator.class, new AttributeCreatorImpl(getInstance(FeatureFinder.class)));
        install(TreeBuilder.class,
                new TreeBuilderImpl(getInstance(ParentFinder.class), getInstance(AttributeCreator.class)));
        install(ExpressionHandler.class, new ExpressionHandlerImpl());
        install(ConditionHandler.class, new ConditionHandlerImpl(getInstance(ExpressionHandler.class)));
        install(ActionHandler.class, new ActionHandlerImpl(getInstance(FeatureFinder.class)));
        install(RuleHandler.class,
                new RuleHandlerImpl(getInstance(ConditionHandler.class), getInstance(ActionHandler.class)));
        install(DmToFmTransformer.class,
                new DmToFmTransformerImpl(getInstance(TreeBuilder.class), getInstance(RuleHandler.class),
                        getInstance(TreeBeautifier.class)));
    }

    private void installFmToDm() {
        install(DnfSimplifier.class, new DnfSimplifierImpl());
        install(DnfToTreeConverter.class, new DnfToTreeConverterImpl());
        install(UnwantedConstraintsReplacer.class, new UnwantedConstraintsReplacerImpl());
        install(IdHandler.class, new IdHandlerImpl(getInstance(DecisionFinder.class)));
        install(ActionCreator.class, new ActionCreatorImpl(getInstance(DecisionFinder.class)));
        install(DnfAlwaysTrueAndFalseRemover.class,
                new DnfAlwaysTrueAndFalseRemoverImpl(getInstance(FeatureFinder.class)));
        install(ConditionCreator.class,
                new ConditionCreatorImpl(getInstance(DecisionFinder.class), getInstance(FeatureFinder.class)));
        install(VisibilityHandler.class,
                new VisibilityHandlerImpl(getInstance(FeatureFinder.class), getInstance(DecisionFinder.class)));
        install(AttributeHandler.class,
                new AttributeHandlerImpl(getInstance(ConditionCreator.class), getInstance(DecisionFinder.class)));
        install(FeatureAndGroupHandler.class,
                new FeatureAndGroupHandlerImpl(getInstance(VisibilityHandler.class), getInstance(IdHandler.class)));
        install(TreeToDnfConverter.class, new TreeToDnfConverterImpl(getInstance(UnwantedConstraintsReplacer.class),
                getInstance(DnfSimplifier.class),
                List.of(new DistributiveLeftDnfRule(), new DistributiveRightDnfRule(), new MorgenAndDnfRule(),
                        new MorgenOrDnfRule(), new NotNotDnfRule())));
        install(ConstraintHandler.class,
                new ConstraintHandlerImpl(getInstance(TreeToDnfConverter.class), getInstance(DnfToTreeConverter.class),
                        getInstance(ActionCreator.class), getInstance(ConditionCreator.class),
                        getInstance(DnfAlwaysTrueAndFalseRemover.class)));
        install(FmToDmTransformer.class, new FmToDmTransformerImpl(getInstance(FeatureAndGroupHandler.class),
                getInstance(ConstraintHandler.class), getInstance(AttributeHandler.class)));
    }
}
