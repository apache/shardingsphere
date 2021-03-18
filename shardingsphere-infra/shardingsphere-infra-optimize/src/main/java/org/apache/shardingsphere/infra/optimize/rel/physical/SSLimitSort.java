package org.apache.shardingsphere.infra.optimize.rel.physical;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;

public final class SSLimitSort extends SSSort implements SSRel {
    
    public SSLimitSort(final RelOptCluster cluster, final RelTraitSet traits, final RelNode child, final RelCollation collation, final RexNode offset, final RexNode fetch) {
        super(cluster, traits, child, collation, offset, fetch);
    }
    
    public static SSLimitSort create(RelNode input, RelCollation collation, RexNode offset, RexNode fetch) {
        final RelOptCluster cluster = input.getCluster();
        final RelTraitSet traitSet = cluster.traitSetOf(ShardingSphereConvention.INSTANCE).replace(collation);
        return new SSLimitSort(cluster, traitSet, input, collation, offset, fetch);
    }
}
