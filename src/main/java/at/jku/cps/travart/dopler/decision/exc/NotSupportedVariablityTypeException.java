package at.jku.cps.travart.dopler.decision.exc;

public class NotSupportedVariablityTypeException extends Exception {

	public NotSupportedVariablityTypeException(String message) {
		super(message);
	}
	
	public NotSupportedVariablityTypeException(Exception e) {
		super(e);
	}
}
