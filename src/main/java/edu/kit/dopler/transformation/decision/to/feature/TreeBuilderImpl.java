package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import com.google.inject.Inject;
import de.vill.model.Feature;
import de.vill.model.FeatureType;
import de.vill.model.Group;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.vill.model.Group.GroupType.ALTERNATIVE;
import static de.vill.model.Group.GroupType.MANDATORY;
import static de.vill.model.Group.GroupType.OPTIONAL;
import static de.vill.model.Group.GroupType.OR;
import static edu.kit.dopler.transformation.Transformer.STANDARD_MODEL_NAME;

/** Implementation of {@link TreeBuilder} */
public class TreeBuilderImpl implements TreeBuilder {

    private final ParentFinderImpl parentFinder;

    @Inject
    TreeBuilderImpl(ParentFinderImpl parentFinder) {
        this.parentFinder = parentFinder;
    }

    @Override
    public Feature buildTree(Dopler decisionModel, IModelTransformer.STRATEGY level) {
        Feature rootFeature = new Feature(STANDARD_MODEL_NAME);

        //Group the decisions by their type
        List<BooleanDecision> booleanDecisions = new ArrayList<>();
        List<EnumerationDecision> enumerationDecisions = new ArrayList<>();
        List<IDecision<?>> numberDecisions = new ArrayList<>();
        List<IDecision<?>> stringDecisions = new ArrayList<>();
        for (IDecision<?> decision : decisionModel.getDecisions()) {
            switch (decision) {
                case BooleanDecision booleanDecision -> booleanDecisions.add(booleanDecision);
                case EnumerationDecision enumerationDecision -> enumerationDecisions.add(enumerationDecision);
                case NumberDecision numberDecision -> numberDecisions.add(numberDecision);
                case StringDecision stringDecision -> stringDecisions.add(stringDecision);
                case null, default -> throw new UnexpectedTypeException(decision);
            }
        }

        //Create features from the decisions
        Map<Feature, IExpression> booleanFeatures = buildBooleanFeatures(booleanDecisions);
        Map<Feature, IExpression> enumFeatures = buildEnumFeatures(enumerationDecisions);
        Map<Feature, IExpression> numberFeatures = buildTypeFeatures(numberDecisions, FeatureType.REAL);
        Map<Feature, IExpression> stringFeatures = buildTypeFeatures(stringDecisions, FeatureType.STRING);

        //The allFeatures map is used for finding features in the handle methods
        Map<Feature, IExpression> allFeatures = new LinkedHashMap<>();
        allFeatures.putAll(booleanFeatures);
        allFeatures.putAll(enumFeatures);
        allFeatures.putAll(numberFeatures);
        allFeatures.putAll(stringFeatures);

        //Link the features together (only top to bottom)
        handleBooleanFeatures(allFeatures, booleanFeatures, rootFeature);
        handleEnumFeatures(allFeatures, enumFeatures, rootFeature, level);
        handleTypeFeatures(allFeatures, numberFeatures, rootFeature);
        handleTypeFeatures(allFeatures, stringFeatures, rootFeature);

        //set links from bottom to top
        setBottomToTopLinks(rootFeature);

        return rootFeature;
    }

    private void setBottomToTopLinks(Feature rootFeature) {
        for (Group child : rootFeature.getChildren()) {
            child.setParentFeature(rootFeature);
            for (Feature feature : child.getFeatures()) {
                feature.setParentGroup(child);
                setBottomToTopLinks(feature);
            }
        }
    }

    private void handleTypeFeatures(Map<Feature, IExpression> allFeatures, Map<Feature, IExpression> typeFeatures,
                                    Feature rootFeature) {
        for (Map.Entry<Feature, IExpression> entry : typeFeatures.entrySet()) {
            Feature feature = entry.getKey();
            IExpression visibility = entry.getValue();

            Group child = new Group(MANDATORY);
            child.getFeatures().add(feature);
            Feature parent = parentFinder.getParentFromVisibility(allFeatures, rootFeature, visibility);
            parent.addChildren(child);
        }
    }

    private void handleEnumFeatures(Map<Feature, IExpression> allFeatures, Map<Feature, IExpression> enumFeatures,
                                    Feature rootFeature, IModelTransformer.STRATEGY level) {
        for (Map.Entry<Feature, IExpression> entry : enumFeatures.entrySet()) {
            Feature feature = entry.getKey();
            IExpression visibility = enumFeatures.get(feature);
            Feature parent = parentFinder.getParentFromVisibility(allFeatures, rootFeature, visibility);

            //Depending on the level insert a new mandatory feature to keep the name of the enum decision
            switch (level) {
                case ONE_WAY -> {
                    Group group = feature.getChildren().getFirst();
                    parent.addChildren(group);
                }
                case ROUNDTRIP -> {
                    Group mandatory = new Group(MANDATORY);
                    mandatory.getFeatures().add(feature);
                    parent.addChildren(mandatory);
                }
                default -> throw new IllegalStateException("Unexpected value: " + level);
            }
        }
    }

    private void handleBooleanFeatures(Map<Feature, IExpression> allFeatures, Map<Feature, IExpression> booleanFeatures,
                                       Feature rootFeature) {
        for (Feature feature : booleanFeatures.keySet()) {
            Group optionalGroup = new Group(OPTIONAL);
            optionalGroup.getFeatures().add(feature);
            Feature parent = parentFinder.getParentFromVisibility(allFeatures, rootFeature, allFeatures.get(feature));
            parent.addChildren(optionalGroup);
        }
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
        for (IDecision<?> decision : booleanDecisions) {
            Feature feature = new Feature(decision.getDisplayId());
            features.put(feature, decision.getVisibilityCondition());
        }
        return features;
    }

    private Map<Feature, IExpression> buildTypeFeatures(List<IDecision<?>> numberDecisions, FeatureType featureType) {
        Map<Feature, IExpression> features = new LinkedHashMap<>();
        for (IDecision<?> decision : numberDecisions) {
            Feature feature = new Feature(decision.getDisplayId());
            feature.setFeatureType(featureType);
            features.put(feature, decision.getVisibilityCondition());
        }
        return features;
    }
}
