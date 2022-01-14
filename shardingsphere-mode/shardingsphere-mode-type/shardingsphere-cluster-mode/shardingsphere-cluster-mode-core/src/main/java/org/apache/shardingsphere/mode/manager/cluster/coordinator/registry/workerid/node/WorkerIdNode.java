package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.node;

/**
 * Worker id node.
 */
public final class WorkerIdNode {
    
    private static final String ROOT_NODE = "worker_id";
    
    /**
     * Get worker id generator path.
     * 
     * @param instanceId instance id
     * @return worker id generator path
     */
    public static String getWorkerIdGeneratorPath(final String instanceId) {
        return String.join("/", "", ROOT_NODE, instanceId);
    }
}
