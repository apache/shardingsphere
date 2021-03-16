package org.apache.shardingsphere.infra.optimize.planner;

import org.apache.calcite.rel.RelNode;

public interface Planner {
    
    RelNode getPhysicPlan(RelNode logicalPlan);
}
