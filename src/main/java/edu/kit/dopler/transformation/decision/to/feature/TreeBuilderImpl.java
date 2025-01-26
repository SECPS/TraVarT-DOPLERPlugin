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
package edu.kit.dopler.transformation.decision.to.feature;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Feature;
import de.vill.model.FeatureType;
import de.vill.model.Group;
import edu.kit.dopler.model.BooleanDecision;
import edu.kit.dopler.model.BooleanLiteralExpression;
import edu.kit.dopler.model.EnumerationDecision;
import edu.kit.dopler.model.EnumerationLiteral;
import edu.kit.dopler.model.IAction;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.model.IExpression;
import edu.kit.dopler.model.NumberDecision;
import edu.kit.dopler.model.StringDecision;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.vill.model.Group.GroupType.ALTERNATIVE;
import static de.vill.model.Group.GroupType.MANDATORY;
import static de.vill.model.Group.GroupType.OPTIONAL;
import static de.vill.model.Group.GroupType.OR;

/** Implementation of {@link TreeBuilder} */
public class TreeBuilderImpl implements TreeBuilder {

    static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(".+#.+#Attribute");
    private final ParentFinder parentFinder;
    private final AttributeCreator attributeHandler;

    /**
     * Constructor of {@link TreeBuilderImpl}
     *
     * @param parentFinder     {@link ParentFinderImpl}
     * @param attributeHandler {@link AttributeCreator}
     */
    public TreeBuilderImpl(ParentFinder parentFinder, AttributeCreator attributeHandler) {
        this.parentFinder = parentFinder;
        this.attributeHandler = attributeHandler;
    }

    @Override
    public Feature buildTree(List<IDecision<?>> allDecisions, List<IAction> allActions,
                             IModelTransformer.STRATEGY strategy) {
        Feature rootFeature = new Feature(TreeBeautifier.STANDARD_MODEL_NAME);

        List<IDecision<?>> attributeDecisions = filterAttributeDecisions(allDecisions);

        //Group the decisions by their type
        List<BooleanDecision> booleanDecisions = filterDecisions(allDecisions, BooleanDecision.class);
        List<EnumerationDecision> enumerationDecisions = filterDecisions(allDecisions, EnumerationDecision.class);
        List<NumberDecision> numberDecisions = filterDecisions(allDecisions, NumberDecision.class);
        List<StringDecision> stringDecisions = filterDecisions(allDecisions, StringDecision.class);

        //Create features from the decisions
        Map<Feature, IExpression> booleanFeatures = createBooleanFeatures(booleanDecisions);
        Map<Feature, IExpression> enumFeatures = createEnumFeatures(enumerationDecisions);
        Map<Feature, IExpression> numberFeatures = createNumberFeatures(numberDecisions);
        Map<Feature, IExpression> stringFeatures = createStringFeatures(stringDecisions);

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
            if (ATTRIBUTE_PATTERN.matcher(decision.getDisplayId()).matches() &&
                    decision.getVisibilityCondition() instanceof BooleanLiteralExpression booleanLiteralExpression &&
                    !booleanLiteralExpression.getLiteral()) {
                allDecisions.remove(decision);
                attributeDecisions.add(decision);
            }
        }
        return attributeDecisions;
    }

    /** Filter all decisions with the given class out. */
    private <K extends IDecision<?>> List<K> filterDecisions(List<IDecision<?>> allDecisions, Class<K> clazz) {
        List<K> filteredOut =
                allDecisions.stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
        allDecisions.removeAll(filteredOut);
        return filteredOut;
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
            Feature parent = parentFinder.getParentFromVisibility(allFeatures.keySet(), allFeatures.get(feature))
                    .orElse(rootFeature);
            parent.addChildren(child);
        }
    }

    private void linkEnumFeatures(Map<Feature, IExpression> allFeatures, Set<Feature> enumFeatures, Feature rootFeature,
                                  IModelTransformer.STRATEGY strategy) {
        for (Feature feature : enumFeatures) {
            Feature parent = parentFinder.getParentFromVisibility(allFeatures.keySet(), allFeatures.get(feature))
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
                default -> throw new UnexpectedTypeException(strategy);
            }
        }
    }

    private void linkBooleanFeatures(Map<Feature, IExpression> allFeatures, Set<Feature> booleanFeatures,
                                     Feature rootFeature) {
        for (Feature feature : booleanFeatures) {
            Group optionalGroup = new Group(OPTIONAL);
            optionalGroup.getFeatures().add(feature);
            Feature parent = parentFinder.getParentFromVisibility(allFeatures.keySet(), allFeatures.get(feature))
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

    /** For each given number-decision a single real {@link Feature} is created. */
    private Map<Feature, IExpression> createNumberFeatures(List<NumberDecision> numberDecisions) {
        Map<Feature, IExpression> features = new LinkedHashMap<>();
        for (IDecision<?> decision : numberDecisions) {
            Feature feature = new Feature(decision.getDisplayId());
            feature.setFeatureType(FeatureType.REAL);
            features.put(feature, decision.getVisibilityCondition());
        }
        return features;
    }

    /** For each given string-decision a single string {@link Feature} is created. */
    private Map<Feature, IExpression> createStringFeatures(List<StringDecision> stringDecisions) {
        Map<Feature, IExpression> features = new LinkedHashMap<>();
        for (IDecision<?> decision : stringDecisions) {
            Feature feature = new Feature(decision.getDisplayId());
            feature.setFeatureType(FeatureType.STRING);
            features.put(feature, decision.getVisibilityCondition());
        }
        return features;
    }
}
