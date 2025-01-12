package edu.kit.dopler.injection;

import edu.kit.dopler.plugin.DoplerDeserializer;
import edu.kit.dopler.plugin.DoplerPrettyPrinter;
import edu.kit.dopler.plugin.DoplerSerializer;
import edu.kit.dopler.transformation.Transformer;
import edu.kit.dopler.transformation.decision.to.feature.*;
import edu.kit.dopler.transformation.decision.to.feature.rules.*;
import edu.kit.dopler.transformation.feature.to.decision.*;
import edu.kit.dopler.transformation.feature.to.decision.constraint.*;
import edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.*;
import edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.rule.*;
import edu.kit.dopler.transformation.util.DecisionFinder;
import edu.kit.dopler.transformation.util.DecisionFinderImpl;
import edu.kit.dopler.transformation.util.FeatureFinder;
import edu.kit.dopler.transformation.util.FeatureFinderImpl;

import java.util.List;

/** Implementation of {@link AbstractInjector} */
public final class Injector extends AbstractInjector {

    protected void installInstances() {
        installPlugin();
        installTransformation();
    }

    private void installTransformation() {
        install(FeatureFinder.class, new FeatureFinderImpl());
        install(DecisionFinder.class, new DecisionFinderImpl());

        installFmToDm();
        installDmToFm();

        install(Transformer.class,
                new Transformer(getInstance(DmToFmTransformer.class), getInstance(FmToDmTransformer.class)));
    }

    private void installPlugin() {
        install(DoplerSerializer.class, new DoplerSerializer());
        install(DoplerDeserializer.class, new DoplerDeserializer());
        install(DoplerPrettyPrinter.class, new DoplerPrettyPrinter(getInstance(DoplerSerializer.class)));
    }

    private void installDmToFm() {
        install(ParentFinder.class, new ParentFinderImpl(getInstance(FeatureFinder.class)));
        install(TreeBeautifier.class, new TreeBeautifierImpl(getInstance(FeatureFinder.class)));
        install(AttributeCreator.class, new AttributeCreatorImpl(getInstance(FeatureFinder.class)));
        install(TreeBuilder.class,
                new TreeBuilderImpl(getInstance(ParentFinder.class), getInstance(AttributeCreator.class)));
        install(LeftCreator.class, new LeftCreatorImpl());
        install(RightCreator.class, new RightCreatorImpl(getInstance(FeatureFinder.class)));
        install(RuleHandler.class,
                new RuleHandlerImpl(getInstance(LeftCreator.class), getInstance(RightCreator.class)));
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
