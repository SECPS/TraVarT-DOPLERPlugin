package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Feature;
import de.vill.model.FeatureType;
import de.vill.model.Group;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.Transformer;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.*;
import java.util.regex.Pattern;

import static de.vill.model.Group.GroupType.ALTERNATIVE;
import static de.vill.model.Group.GroupType.MANDATORY;
import static de.vill.model.Group.GroupType.OPTIONAL;
import static de.vill.model.Group.GroupType.OR;

/** Implementation of {@link TreeBuilder} */
public class TreeBuilderImpl implements TreeBuilder {

    static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(".+#.+#Attribute");
    private final ParentFinder parentFinder;
    private final AttributeCreator attributeHandler;

    public TreeBuilderImpl(ParentFinder parentFinder, AttributeCreator attributeHandler) {
        this.parentFinder = parentFinder;
        this.attributeHandler = attributeHandler;
    }

    private static void distributeDecisions(List<IDecision<?>> allDecisions, List<BooleanDecision> booleanDecisions,
                                            List<EnumerationDecision> enumerationDecisions,
                                            List<IDecision<?>> numberDecisions, List<IDecision<?>> stringDecisions) {
        for (IDecision<?> decision : allDecisions) {
            switch (decision) {
                case BooleanDecision booleanDecision -> booleanDecisions.add(booleanDecision);
                case EnumerationDecision enumerationDecision -> enumerationDecisions.add(enumerationDecision);
                case NumberDecision numberDecision -> numberDecisions.add(numberDecision);
                case StringDecision stringDecision -> stringDecisions.add(stringDecision);
                case null, default -> throw new UnexpectedTypeException(decision);
            }
        }
    }

    @Override
    public Feature buildTree(List<IDecision<?>> allDecisions, List<IAction> allActions,
                             IModelTransformer.STRATEGY strategy) {
        Feature rootFeature = new Feature(Transformer.STANDARD_MODEL_NAME);

        List<IDecision<?>> attributeDecisions = filterAttributeDecisions(allDecisions);

        //Group the decisions by their type
        List<BooleanDecision> booleanDecisions = new ArrayList<>();
        List<EnumerationDecision> enumerationDecisions = new ArrayList<>();
        List<IDecision<?>> numberDecisions = new ArrayList<>();
        List<IDecision<?>> stringDecisions = new ArrayList<>();
        distributeDecisions(allDecisions, booleanDecisions, enumerationDecisions, numberDecisions, stringDecisions);

        //Create features from the decisions
        Map<Feature, IExpression> booleanFeatures = createBooleanFeatures(booleanDecisions);
        Map<Feature, IExpression> enumFeatures = createEnumFeatures(enumerationDecisions);
        Map<Feature, IExpression> numberFeatures = createTypeFeatures(numberDecisions, FeatureType.REAL);
        Map<Feature, IExpression> stringFeatures = createTypeFeatures(stringDecisions, FeatureType.STRING);

        //The allFeatures map is used for finding features in the link methods
        Map<Feature, IExpression> allFeatures = new LinkedHashMap<>();
        allFeatures.putAll(booleanFeatures);
        allFeatures.putAll(enumFeatures);
        allFeatures.putAll(numberFeatures);
        allFeatures.putAll(stringFeatures);

        //Link the features together (only top to bottom)
        linkBooleanFeatures(allFeatures, booleanFeatures.keySet(), rootFeature);
        linkEnumFeatures(allFeatures, enumFeatures.keySet(), rootFeature, strategy);
        linkTypeFeatures(allFeatures, numberFeatures.keySet(), rootFeature);
        linkTypeFeatures(allFeatures, stringFeatures.keySet(), rootFeature);

        //set links from bottom to top
        setBottomToTopLinks(rootFeature);

        attributeHandler.handleAttributeDecisions(attributeDecisions, rootFeature, allActions);

        return rootFeature;
    }

