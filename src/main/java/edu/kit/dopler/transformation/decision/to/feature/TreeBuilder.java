package edu.kit.dopler.transformation.decision.to.feature;

import com.google.inject.Inject;
import de.vill.model.Feature;
import de.vill.model.Group;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;
import edu.kit.dopler.transformation.util.TreeBeautifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.vill.model.Group.GroupType.ALTERNATIVE;
import static de.vill.model.Group.GroupType.OPTIONAL;
import static de.vill.model.Group.GroupType.OR;

class TreeBuilder {

    private final TreeBeautifier treeBeautifier;
    private final ParentFinder parentFinder;

    @Inject
    TreeBuilder(TreeBeautifier treeBeautifier, ParentFinder parentFinder) {
        this.treeBeautifier = treeBeautifier;
        this.parentFinder = parentFinder;
    }

    public Feature buildTree(Dopler decisionModel) {
        Feature rootFeature = new Feature("Root");

        List<BooleanDecision> booleanDecisions = new ArrayList<>();
        List<EnumerationDecision> enumerationDecisions = new ArrayList<>();
        List<NumberDecision> numberDecisions = new ArrayList<>();
        List<StringDecision> stringDecisions = new ArrayList<>();

        for (IDecision<?> decision : decisionModel.getDecisions()) {
            switch (decision) {
                case BooleanDecision booleanDecision -> booleanDecisions.add(booleanDecision);
                case EnumerationDecision enumerationDecision -> enumerationDecisions.add(enumerationDecision);
                case NumberDecision numberDecision -> numberDecisions.add(numberDecision);
                case StringDecision stringDecision -> stringDecisions.add(stringDecision);
                case null, default -> throw new UnexpectedTypeException(decision);
            }
        }

        Map<Feature, IExpression> booleanFeatures = buildBooleanFeatures(booleanDecisions);
        Map<Feature, IExpression> enumFeatures = buildEnumFeatures(enumerationDecisions);

        Map<Feature, IExpression> allFeatures = new LinkedHashMap<>();
        allFeatures.putAll(booleanFeatures);
        allFeatures.putAll(enumFeatures);

        for (Feature feature : booleanFeatures.keySet()) {
            Group optionalGroup = new Group(OPTIONAL);
            optionalGroup.getFeatures().add(feature);
            parentFinder.getParentFromVisibility(allFeatures, rootFeature, allFeatures.get(feature))
                    .addChildren(optionalGroup);
        }

        for (Feature feature : enumFeatures.keySet()) {
            if (feature.getChildren().size() != 1) {
                throw new RuntimeException("Feature should have exactly one child");
            }

            Group group = feature.getChildren().getFirst();
            parentFinder.getParentFromVisibility(allFeatures, rootFeature, allFeatures.get(feature)).addChildren(group);
        }

        treeBeautifier.beautify(rootFeature);
        return rootFeature;
    }

    private Map<Feature, IExpression> buildEnumFeatures(List<EnumerationDecision> enumerationDecisions) {
        Map<Feature, IExpression> features = new LinkedHashMap<>();

        for (EnumerationDecision decision : enumerationDecisions) {
            Feature feature = new Feature(decision.getDisplayId());

            Group group;
            if (1 == decision.getMaxCardinality()) {
                group = new Group(ALTERNATIVE);
            } else {
                group = new Group(OR);
            }

            for (EnumerationLiteral literal : decision.getEnumeration().getEnumerationLiterals()) {
                Feature enumFeature = new Feature(literal.getValue());
                group.getFeatures().add(enumFeature);
            }

            feature.addChildren(group);
            features.put(feature, decision.getVisibilityCondition());
        }

        return features;
    }

    private Map<Feature, IExpression> buildBooleanFeatures(List<BooleanDecision> booleanDecisions) {
        Map<Feature, IExpression> features = new LinkedHashMap<>();
        for (BooleanDecision decision : booleanDecisions) {
            features.put(new Feature(decision.getDisplayId()), decision.getVisibilityCondition());
        }
        return features;
    }
}
