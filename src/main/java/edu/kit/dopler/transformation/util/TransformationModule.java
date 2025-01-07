package edu.kit.dopler.transformation.util;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import edu.kit.dopler.transformation.decision.to.feature.*;
import edu.kit.dopler.transformation.decision.to.feature.rules.*;
import edu.kit.dopler.transformation.feature.to.decision.*;
import edu.kit.dopler.transformation.feature.to.decision.constraint.*;
import edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.*;
import edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.rule.*;

import java.util.List;

/**
 * This class is a Guice module and therefore responsible for dependency injection.
 */
public class TransformationModule extends AbstractModule {

    @Override
    protected void configure() {
        //Bind classes to interfaces
        configureFmToDm();
        configureDmToFm();
    }

    private void configureDmToFm() {
        bind(DmToFmTransformer.class).to(DmToFmTransformerImpl.class);
        bind(ParentFinder.class).to(ParentFinderImpl.class);
        bind(TreeBeautifier.class).to(TreeBeautifierImpl.class);
        bind(TreeBuilder.class).to(TreeBuilderImpl.class);
        bind(RuleHandler.class).to(RuleHandlerImpl.class);
        bind(LeftCreator.class).to(LeftCreatorImpl.class);
        bind(RightCreator.class).to(RightCreatorImpl.class);
        bind(AttributeCreator.class).to(AttributeCreatorImpl.class);
    }

    private void configureFmToDm() {
        bind(FmToDmTransformer.class).to(FmToDmTransformerImpl.class);
        bind(AttributeHandler.class).to(AttributeHandlerImpl.class);
        bind(FeatureAndGroupHandler.class).to(FeatureAndGroupHandlerImpl.class);
        bind(ConstraintHandler.class).to(ConstraintHandlerImpl.class);
        bind(ConditionCreator.class).to(ConditionCreatorImpl.class);
        bind(IdHandler.class).to(IdHandlerImpl.class);
        bind(VisibilityHandler.class).to(VisibilityHandlerImpl.class);
        bind(ActionCreator.class).to(ActionCreatorImpl.class);
        bind(DnfAlwaysTrueAndFalseRemover.class).to(DnfAlwaysTrueAndFalseRemoverImpl.class);
        bind(DnfToTreeConverter.class).to(DnfToTreeConverterImpl.class);
        bind(TreeToDnfConverter.class).to(TreeToDnfConverterImpl.class);
        bind(DnfSimplifier.class).to(DnfSimplifierImpl.class);
        bind(UnwantedConstraintsReplacer.class).to(UnwantedConstraintsReplacerImpl.class);
        bind(FeatureFinder.class).to(FeatureFinderImpl.class);
        bind(DecisionFinder.class).to(DecisionFinderImpl.class);

        //Bind dnf rules
        bind(new TypeLiteral<List<DnfRule>>() {
        }).toInstance(List.of(new DistributiveLeftDnfRule(), new DistributiveRightDnfRule(), new MorgenAndDnfRule(),
                new MorgenOrDnfRule(), new NotNotDnfRule()));
    }
}
