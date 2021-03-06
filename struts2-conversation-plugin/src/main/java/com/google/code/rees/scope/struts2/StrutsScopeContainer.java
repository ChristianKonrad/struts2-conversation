package com.google.code.rees.scope.struts2;

import com.google.code.rees.scope.container.AbstractScopeContainer;
import com.google.code.rees.scope.container.ScopeContainer;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;

public class StrutsScopeContainer extends AbstractScopeContainer {

private static final long serialVersionUID = -6820777796732236492L;
	
	private Container container;
	
	@Inject
	public void setContainer(Container container) {
		this.container = container;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T getPropertyFromPrimaryContainer(Class<T> clazz, String name) {
		String string = container.getInstance(String.class, name);
		if (clazz == long.class) {
			return (T) Long.valueOf(string);
		} else if (clazz == int.class) {
			return (T) Integer.valueOf(string);
		}
		return (T) string;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T getComponentFromPrimaryContainer(Class<T> clazz) {
		if (ScopeContainer.class.isAssignableFrom(clazz)) {
			return (T) this;
		} else {
			String typeKey = container.getInstance(String.class, clazz.getName());
			if (typeKey == null) {
				return container.getInstance(clazz);
			}
			return container.getInstance(clazz, typeKey);
		}
	}

}
