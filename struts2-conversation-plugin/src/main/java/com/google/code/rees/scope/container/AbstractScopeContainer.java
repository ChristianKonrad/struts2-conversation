package com.google.code.rees.scope.container;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.code.rees.scope.session.SessionUtil;
import com.google.code.rees.scope.util.ReflectionUtil;

public abstract class AbstractScopeContainer implements ScopeContainer {
	
	static private final long serialVersionUID = 201905082129L;
	
	static private final Logger LOG = LogManager.getLogger(SessionUtil.class);
	
	private Map<Class<?>, Object> components = new HashMap<Class<?>, Object>();

	@Override
	public <T> T getComponent(Class<T> clazz) {
		@SuppressWarnings("unchecked")
		T component = (T) components.get(clazz);
		if (component == null) {
			component = getComponentFromPrimaryContainer(clazz);
			synchronized(components) {
				if (components.get(clazz) == null) {
					inject(component);
					if (component instanceof PostConstructable) {
						((PostConstructable) component).init();
					}
					components.put(clazz, component);
				}
			}
		}
		return component;
	}
	
	@Override
	public <T> T getProperty(Class<T> clazz, String name) {
		return getPropertyFromPrimaryContainer(clazz, name);
	}
	
	protected void inject(Object component) {
		LOG.debug("Injecting dependencies into component of type [{}]", component.getClass().getName());
		for (Method method : component.getClass().getMethods()) {
			if(ReflectionUtil.isPublicSetter(method)) {
				Class<?> type = method.getParameterTypes()[0];
				try {
					if (ReflectionUtil.isPropertyType(type) && method.isAnnotationPresent(Property.class)) {
						Property property = method.getAnnotation(Property.class);
						Object value = getPropertyFromPrimaryContainer(type, property.value());
						LOG.info("Setting property [{}] on component of type [{}] with value [{}]", new Object[]{property.value(), component.getClass().getName(), value});
						method.invoke(component, new Object[]{value});
					} else if (method.isAnnotationPresent(Component.class)) {
						method.invoke(component, new Object[]{this.getComponent(type)});
					}
				} catch (Exception e) {
					if (type.getName().startsWith("com.github.overengineer")) {
						LOG.warn("Could not set component of type [{}] on component of type [{}] using setter [{}]", new Object[]{type.getName(), component.getClass(), method.getName(), e});
					} else {
						LOG.debug("Could not set component of type [{}] on component of type [{}] using setter [{}]", new Object[]{type.getName(), component.getClass(), method.getName()});
					}
				}
			}
		}
	}
	
	protected abstract <T> T getPropertyFromPrimaryContainer(Class<T> clazz, String name);
	
	protected abstract <T> T getComponentFromPrimaryContainer(Class<T> clazz);

}
