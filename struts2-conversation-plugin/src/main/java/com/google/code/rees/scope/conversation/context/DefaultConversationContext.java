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
 *  $Id: DefaultConversationContext.java reesbyars $
 ******************************************************************************/
package com.google.code.rees.scope.conversation.context;

import com.google.code.rees.scope.util.monitor.HashMonitoredContext;

/**
 * The default implementation of the {@link ConversationContext}
 * 
 * @author rees.byars
 * 
 */
public class DefaultConversationContext extends HashMonitoredContext<String, Object, ConversationContext> implements ConversationContext {

	private static final long serialVersionUID = 2795735781863798576L;
	protected String conversationName;
	protected String id;

	public DefaultConversationContext(String conversationName, String id, long duration) {
		super(duration);
		this.id = id;
		this.conversationName = conversationName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getConversationName() {
		return this.conversationName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return this.id;
	}
	
	@Override
	public String toString() {
		return "[" + this.conversationName + ":" + this.id + "]" + super.toString();
	}

}
