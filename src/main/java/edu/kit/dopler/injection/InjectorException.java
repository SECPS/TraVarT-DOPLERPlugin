package edu.kit.dopler.injection;

/** This exception will be thrown, if the injector could not find an instance of a given class. */
class InjectorException extends RuntimeException {

    private final Class<?> clazz;

    InjectorException(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String getMessage() {
        return "Could not find instance of class '%s'. Did you install it and is the order correct?".formatted(clazz);
    }
}
