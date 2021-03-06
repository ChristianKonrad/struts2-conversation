package com.google.code.rees.scope.struts2.config_browser;

import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.code.rees.scope.struts2.ActionUtil;
import com.opensymphony.xwork2.config.entities.ActionConfig;

public class ActionNamesAction extends org.apache.struts2.config_browser.ActionNamesAction {

    static private final long serialVersionUID = 2L;

    static private final Logger LOG = LogManager.getLogger(ActionNamesAction.class);

    
    @Override
    public Set<String> getActionNames() {
        String namespace = this.getNamespace();
        Set<String> actionNames = new TreeSet<String>(this.configHelper.getActionNames(this.getNamespace()));
        for (String actionName : actionNames) {
            if (ActionConfig.WILDCARD.equals(actionName)) {
                ActionConfig actionConfig = this.configHelper.getActionConfig(namespace, actionName);
                try {
                    Class<?> actionClass = Class.forName(actionConfig.getClassName());
                    actionNames.addAll(ActionUtil.getActions(actionClass));
                } catch (ClassNotFoundException e) {
                    LOG.error("Could not find action class while trying to obtain Wild Card action methods!");
                }
            }
        }
        return actionNames;
    }
    
}
