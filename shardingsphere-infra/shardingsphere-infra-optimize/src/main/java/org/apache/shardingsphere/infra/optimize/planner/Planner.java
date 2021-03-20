package org.apache.shardingsphere.infra.optimize.planner;

import org.apache.calcite.rel.RelNode;

public interface Planner {
    
    /**
     * Get Physical plan from logical plan.
     * @param logicalPlan logical plan
     * @return Physical plan
     */
    RelNode getPhysicPlan(RelNode logicalPlan);
}
