package com.google.code.rees.scope.conversation.context;

import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebListener
public class SchedulerShutdownListener implements ServletContextListener 
{
	
    static private final Logger LOG = LogManager.getLogger(SchedulerShutdownListener.class);
	
    static private ScheduledExecutorService scheduler;
	
	public static void setScheduler(ScheduledExecutorService scheduler) {
		SchedulerShutdownListener.scheduler = scheduler;
	}
	
	public static ScheduledExecutorService getScheduler() {
		return SchedulerShutdownListener.scheduler;
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {	
		LOG.info("Initializing SchedulerShutdownListener");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		SchedulerShutdownListener.scheduler.shutdownNow();
		LOG.info("Scheduler has been shutdown");
	}

}
