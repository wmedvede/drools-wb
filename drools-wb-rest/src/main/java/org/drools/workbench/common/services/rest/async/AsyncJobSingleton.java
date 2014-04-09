package org.drools.workbench.common.services.rest.async;

import javax.ejb.Asynchronous;
import javax.ejb.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This a Singleton *EJB*. The @ApplicationScoped annotation should *NOT* be
 * added here, because this EJB should only be used when the Java EE container
 * is available!
 */
@Singleton
public class AsyncJobSingleton {

    private final static Logger logger = LoggerFactory.getLogger(AsyncJobSingleton.class);
    
    @Asynchronous
    public void submitJob(DroolsWbRestJobCallable job) {
        // force similar jobs to be processed serially 
        synchronized( job.getLock() ) { 
            logger.info( "= JOB: >");
            try { 
                job.doJob();
            } finally { 
                logger.info( "= JOB: <");
            } 
        }
    }

}
