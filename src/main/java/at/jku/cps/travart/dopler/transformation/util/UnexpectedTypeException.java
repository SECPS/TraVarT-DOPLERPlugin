package at.jku.cps.travart.dopler.transformation.util;

public class UnexpectedTypeException extends RuntimeException {

    private final Object object;

    public UnexpectedTypeException(Object object) {
        this.object = object;
    }

    @Override
    public String getMessage() {
        return "Unexpected type: " + object.getClass().getSimpleName();
    }
}
