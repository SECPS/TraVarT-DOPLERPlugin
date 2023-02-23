package at.jku.cps.travart.dopler.decision.model;

import java.util.Collection;
import java.util.Set;

import at.jku.cps.travart.core.common.IConfigurable;
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.ADecision.DecisionType;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;

public interface IDecision<T> extends IConfigurable {

	String getId();

	void setId(String id);

	String getQuestion();

	void setQuestion(String question);

	String getDescription();

	void setDescription(String description);

	DecisionType getType();

	Range<T> getRange();

	ARangeValue<T> getRangeValue(T value);

	ARangeValue<T> getRangeValue(String value);

	void setRange(Range<T> range);

	IValue<T> getValue();

	void setValue(T value) throws RangeValueException;

	void executeRules();

	Set<Rule> getRules();

	void addRule(Rule rule);

	void addRules(Collection<Rule> rules);

	boolean removeRule(Rule rule);

	void setRules(Set<Rule> rules);

	ICondition getVisiblity();

	void setVisibility(ICondition visiblity);

	boolean isVisible();

	boolean isTaken();

	void setIsTaken(boolean isTaken);

	// is only supposed to reset Values of decisions, not removing rules from a
	// decision
	void reset() throws RangeValueException;
}
