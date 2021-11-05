package org.apache.shardingsphere.infra.rule.identifier.type;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

/**
 * cron contained rule.
 */
public interface CronContainedRule extends ShardingSphereRule {
    
    /**
     * Update configuration to job.
     *
     */
    void updateCornToJob();
}
