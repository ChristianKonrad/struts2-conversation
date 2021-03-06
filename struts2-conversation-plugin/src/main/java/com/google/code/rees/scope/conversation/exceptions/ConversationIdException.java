/***********************************************************************************************************************
 *
 * Struts2-Conversation-Plugin - An Open Source Conversation- and Flow-Scope Solution for Struts2-based Applications
 * =================================================================================================================
 *
 * Copyright (C) 2012 by Rees Byars
 * http://code.google.com/p/struts2-conversation/
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 *
 * $Id: ConversationIdException.java Jun 5, 2012 8:53:11 PM reesbyars $
 *
 **********************************************************************************************************************/
package com.google.code.rees.scope.conversation.exceptions;


/**
 * @author rees.byars
 */
public class ConversationIdException extends ConversationException {

	private static final long serialVersionUID = -1729010468518612616L;
	
	private String conversationName;
	private String conversationId;

	/**
	 * @param message
	 */
	public ConversationIdException(String message, String conversationName, String conversationId) {
		super(message);
		this.conversationName = conversationName;
		this.conversationId = conversationId;
	}
	
	public String getConversationName() {
		return this.conversationName;
	}
	
	public String getConversationId() {
		return this.conversationId;
	}

}
