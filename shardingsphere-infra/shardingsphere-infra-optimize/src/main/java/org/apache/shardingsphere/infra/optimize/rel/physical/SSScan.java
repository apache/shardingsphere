package org.apache.shardingsphere.infra.optimize.rel.physical;

import lombok.Getter;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimize.rel.AbstractScan;
import org.apache.shardingsphere.infra.route.context.RouteContext;

public final class SSScan extends AbstractScan implements SSRel {
    
    @Getter
    private final RelNode pushdownRelNode;
    
    public SSScan(final RelOptCluster cluster, final RelTraitSet traitSet, final RelNode pushdownRelNode) {
        super(cluster, traitSet);
        this.traitSet = this.traitSet.replace(ShardingSphereConvention.INSTANCE);
        this.pushdownRelNode = pushdownRelNode;
    }
    
    @Override
    public RouteContext route() {
        return route(pushdownRelNode);
    }
    
    @Override
    protected RelDataType deriveRowType() {
        return pushdownRelNode.getRowType();
    }
    
    public static SSScan create(final RelOptCluster cluster, final RelTraitSet traitSet, final RelNode pushdownRelNode) {
        return new SSScan(cluster, traitSet, pushdownRelNode);
    }
}
