package org.apache.shardingsphere.infra.optimize.planner;

import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlExplainFormat;
import org.apache.calcite.sql.SqlExplainLevel;

@Slf4j
public class DefaultPlanner extends AbstractPlanner implements Planner {

    @Override
    public final RelNode getPhysicPlan(final RelNode logicalPlan) {
        RelOptCluster cluster = logicalPlan.getCluster();
        RelOptPlanner volcanoPlanner = cluster.getPlanner();
        assert volcanoPlanner instanceof VolcanoPlanner;
        assert ((VolcanoPlanner) volcanoPlanner).isLogical(logicalPlan);
        
        RelNode rewritedRelNode = rewrite(logicalPlan);
        
        RelNode physicalRelNode = optimize(rewritedRelNode);
        log("physical plan", physicalRelNode);
        return physicalRelNode;
        
    }
    
    private RelNode optimize(final RelNode rewritedRelNode) {
        RelOptPlanner volcanoPlanner = rewritedRelNode.getCluster().getPlanner();
        ShardingSphereConvention.INSTANCE.register(volcanoPlanner);
        RelNode root2 = changeTraits(volcanoPlanner, rewritedRelNode, ShardingSphereConvention.INSTANCE);
        volcanoPlanner.setRoot(root2);
        return volcanoPlanner.findBestExp();
    }

    private RelNode changeTraits(final RelOptPlanner volcanoPlanner, final RelNode logicalRelNode, final Convention convention) {
        return volcanoPlanner.changeTraits(logicalRelNode, logicalRelNode.getCluster().traitSetOf(convention));
    }
    
    @Override
    protected final void log(final String desc, final RelNode relNode) {
        if (log.isDebugEnabled()) {
            log.debug(RelOptUtil.dumpPlan(desc, relNode, SqlExplainFormat.TEXT, SqlExplainLevel.EXPPLAN_ATTRIBUTES));
        }
        
    }
}
