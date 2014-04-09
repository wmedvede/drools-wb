package org.drools.workbench.common.services.rest.async;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.workbench.common.services.shared.rest.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Unfortunately, the Resteasy asynchronous job mechanism has a number of bugs (most importantly, RESTEASY-682) which make it
 * unusable for our purposes.
 * </p>
 * Because tomcat compatibility is also important, we also can't use the EJB asynchronous mechanisms.
 * </p>
 * That leaves us with the java.util.concurrent.* executor framework. 
 */
@ApplicationScoped
public class JobRequestExecutor {

    @Inject
    private AsyncJobDelegator jobDelegator;
 
    private Map<String, JobResult> jobs;
    private int maxCacheSize = 10000;
    
    @PostConstruct
    public void start() {
        jobs = Collections.synchronizedMap( new Cache( maxCacheSize ) );
    }
   
    public JobResult removeJobResult(String jobId) { 
        return jobs.remove(jobId);
    }
   
    public JobResult getJobResult(String jobId) { 
        return jobs.get(jobId);
    }
    
    void submitJob(DroolsWbRestJobCallable job) { 
        job.addApprovalStatusJobResult(JobStatus.ACCEPTED);
        jobDelegator.submitJob(job);
    }

    public void createOrCloneRepository(final CreateOrCloneRepositoryRequest request) { 
        submitJob(new DroolsWbRestJobCallable(jobs, request) {
            @Override
            public void doJob() {
                if ( approveRequest() ) {
                    logger.info( "CreateOrCloneRepositoryRequest event approved. Performing requested operation." );
                    JobResult result = getRequestHelper().createOrCloneRepository(request.getJobId(), request.getRepository());
                    jobs.put(request.getJobId(), result);
                }
            }
        });
    }

    public void removeRepository(final RemoveRepositoryRequest request) { 
        submitJob(new DroolsWbRestJobCallable(jobs, request) {
            @Override
            public void doJob() {
                if ( approveRequest() ) {
                    logger.info( "RemoveRepositoryRequest event approved. Performing requested operation." );
                    JobResult result = getRequestHelper().removeRepository(request.getJobId(), request.getRepositoryName());
                    jobs.put(request.getJobId(), result);
                }
            }
        });
    }

    public void createProject(final CreateProjectRequest request) { 
        submitJob(new DroolsWbRestJobCallable(jobs, request) {
            @Override
            public void doJob() {
                if ( approveRequest() ) {
                    logger.info( "CreateProjectRequest event approved. Performing requested operation." );
                    JobResult result = getRequestHelper().createProject(request.getJobId(), request.getRepositoryName(), request.getProjectName());
                    jobs.put(request.getJobId(), result);
                }
            }
        });
    }

    public void compileProject(final CompileProjectRequest request) { 
        submitJob(new DroolsWbRestJobCallable(jobs, request) {
            @Override
            public void doJob() {
                if ( approveRequest() ) {
                    logger.info( "CompileProjectRequest event approved. Performing requested operation." );
                    JobResult result =getRequestHelper().compileProject(request.getJobId(), request.getRepositoryName(), request.getProjectName());
                    jobs.put(request.getJobId(), result);
                }
            }
        });
    }

    public void installProject(final InstallProjectRequest request) { 
        submitJob(new DroolsWbRestJobCallable(jobs, request) {
            @Override
            public void doJob() {
                if ( approveRequest() ) {
                    logger.info( "InstallProjectRequest event approved. Performing requested operation." );
                    JobResult result = getRequestHelper().installProject(request.getJobId(), request.getRepositoryName(), request.getProjectName());
                    jobs.put(request.getJobId(), result);
                }
            }
        });
    }

    public void testProject(final TestProjectRequest request) { 
        submitJob(new DroolsWbRestJobCallable(jobs, request) {
            @Override
            public void doJob() {
                if ( approveRequest() ) {
                    logger.info( "TestProjectRequest event approved. Performing requested operation." );
                    JobResult result = getRequestHelper().testProject(request.getJobId(), request.getRepositoryName(), request.getProjectName(), request.getBuildConfig());
                    jobs.put(request.getJobId(), result);
                }
            }
        });
    }

    public void deployProject(final DeployProjectRequest request) { 
        submitJob(new DroolsWbRestJobCallable(jobs, request) {
            @Override
            public void doJob() {
                if ( approveRequest() ) {
                    logger.info( "DeployProjectRequest event approved. Performing requested operation." );
                    JobResult result = getRequestHelper().deployProject(request.getJobId(), request.getRepositoryName(), request.getProjectName());
                    jobs.put(request.getJobId(), result);
                }
            }
        });
    }

    public void createOrganizationalUnit(final CreateOrganizationalUnitRequest request) { 
        submitJob(new DroolsWbRestJobCallable(jobs, request) {
            @Override
            public void doJob() {
                if ( approveRequest() ) {
                    logger.info( "CreateOrganizationalUnitRequest event approved. Performing requested operation." );
                    JobResult result = getRequestHelper().createOrganizationalUnit(request.getJobId(), request.getOrganizationalUnitName(), request.getOwner(), request.getRepositories());
                    jobs.put(request.getJobId(), result);
                }
            }
        });
    }

    public void addRepositoryToOrganizationalUnit(final AddRepositoryToOrganizationalUnitRequest request) {
        submitJob(new DroolsWbRestJobCallable(jobs, request) {
            @Override
            public void doJob() {
                if ( approveRequest() ) {
                    logger.info( "AddRepositoryToOrganizationalUnitRequest event approved. Performing requested operation." );
                    JobResult result = getRequestHelper().addRepositoryToOrganizationalUnit(request.getJobId(), request.getOrganizationalUnitName(), request.getRepositoryName());
                    jobs.put(request.getJobId(), result);
                }
            }
        });
    }

    public void removeRepositoryFromOrganizationalUnit(final RemoveRepositoryFromOrganizationalUnitRequest request) { 
        submitJob(new DroolsWbRestJobCallable(jobs, request) {
            @Override
            public void doJob() {
                if ( approveRequest() ) {
                    logger.info( "RemoveRepositoryFromOrganizationalUnitRequest event approved. Performing requested operation." );
                    JobResult result = getRequestHelper().removeRepositoryFromOrganizationalUnit(request.getJobId(), request.getOrganizationalUnitName(), request.getRepositoryName());
                    jobs.put(request.getJobId(), result);
                }
            }
        });
    }

    // helper classes/methods -----------------------------------------------------------------------------------------------------
    
    private static class Cache extends LinkedHashMap<String, JobResult> {
        
        private static final long serialVersionUID = 23L;
        private int maxSize = 1000;

        public Cache( int maxSize ) {
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry( Map.Entry<String, JobResult> stringFutureEntry ) {
            return size() > maxSize;
        }
    }
    
}
