package org.drools.workbench.common.services.rest.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AsyncJobDelegator {

    private boolean useThreadPool = false;

    @EJB
    private AsyncJobSingleton asyncJobSingleton;
   
    private final ExecutorService executorService;

    public AsyncJobDelegator() {
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");

            // Look up our data source
            String useThreadPoolProp = (String) envCtx.lookup("org.drools.wb.async.thread");
            if (useThreadPoolProp != null) {
                useThreadPool = Boolean.parseBoolean(useThreadPoolProp);
            }
        } catch (Exception e) {
            // do nothing
        }

        if (useThreadPool) {
            executorService = Executors.newSingleThreadExecutor();
        } else {
            executorService = null;
        }
    }

    void submitJob(DroolsWbRestJobCallable job) {
        if (useThreadPool) {
//            job.initCdiResources();
            executorService.submit(job);
        } else {
            asyncJobSingleton.submitJob(job);
        }
    }

}
