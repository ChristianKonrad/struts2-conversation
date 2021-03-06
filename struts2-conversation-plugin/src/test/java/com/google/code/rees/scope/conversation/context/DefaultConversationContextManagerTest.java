package com.google.code.rees.scope.conversation.context;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Ignore;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.code.rees.scope.testutil.SerializationTestingUtil;
import com.google.code.rees.scope.testutil.thread.BasicTaskThread;
import com.google.code.rees.scope.testutil.thread.TaskThread;
import com.google.code.rees.scope.testutil.thread.ThreadTask;


public class DefaultConversationContextManagerTest {
	
	static final String TEST_NAME = "test";

    @Test
    public void testGetContext() {

    }

    @Test
    public void testSerialization() throws Exception {

        String testName = "test-conversation";
        String testId = "testId";
        DefaultConversationContextManager mgr = new DefaultConversationContextManager();
        mgr.setContextFactory(new DefaultConversationContextFactory());
        ConversationContext ctx = mgr.getContext(testName, testId);

        mgr = SerializationTestingUtil.getSerializedCopy(mgr);
        assertEquals(ctx, mgr.getContext(testName, testId));

    }
    
    /**
     * 
     * test takes a long time, run manually
     */
    @Ignore
    @Test
    public void testConcurrentModification() throws InterruptedException {
    	
    	ConversationContextManager manager = new DefaultConversationContextManager();
    	manager.setContextFactory(new DefaultConversationContextFactory());
    	manager.setMaxInstances(4);
    	
    	TaskThread contractionThread = BasicTaskThread.spawnInstance();
    	CollectionContractionTask collectionContractionTask = new CollectionContractionTask(manager);
    	contractionThread.addTask(collectionContractionTask);
    	
    	TaskThread expansionThread = BasicTaskThread.spawnInstance();
    	expansionThread.addTask(new CollectionExpansionTask(manager, collectionContractionTask));
    	
    	Thread.sleep(8000L);
    	
    	for (int i = 0; i < 200; i++) {
    		BasicTaskThread.spawnInstance().addTask(new CollectionExpansionTask(manager, collectionContractionTask));
    	}
    	
    	Thread.sleep(8000L);
    	
    	
    	
    }
    
    class CollectionExpansionTask implements ThreadTask {
    	
    	private final Logger LOG = LogManager.getLogger(CollectionExpansionTask.class);
    	
    	private ConversationContextManager manager;
    	private CollectionContractionTask removalTask;
    	
    	CollectionExpansionTask(ConversationContextManager manager, CollectionContractionTask removalTask) {
    		this.manager = manager;
    		this.removalTask = removalTask;
    	}
		
		@Override public boolean isActive() {return true;}
		@Override public void cancel() {}

		@Override
		public void doTask() {
			//this will create a new context
			LOG.info("Expanding...");
			String id = getNextId();
			this.manager.getContext(TEST_NAME, id);
			this.removalTask.addId(id);
			Thread.yield();
		}
    	
    }
    
    static long id = 1L;
	public static synchronized String getNextId() {
		return String.valueOf(id++);
	}
    
    class CollectionContractionTask implements ThreadTask {
    	
    	private final Logger LOG = LogManager.getLogger(CollectionContractionTask.class);
    	
    	private ConversationContextManager manager;
    	private Collection<String> ids = Collections.synchronizedSet(new HashSet<String>());
    	
    	CollectionContractionTask(ConversationContextManager manager) {
    		this.manager = manager;
    	}
		
		@Override public boolean isActive() {return true;}
		@Override public void cancel() {}

		@Override
		public void doTask() {
			synchronized (this.ids) {
				for (String id : this.ids) {
					LOG.info("Contracting...");
					manager.remove(TEST_NAME, id);
					Thread.yield();
				}
				this.ids.clear();
			}
			try {
				Thread.sleep(1L);
			} catch (InterruptedException e) {
				// meh
			}
		}
		
		public void addId(String id) {
			synchronized (this.ids) {
				this.ids.add(id);
			}
		}
    	
    }

}
