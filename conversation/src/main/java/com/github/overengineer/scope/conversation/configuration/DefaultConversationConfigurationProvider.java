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
 *  $Id: DefaultConversationConfigurationProvider.java reesbyars $
 ******************************************************************************/
package com.github.overengineer.scope.conversation.configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.overengineer.scope.bijection.BijectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.overengineer.scope.ActionProvider;
import com.github.overengineer.container.metadata.Component;
import com.github.overengineer.container.metadata.PostConstructable;
import com.github.overengineer.container.metadata.Property;
import com.github.overengineer.scope.conversation.ConversationConstants.Properties;
import com.github.overengineer.scope.conversation.annotations.BeginConversation;
import com.github.overengineer.scope.conversation.annotations.EndConversation;

/**
 * The default implementation of {@link ConversationConfigurationProvider}
 * <p/>
 * TODO add default transacitonal and accessibleFromView settings, add new config settings to the log messages, add to config browser
 *
 * @author rees.byars
 */
public class DefaultConversationConfigurationProvider implements ConversationConfigurationProvider, PostConstructable {

    private static final long serialVersionUID = -1227350994518195549L;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConversationConfigurationProvider.class);

    protected ConversationArbitrator arbitrator;
    protected ActionProvider actionProvider;
    protected BijectorFactory bijectorFactory;
    protected ConcurrentMap<Class<?>, Collection<ConversationClassConfiguration>> classConfigurations = new ConcurrentHashMap<Class<?>, Collection<ConversationClassConfiguration>>();
    protected long maxIdleTimeMillis;
    protected int maxInstances;

    @Property(Properties.CONVERSATION_IDLE_TIMEOUT)
    public void setDefaultMaxIdleTime(long maxIdleTimeMillis) {
        this.maxIdleTimeMillis = maxIdleTimeMillis;
    }

    @Property(Properties.CONVERSATION_MAX_INSTANCES)
    public void setDefaultMaxInstances(int maxInstances) {
        this.maxInstances = maxInstances;
    }

    @Component
    public void setArbitrator(ConversationArbitrator arbitrator) {
        this.arbitrator = arbitrator;
    }

    @Component
    public void setActionProvider(ActionProvider actionProvider) {
        this.actionProvider = actionProvider;
    }

    @Component
    public void setBijectorFactory(BijectorFactory bijectorFactory) {
        this.bijectorFactory = bijectorFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        if (this.classConfigurations.size() != actionProvider.getActionClasses().size()) { //in case it's already been called
            LOG.info("Building Conversation Configurations...");
            for (Class<?> clazz : actionProvider.getActionClasses()) {
                processClass(clazz, classConfigurations);
            }
            LOG.info("...building of Conversation Configurations successfully completed.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ConversationClassConfiguration> getConfigurations(Class<?> clazz) {
        Collection<ConversationClassConfiguration> configurations = classConfigurations.get(clazz);
        if (configurations == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No cached ConversationClassConfiguration found for class [{}] ", clazz.getName());
            }
            configurations = this.processClass(clazz, classConfigurations);
        }
        return configurations;
    }

    /**
     * good candidate for refactoring... but it works!
     *
     * @param clazz
     * @param classConfigurations
     * @return
     */
    protected Collection<ConversationClassConfiguration> processClass(Class<?> clazz, ConcurrentMap<Class<?>, Collection<ConversationClassConfiguration>> classConfigurations) {
        Collection<ConversationClassConfiguration> configurations = classConfigurations.get(clazz);
        if (configurations == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Building ConversationClassConfiguration for class [{}]", clazz.getName());
            }
            configurations = new HashSet<ConversationClassConfiguration>();
            Map<String, ConversationClassConfiguration> temporaryConversationMap = new HashMap<String, ConversationClassConfiguration>();

            for (Field field : this.arbitrator.getCandidateConversationFields(clazz)) {
                Collection<String> fieldConversations = this.arbitrator.getConversations(clazz, field);
                if (fieldConversations != null) {
                    String fieldName = this.arbitrator.getName(field);
                    for (String conversation : fieldConversations) {
                        ConversationClassConfiguration configuration = temporaryConversationMap.get(conversation);
                        if (configuration == null) {
                            configuration = new ConversationClassConfigurationImpl(conversation);
                            temporaryConversationMap.put(conversation, configuration);
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Adding field [{}] as member of conversation [{}]", fieldName, conversation);
                        }
                        configuration.addBijector(bijectorFactory.create(fieldName, field));
                    }
                }
            }

            // TODO refactor into multiple methods to make more beautimous instead of atrocious
            for (Method method : this.arbitrator.getCandidateConversationMethods(clazz)) {

                String methodName = this.arbitrator.getName(method);

                //intermediate action methods
                Collection<String> methodConversations = this.arbitrator.getConversations(clazz, method);
                if (methodConversations != null) {
                    for (String conversation : methodConversations) {
                        ConversationClassConfiguration configuration = temporaryConversationMap.get(conversation);
                        if (configuration == null) {
                            configuration = new ConversationClassConfigurationImpl(conversation);
                            temporaryConversationMap.put(conversation, configuration);
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Adding method [{}] as an Action for conversation [{}]", methodName, conversation);
                        }
                        configuration.addAction(methodName);
                    }
                }

                //begin action methods
                Collection<String> methodBeginConversations = this.arbitrator.getBeginConversations(clazz, method);
                if (methodBeginConversations != null) {
                    for (String conversation : methodBeginConversations) {
                        ConversationClassConfiguration configuration = temporaryConversationMap.get(conversation);
                        if (configuration == null) {
                            configuration = new ConversationClassConfigurationImpl(conversation);
                            temporaryConversationMap.put(conversation, configuration);
                        }

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Adding method [{}] as a Begin Action for conversation [{}]", methodName, conversation);
                        }
                        if (method.isAnnotationPresent(BeginConversation.class)) {
                            BeginConversation config = method.getAnnotation(BeginConversation.class);
                            long maxIdle = config.maxIdleTimeMillis();
                            if (maxIdle == -1L) {
                                maxIdle = this.maxIdleTimeMillis;
                            }
                            int maxInstance = config.maxInstances();
                            if (maxInstance == 0) {
                                maxInstance = this.maxInstances;
                            }
                            configuration.addBeginAction(methodName, maxIdle, config.maxIdleTime(), maxInstance, config.transactional());
                        } else {
                            configuration.addBeginAction(methodName, this.maxIdleTimeMillis, "", this.maxInstances, false);
                        }

                    }
                }

                //end action methods
                Collection<String> methodEndConversations = this.arbitrator.getEndConversations(clazz, method);
                if (methodEndConversations != null) {
                    for (String conversation : methodEndConversations) {
                        ConversationClassConfiguration configuration = temporaryConversationMap.get(conversation);
                        if (configuration == null) {
                            configuration = new ConversationClassConfigurationImpl(conversation);
                            temporaryConversationMap.put(conversation, configuration);
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Adding method [{}] as an End Action for Conversation [{}]", methodName, conversation);
                        }
                        if (method.isAnnotationPresent(EndConversation.class)) {
                            EndConversation config = method.getAnnotation(EndConversation.class);
                            configuration.addEndAction(methodName, config.accessibleFromView());
                        } else {
                            configuration.addEndAction(methodName, false);
                        }
                    }
                }
            }

            configurations.addAll(temporaryConversationMap.values());
            classConfigurations.putIfAbsent(clazz, configurations);
        }

        return configurations;
    }

}
