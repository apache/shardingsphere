package org.apache.shardingsphere.infra.optimize.rel.physical;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;

public class SSMergeSort extends Sort implements SSRel {
    
    public SSMergeSort(final RelOptCluster cluster, final RelTraitSet traits, final RelNode child, final RelCollation collation) {
        super(cluster, traits, child, collation);
    }
    
    public SSMergeSort(final RelOptCluster cluster, final RelTraitSet traits, final RelNode child, final RelCollation collation, final RexNode offset, final RexNode fetch) {
        super(cluster, traits, child, collation, offset, fetch);
    }
    
    @Override
    public Sort copy(final RelTraitSet traitSet, final RelNode newInput, final RelCollation newCollation, final RexNode offset, final RexNode fetch) {
        return new SSMergeSort(this.getCluster(), traitSet, newInput, newCollation, offset, fetch);
    }
    
    public static SSMergeSort create(RelTraitSet traitSet, RelNode input, RelCollation collation, RexNode offset, RexNode fetch) {
        return new SSMergeSort(input.getCluster(), traitSet.replace(ShardingSphereConvention.INSTANCE), input, collation, offset, fetch);
    }
    
    public static SSMergeSort create(RelTraitSet traitSet, RelNode input, RelCollation collation) {
        return new SSMergeSort(input.getCluster(), traitSet.replace(ShardingSphereConvention.INSTANCE), input, collation);
    }
}
