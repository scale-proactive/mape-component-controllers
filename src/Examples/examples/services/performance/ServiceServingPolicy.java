package examples.services.performance;

import java.util.List;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.multiactivity.compatibility.StatefulCompatibilityMap;
import org.objectweb.proactive.multiactivity.policy.ServingPolicy;

public class ServiceServingPolicy extends ServingPolicy {

    /**
     * Apply the policy on a request from the request queue.
     * 
     * @param requestIndexInRequestQueue
     *            index of the request in the request queue.
     * @param compatibility
     *            the compatibility map to perform compatibility checks, to
     *            retrieve to the request queue and thus removing requests from
     *            the request queue.
     * @param runnableRequests
     *            the requests that have been removed from the request queue
     *            should be put in this list to be passed to the executor for
     *            scheduling.
     * 
     * @return the index of the next request to check in the request queue. It
     *         should be equals to
     *         {@code requestIndexInRequestQueue - numberOfRequestsRemovedFromRequestQueue}
     *         .
     */
    public int runPolicyOnRequest(int requestIndex, StatefulCompatibilityMap compatibility,
            List<Request> runnableRequests) {
    	
    	List<Request> requestQueue = compatibility.getQueueContents();
    	Request request = requestQueue.get(requestIndex);

    	// ACCEPT EVERYTHING
    	//System.out.println(
    	//		"==== requestIndex: " + requestIndex + 
    	//		" of: " + requestQueue.size() +
    	//		" executing: " + compatibility.getNumberOfExecutingRequests());

        runnableRequests.add(request);
        compatibility.addRunning(request);

        requestQueue.remove(requestIndex);
        
               return --requestIndex;

    }
   
}
