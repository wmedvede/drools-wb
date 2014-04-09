package org.drools.workbench.common.services.rest.async;

import java.util.concurrent.Callable;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JobCallable implements Callable<Void> {

    protected final static Logger logger = LoggerFactory.getLogger(JobCallable.class);
   
    private Object lockObj;
    
    public JobCallable(Object lockObj) { 
        this.lockObj = lockObj;
    }

    @Override
    public Void call() {
        try { 
            synchronized(lockObj) { 
                doJob();
            }
        } finally { 
            makeGarbageCollectionEasy();
        }
        return null;
    }
    
    protected abstract void doJob();
    protected abstract void makeGarbageCollectionEasy();
 
    public Object getLock() { 
        return lockObj;
    }
    
    public void setLock(Object lockObj) { 
        this.lockObj = lockObj;
    }
    
    private BeanManager getBeanManager() { 
        return BeanManagerProvider.getInstance().getBeanManager();
    }
    
    public <T> T resolveCdiBean(Class<T> type) { 
        logger.info( ". resolving CDI resource: " + type.getSimpleName());
        BeanManager beanManager = getBeanManager();
        if( beanManager == null ) { 
            logger.error( "A" );
            throw new IllegalStateException("No BeanManager available to resolve " + type.getSimpleName() + " bean!");
        } 
        final Bean<?> bean = beanManager.resolve(beanManager.getBeans(type));
        if (bean == null) {
            logger.error( "B" );
            throw new IllegalStateException("Unable to resolve " + type.getSimpleName() + " bean!");
        }
        CreationalContext<?> cc = beanManager.createCreationalContext(null);
        T beanInstance = (T) beanManager.getReference(bean, type, cc);
        if( beanInstance == null ) { 
            logger.error( "C" );
            throw new IllegalStateException("Unable to create reference to " + type.getSimpleName() + " bean!");
        }
        return beanInstance;
    }
    
}