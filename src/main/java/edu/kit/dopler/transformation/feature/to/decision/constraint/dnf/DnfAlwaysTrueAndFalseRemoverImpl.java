package edu.kit.dopler.transformation.feature.to.decision.constraint.dnf;

import com.google.inject.Inject;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;
import edu.kit.dopler.transformation.exceptions.ConjunctionAlwaysFalseException;
import edu.kit.dopler.transformation.exceptions.ConjunctionAlwaysTrueException;
import edu.kit.dopler.transformation.exceptions.DnfAlwaysFalseException;
import edu.kit.dopler.transformation.exceptions.DnfAlwaysTrueException;
import edu.kit.dopler.transformation.util.FeatureFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementation of {@link DnfAlwaysTrueAndFalseRemover}. */
public class DnfAlwaysTrueAndFalseRemoverImpl implements DnfAlwaysTrueAndFalseRemover {

    private final FeatureFinder featureFinder;

    @Inject
    DnfAlwaysTrueAndFalseRemoverImpl(FeatureFinder featureFinder) {
        this.featureFinder = featureFinder;
    }

    @Override
    public List<List<Constraint>> removeAlwaysTruOrFalseConstraints(FeatureModel featureModel,
                                                                    List<List<Constraint>> dnf)
            throws DnfAlwaysFalseException, DnfAlwaysTrueException {
        List<List<Constraint>> newDnf = new ArrayList<>();

        //Create copy of DNF but remove unnecessary variables from the conjunctions
        for (List<Constraint> conjunction : dnf) {
            try {
                newDnf.add(createNewConjunction(featureModel, conjunction));
            } catch (ConjunctionAlwaysTrueException e) {
                throw new DnfAlwaysTrueException(e, dnf);
            } catch (ConjunctionAlwaysFalseException e) {
                //Don't add conjunction to the new DNF
            }
        }

        //If the DNF is empty, then the DNF is always false
        if (newDnf.isEmpty()) {
            throw new DnfAlwaysFalseException(dnf);
        }

        return newDnf;
    }

    private List<Constraint> createNewConjunction(FeatureModel featureModel, List<Constraint> conjunction)
            throws ConjunctionAlwaysTrueException, ConjunctionAlwaysFalseException {
        List<Constraint> newConjunction = new ArrayList<>();

        for (Constraint constraint : conjunction) {
            Optional<? extends Constraint> parent = getNonMandatoryParent(featureModel, constraint);

            if (constraint instanceof NotConstraint) {
                //If parent was not found, then conjunction is always false and is not added to the new DNF
                if (parent.isEmpty()) {
                    throw new ConjunctionAlwaysFalseException(conjunction);
                } else {
                    newConjunction.add(new NotConstraint(parent.get()));
                }
            } else if (constraint instanceof LiteralConstraint) {
                //If parent was not found, constraint is always true and does not need to be added to the conjunction
                parent.ifPresent(newConjunction::add);
            } else {
                newConjunction.add(constraint);
            }
        }

        //If the conjunction does not contain any variables, then the conjunction is always true
        if (newConjunction.isEmpty()) {
            throw new ConjunctionAlwaysTrueException(conjunction);
        }

        return newConjunction;
    }

    private Optional<? extends Constraint> getNonMandatoryParent(FeatureModel featureModel, Constraint constraint) {
        return switch (constraint) {
            case NotConstraint notConstraint when notConstraint.getContent() instanceof LiteralConstraint literalConstraint ->
                    featureFinder.findFirstNonMandatoryParent(featureModel, literalConstraint);
            case LiteralConstraint literalConstraint ->
                    featureFinder.findFirstNonMandatoryParent(featureModel, literalConstraint);
            case null, default -> Optional.ofNullable(constraint);
        };
    }
}
