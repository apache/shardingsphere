package org.apache.shardingsphere.infra.optimize.rel.physical;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;

/**
 * sort without offset and fetch
 */
public class SSSort extends Sort implements SSRel {
    
    public SSSort(RelOptCluster cluster, RelTraitSet traits, RelNode child, RelCollation collation) {
        super(cluster, traits, child, collation);
    }
    
    protected SSSort(RelOptCluster cluster, RelTraitSet traits, RelNode child, RelCollation collation,
                   RexNode offset, RexNode fetch) {
        super(cluster, traits, child, collation, offset, fetch);
    }
    
    @Override
    public Sort copy(final RelTraitSet traitSet, final RelNode input, final RelCollation newCollation, final RexNode offset, final RexNode fetch) {
        return new SSSort(input.getCluster(), traitSet, input, newCollation, offset, fetch);
    }
    
    public static SSSort create(RelNode input, RelCollation collation) {
        RelOptCluster cluster = input.getCluster();
        RelTraitSet traitSet = cluster.traitSetOf(ShardingSphereConvention.INSTANCE).replace(collation);
        return new SSSort(cluster, traitSet, input, collation);
    }
}
