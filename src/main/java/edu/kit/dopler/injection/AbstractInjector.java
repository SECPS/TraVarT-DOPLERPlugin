package edu.kit.dopler.injection;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for dependency injection. So if you want an instance of a class, then you can get it here.
 */
public abstract class AbstractInjector {

    private final Map<Class<?>, Object> instanceMap;

    AbstractInjector() {
        instanceMap = new HashMap<>();
        installInstances();
    }

    /**
     * Installs single instance to the given class.
     *
     * @param clazz    Class to which the instance is installed
     * @param instance Instance to install
     * @param <K>      Type of class and instance
     */
    protected <K> void install(Class<K> clazz, K instance) {
        instanceMap.put(clazz, instance);
    }

    /**
     * Returns an instance of the given class. The instance must be installed in {@link Injector}.
     *
     * @param clazz Class from which an instance is wanted
     * @param <K>   Type of class
     *
     * @return Instance of the class
     */
    public <K> K getInstance(Class<K> clazz) {
        K instance = (K) instanceMap.get(clazz);
        if (null == instance) {
            throw new InjectorException(clazz);
        }
        return instance;
    }

    /**
     * Install all instances here. This method is called, when the class is initialised. Order is important!
     */
    protected abstract void installInstances();
}
