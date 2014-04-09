package org.drools.workbench.common.services.rest.async;

import java.util.Map;

import org.drools.workbench.common.services.rest.JobRequestApprovalService;
import org.drools.workbench.common.services.rest.JobRequestHelper;
import org.kie.workbench.common.services.shared.rest.JobRequest;
import org.kie.workbench.common.services.shared.rest.JobResult;
import org.kie.workbench.common.services.shared.rest.JobStatus;

public abstract class DroolsWbRestJobCallable extends JobCallable { 

    public abstract void doJob();
    
    private Map<String, JobResult> jobs;
    private JobRequest request;
    
    private JobRequestHelper jobRequestHelper;
    private JobRequestApprovalService approvalService;
   
    public DroolsWbRestJobCallable(Map<String, JobResult> jobs, JobRequest request) { 
        super(jobs);
        this.jobs = jobs;
        this.request = request;
    }
    
    protected void makeGarbageCollectionEasy() {
        this.jobs = null;
        this.request = null;
        this.jobRequestHelper = null;
        this.approvalService = null;
    }
   
    public void initCdiResources() { 
        logger.info( ". Init CDI Resources");
        getRequestHelper();
        getApprovalService();
    }
    
    public JobRequestHelper getRequestHelper() { 
        if( jobRequestHelper == null ) { 
            jobRequestHelper = resolveCdiBean(JobRequestHelper.class);
        }
        return jobRequestHelper;
    }

    public JobRequestApprovalService getApprovalService() { 
        if( approvalService == null ) { 
            approvalService = resolveCdiBean(JobRequestApprovalService.class);
        }
        return approvalService;
    }
    
    public boolean approveRequest() {
        final JobResult result = getApprovalService().requestApproval( request );
        boolean approved = result.getStatus().equals( JobStatus.APPROVED );
        if( approved ) { 
           addApprovalStatusJobResult(JobStatus.APPROVED); 
        } else { 
           addApprovalStatusJobResult(JobStatus.DENIED); 
        }
        return approved;
    }
    
    public void addApprovalStatusJobResult(JobStatus status) { 
        JobResult result = new JobResult();
        result.setJobId(request.getJobId());
        result.setStatus(status);
        StringBuffer resultString = new StringBuffer("The " + request.getClass().getSimpleName() + " "); 
        switch(status) { 
        case ACCEPTED:
            resultString.append("has been accepted and is being approved.");
            break;
        case APPROVED:
            resultString.append("has been approved and is being processed.");
            break;
        case DENIED:
            resultString.append("has been denied. The request will NOT be processed.");
            break;
        default:
            throw new IllegalStateException("Unknown status type: " + status );
        } 
        result.setResult(resultString.toString());
        result.setLastModified( System.currentTimeMillis() );
        jobs.put(result.getJobId(), result);
    }
   
}
