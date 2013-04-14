package com.github.overengineer.scope.container;

import com.github.overengineer.scope.container.inject.CompositeInjector;
import com.github.overengineer.scope.container.instantiate.Instantiator;

import java.util.List;

/**
 */
public class SingletonComponentStrategy<T> implements ComponentStrategy<T> {

    private T component;
    private ComponentStrategy<T> delegateStrategy;
    private List<ComponentInitializationListener> initializationListeners;

    public SingletonComponentStrategy(CompositeInjector<T> injector, Instantiator<T> instantiator, List<ComponentInitializationListener> initializationListeners) {
        this.initializationListeners = initializationListeners;
        delegateStrategy = new PrototypeComponentStrategy<T>(injector, instantiator, initializationListeners);
    }

    @Override
    public T get(Provider provider) {
         if (component == null) {
             component = delegateStrategy.get(provider);
         }
        return component;
    }

}