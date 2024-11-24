package edu.kit.dopler.transformation.feature.to.decision.constraint.dnf;

import edu.kit.dopler.transformation.exceptions.DnfAlwaysFalseException;
import edu.kit.dopler.transformation.exceptions.DnfAlwaysTrueException;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;

import java.util.List;

public interface DnfAlwaysTrueAndFalseRemover {

    List<List<Constraint>> removeAlwaysTruOrFalseConstraints(FeatureModel featureModel, List<List<Constraint>> dnf)
            throws DnfAlwaysFalseException, DnfAlwaysTrueException;
}
