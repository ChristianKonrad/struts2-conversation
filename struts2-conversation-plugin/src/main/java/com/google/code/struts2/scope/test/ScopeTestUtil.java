package com.google.code.struts2.scope.test;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.struts2.dispatcher.Dispatcher;
import org.springframework.mock.web.MockHttpServletRequest;

import com.google.code.struts2.scope.conversation.ConversationConfigBuilderImpl;
import com.google.code.struts2.scope.conversation.ConversationConstants;
import com.google.code.struts2.scope.conversation.ConversationManager;
import com.google.code.struts2.scope.sessionfield.SessionFieldUtil;
import com.opensymphony.xwork2.ActionContext;

public class ScopeTestUtil {
	
	private static ConversationManager manager;
	
	protected static ConversationManager getconversationManager() {
		if (manager == null) {
			manager = Dispatcher.getInstance().getContainer().getInstance(ConversationManager.class, ConversationConstants.MANAGER_KEY);
		}
		return manager;
	}

	/**
	 * For unit testing, sets the conversation IDs of the conversations in the current thread
	 * onto a given mock request.
	 * 
	 * @param request
	 */
	public static void setConversationIdsOnRequest(MockHttpServletRequest request, Class<?> actionClass) {
		ActionContext actionContext = ActionContext.getContext();
		@SuppressWarnings("unchecked")
		Map<String, String> convoIdMap = ((Map<String, String>) actionContext.getValueStack()
				.findValue(ConversationConstants.CONVERSATION_ID_MAP_STACK_KEY));
		if (convoIdMap != null) {
			for (Entry<String, String> entry : convoIdMap.entrySet()) {
				request.addParameter(entry.getKey(), new String[]{entry.getValue()});
			}
		} else {
			for (String c : ConversationConfigBuilderImpl.getConversationNames(actionClass)) {
				request.addParameter(c + ConversationConstants.CONVERSATION_NAME_SESSION_MAP_SUFFIX, c + "-test-id");
			}
		}
	}
	
	/**
     * The current values of session and conversation fields that are annotated with 
     * {@link SessionField} and {@link ConversationField} 
     * are extracted from the target object and placed into the session
     * and the active conversations available in the current thread. 
     * 
     * @param target
     */
    public static void extractScopeFields(Object target) {
            SessionFieldUtil.extractSessionFields(target);
            getconversationManager().extractConversationFields(target);
    }
    
    /**
     * The target object's session and conversation fields that are annotated with 
     * {@link SessionField} and {@link ConversationField} are injected from the session
     * and the active conversations available in the current thread. 
     * 
     * @param target
     */
    public static void injectScopeFields(Object target) {
            SessionFieldUtil.injectSessionFields(target);
            getconversationManager().injectConversationFields(target);
    }

}
