package com.github.overengineer.scope.container;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ComponentInjector<T> extends BaseInjector<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentInjector.class);

    public ComponentInjector(Method setter, Class<?> type) {
        super(setter, type);
    }

    public void inject(T component, Provider container) {
        try {
            Object dependency = container.get(type);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Setting component of type [{}] on component of type [{}]", dependency.getClass().getName(), component.getClass().getName());
            }
            setter.invoke(component, dependency);
        } catch (Exception e) {
            throw new InjectionException("Could not set component of type [" + type.getName() + "] on component of type [" + component.getClass().getName() + "] using setter [" + setter.getName() + "]", e);
        }
    }

}
