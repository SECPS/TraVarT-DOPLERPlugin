package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import com.google.inject.Inject;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IAction;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.model.Rule;
import edu.kit.dopler.transformation.decision.to.feature.rules.RuleHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/** Implementation of {@link DmToFmTransformer}. */
public class DmToFmTransformerImpl implements DmToFmTransformer {

    private final TreeBuilder treeBuilder;
    private final RuleHandler ruleHandler;
    private final TreeBeautifier treeBeautifier;

    @Inject
    public DmToFmTransformerImpl(TreeBuilder treeBuilder, RuleHandler ruleHandler, TreeBeautifier treeBeautifier) {
        this.treeBuilder = treeBuilder;
        this.ruleHandler = ruleHandler;
        this.treeBeautifier = treeBeautifier;
    }

    @Override
    public FeatureModel transform(Dopler decisionModel, IModelTransformer.STRATEGY level) {
        // Create some list, so the original Dopler model is not edited, but the lists.
        // This is needed for attribute handling
        List<IDecision<?>> allDecisions = new ArrayList<>(decisionModel.getDecisions());
        List<Rule> allRules = allDecisions.stream().flatMap(decision -> decision.getRules().stream()).toList();
        List<IAction> allActions =
                allRules.stream().flatMap(rule -> rule.getActions().stream()).collect(Collectors.toList());

        Feature rootFeature = treeBuilder.buildTree(allDecisions, allActions, level);
        Feature newRoot = treeBeautifier.beautify(rootFeature, level);

        FeatureModel featureModel = new FeatureModel();
        featureModel.setRootFeature(newRoot);

        //Filter out rules, where actions were deleted. These are rules for attributes
        List<Rule> filteredRules =
                allRules.stream().filter(rule -> new HashSet<>(allActions).containsAll(rule.getActions())).toList();

        ruleHandler.handleRules(featureModel, filteredRules);

        return featureModel;
    }
}