    /** Filter all attribute decisions out. */
    private List<IDecision<?>> filterAttributeDecisions(List<IDecision<?>> allDecisions) {
        List<IDecision<?>> attributeDecisions = new ArrayList<>();
        for (IDecision<?> decision : new ArrayList<>(allDecisions)) {
            if (ATTRIBUTE_PATTERN.matcher(decision.getDisplayId()).matches()) {
                allDecisions.remove(decision);
                attributeDecisions.add(decision);
            }
        }
        return attributeDecisions;
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

    private void linkTypeFeatures(Map<Feature, IExpression> allFeatures, Set<Feature> typeFeatures,
                                  Feature rootFeature) {
        for (Feature feature : typeFeatures) {
            Group child = new Group(MANDATORY);
            child.getFeatures().add(feature);
            Feature parent =
                    parentFinder.getParentFromVisibility(allFeatures.keySet(), allFeatures.get(feature))
                            .orElse(rootFeature);
            parent.addChildren(child);
        }
    }

    private void linkEnumFeatures(Map<Feature, IExpression> allFeatures, Set<Feature> enumFeatures, Feature rootFeature,
                                  IModelTransformer.STRATEGY strategy) {
        for (Feature feature : enumFeatures) {
            Feature parent =
                    parentFinder.getParentFromVisibility(allFeatures.keySet(), allFeatures.get(feature))
                            .orElse(rootFeature);

            switch (strategy) {
                case ONE_WAY -> {
                    Group group = feature.getChildren().getFirst();
                    parent.addChildren(group);
                }
                case ROUNDTRIP -> {
                    //Insert a mandatory feature to keep the name of the enum decision
                    Group mandatory = new Group(MANDATORY);
                    mandatory.getFeatures().add(feature);
                    parent.addChildren(mandatory);
                }
                default -> throw new IllegalStateException("Unexpected value: " + strategy);
            }
        }
    }

    private void linkBooleanFeatures(Map<Feature, IExpression> allFeatures, Set<Feature> booleanFeatures,
                                     Feature rootFeature) {
        for (Feature feature : booleanFeatures) {
            Group optionalGroup = new Group(OPTIONAL);
            optionalGroup.getFeatures().add(feature);
            Feature parent =
                    parentFinder.getParentFromVisibility(allFeatures.keySet(), allFeatures.get(feature))
                            .orElse(rootFeature);
            parent.addChildren(optionalGroup);
        }
    }

    /**
     * For each {@link EnumerationDecision} a `ALTERNATIVE` or `OR` group is created, depending on the max cardinality.
     * The parent of the group has the name of the given decision. The children of the group are the enum values of the
     * decision.
     */
    private Map<Feature, IExpression> createEnumFeatures(List<EnumerationDecision> enumerationDecisions) {
        Map<Feature, IExpression> features = new LinkedHashMap<>();
        for (EnumerationDecision decision : enumerationDecisions) {
            Feature feature = new Feature(decision.getDisplayId());
            Group group = 1 == decision.getMaxCardinality() ? new Group(ALTERNATIVE) : new Group(OR);

            for (EnumerationLiteral literal : decision.getEnumeration().getEnumerationLiterals()) {
                group.getFeatures().add(new Feature(literal.getValue()));
            }

            feature.addChildren(group);
            features.put(feature, decision.getVisibilityCondition());
        }

        return features;
    }

    /** For each {@link BooleanDecision} a single {@link Feature} is created. */
    private Map<Feature, IExpression> createBooleanFeatures(List<BooleanDecision> booleanDecisions) {
        Map<Feature, IExpression> features = new LinkedHashMap<>();
        for (IDecision<?> decision : booleanDecisions) {
            Feature feature = new Feature(decision.getDisplayId());
            features.put(feature, decision.getVisibilityCondition());
        }
        return features;
    }

    /** For each given type-decision a single {@link Feature} with the given {@link FeatureType} is created. */
    private Map<Feature, IExpression> createTypeFeatures(List<IDecision<?>> typeDecisions, FeatureType featureType) {
        Map<Feature, IExpression> features = new LinkedHashMap<>();
        for (IDecision<?> decision : typeDecisions) {
            Feature feature = new Feature(decision.getDisplayId());
            feature.setFeatureType(featureType);
            features.put(feature, decision.getVisibilityCondition());
        }
        return features;
    }
}
