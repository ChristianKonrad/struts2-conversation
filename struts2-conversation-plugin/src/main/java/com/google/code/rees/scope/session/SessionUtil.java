/*******************************************************************************
 * 
 *  Struts2-Conversation-Plugin - An Open Source Conversation- and Flow-Scope Solution for Struts2-based Applications
 *  =================================================================================================================
 * 
 *  Copyright (C) 2012 by Rees Byars
 *  http://code.google.com/p/struts2-conversation/
 * 
 * **********************************************************************************************************************
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations under the License.
 * 
 * **********************************************************************************************************************
 * 
 *  $Id: SessionUtil.java reesbyars $
 ******************************************************************************/
package com.google.code.rees.scope.session;

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.code.rees.scope.struts2.ConversationInterceptor;
import com.google.code.rees.scope.util.ReflectionUtil;

/**
 * 
 * A utility class that provides static methods that are used internally and
 * for unit testing. Use outside of these contexts should come only as
 * a last resort.
 * 
 * @author rees.byars
 * 
 */
public class SessionUtil 
{

    static private final Logger LOG = LogManager.getLogger(SessionUtil.class);

    /**
     * Given the name of a session-scoped field and its class, this method
     * returns the appropriate key that is used to identify instances of the
     * field in the session-field map.
     * 
     * @param name
     * @param clazz
     * @return
     */
    public static String buildKey(String name, Class<?> clazz) {
        return clazz.getName() + "." + name;
    }

    /**
     * Given a session field's name and class, the value of the field is
     * returned from the session.
     * 
     * @param <T>
     * @param name
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getField(String name, Class<T> clazz) {
        String key = buildKey(name, clazz);
        SessionAdapter adapter = SessionAdapter.getAdapter();
        if (adapter == null)
            return null;
        Map<String, Object> sessionContext = adapter.getSessionContext();
        return (T) sessionContext.get(key);
    }

    /**
     * Given a session field's name and an instance, the value is set
     * for the field in the session.
     * 
     * @param fieldName
     * @param fieldValue
     */
    public static void setField(String name, Object sessionField) {
        String key = buildKey(name, sessionField.getClass());
        SessionAdapter adapter = SessionAdapter.getAdapter();
        if (adapter != null) {
            Map<String, Object> sessionContext = adapter.getSessionContext();
            sessionContext.put(key, sessionField);
        }
    }

    /**
     * The current values of session fields annotated with {@link SessionField}
     * are extracted from the target object and placed into the session. No
     * caching
     * is performed, for use only in unit testing.
     * 
     * @param target
     */
    public static void extractFields(Object target) {
        Class<?> clazz = target.getClass();
        for (Field field : ReflectionUtil.getFields(clazz)) {
            if (field.isAnnotationPresent(SessionField.class)) {
                String name = getSessionFieldName(field);
                ReflectionUtil.makeAccessible(field);
                try {
                    Object value = field.get(target);
                    if (value != null) {
                        setField(name, value);
                    }
                } catch (IllegalArgumentException e) {
                    LOG.info("Illegal Argument on session field " + field.getName());
                } catch (IllegalAccessException e) {
                    LOG.info("Illegal Access on session field " + field.getName());
                }
            }
        }
    }

    /**
     * The target object's session fields that are annotated with
     * {@link SessionField} are injected from the session. No caching
     * is performed, for use only in unit testing.
     * 
     * @param target
     */
    public static void injectFields(Object target) {
        for (Field field : ReflectionUtil.getFields(target.getClass())) {
            if (field.isAnnotationPresent(SessionField.class)) {
                Object value = getField(field.getName(), field.getType());
                ReflectionUtil.makeAccessible(field);
                try {
                    field.set(target, value);
                } catch (IllegalArgumentException e) {
                    LOG.info("Illegal Argument on session field " + field.getName());
                } catch (IllegalAccessException e) {
                    LOG.info("Illegal Access on session field " + field.getName());
                }
            }
        }
    }

    /**
     * Returns the name of the given field's SessionField
     * 
     * @param field
     * @return
     */
    protected static String getSessionFieldName(Field field) {
        String name = field.getAnnotation(SessionField.class).name();
        if (name.equals(SessionField.DEFAULT)) {
            name = field.getName();
        }
        return name;
    }

}
