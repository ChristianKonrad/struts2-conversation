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
 *  $Id: SimpleConversationManager.java reesbyars $
 ******************************************************************************/
package com.google.code.rees.scope.conversation.processing;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.rees.scope.conversation.ConversationAdapter;
import com.google.code.rees.scope.conversation.ConversationUtil;
import com.google.code.rees.scope.conversation.annotations.ConversationField;
import com.google.code.rees.scope.conversation.configuration.ConversationConfiguration;
import com.google.code.rees.scope.conversation.configuration.ConversationConfigurationProvider;
import com.google.code.rees.scope.conversation.context.ConversationContextAware;

/**
 * A simple yet effective implementation of {@link ConversationManager} that
 * manages conversation life cycles, but does not inject
 * {@link ConversationField ConversationFields}. Ideal for using in cases such
 * as when field injection will be handled by Spring.
 * 
 * @author rees.byars
 */
public class SimpleConversationManager implements ConversationManager {

	private static final long serialVersionUID = -518452439785782433L;
	private static final Logger LOG = LoggerFactory.getLogger(SimpleConversationManager.class);
	
	protected ConversationConfigurationProvider configurationProvider;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setConfigurationProvider(ConversationConfigurationProvider configurationProvider) {
		this.configurationProvider = configurationProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processConversations(ConversationAdapter conversationAdapter) {
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Beginning processing of conversations...");
            LOG.debug("Conversation Request Context:  " + conversationAdapter.getRequestContext());
        }
		
		Object action = conversationAdapter.getAction();
		
		Collection<ConversationConfiguration> actionConversationConfigs = this.configurationProvider.getConfigurations(action.getClass());
		if (actionConversationConfigs != null) {
			for (ConversationConfiguration conversationConfig : actionConversationConfigs) {
				processConversation(conversationConfig, conversationAdapter, action);
			}
		}
		
		if (action instanceof ConversationContextAware) {
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("Action implements ConversationContextAware, setting composite ConversationContext on action class instance.");
			}
			
			ConversationContextAware contextAware = (ConversationContextAware) action;
			contextAware.setConversationContext(ConversationUtil.getContext(conversationAdapter));
		}
		
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("...processing of conversations complete.");
		}
	}

	protected void processConversation(ConversationConfiguration conversationConfig, ConversationAdapter conversationAdapter, Object action) {

		String actionId = conversationAdapter.getActionId();
		String conversationName = conversationConfig.getConversationName();
		String conversationId = (String) conversationAdapter.getRequestContext().get(conversationName);
		
		if (conversationId != null) {
			if (conversationConfig.containsAction(actionId)) {
				if (conversationConfig.isEndAction(actionId)) {
					conversationAdapter.addPostProcessor(new ConversationEndProcessor(), conversationConfig, conversationId);
				} else {
					conversationAdapter.getViewContext().put(conversationName, conversationId);
				}
			}
		} else if (conversationConfig.isBeginAction(actionId)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Beginning new " + conversationName + ".");
			}
			ConversationUtil.begin(conversationName, conversationAdapter);
		}
	}

}
