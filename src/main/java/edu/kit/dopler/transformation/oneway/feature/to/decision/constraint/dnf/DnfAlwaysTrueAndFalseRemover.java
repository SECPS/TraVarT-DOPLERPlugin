package edu.kit.dopler.transformation.oneway.feature.to.decision.constraint.dnf;

import edu.kit.dopler.transformation.util.DnfAlwaysFalseException;
import edu.kit.dopler.transformation.util.DnfAlwaysTrueException;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;

import java.util.List;

public interface DnfAlwaysTrueAndFalseRemover {

    List<List<Constraint>> removeAlwaysTruOrFalseConstraints(FeatureModel featureModel, List<List<Constraint>> dnf)
            throws DnfAlwaysFalseException, DnfAlwaysTrueException;
}
