package org.apache.shardingsphere.infra.optimize.rel.physical;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;

/**
 * Physical Relational operator that imposes a particular sort order on its input 
 * with rows start from and number of rows to fetch. 
 */
public final class SSLimitSort extends SSSort implements SSRel {
    
    public SSLimitSort(final RelOptCluster cluster, final RelTraitSet traits, final RelNode child, final RelCollation collation, final RexNode offset, final RexNode fetch) {
        super(cluster, traits, child, collation, offset, fetch);
    }
    
    @Override
    public Sort copy(final RelTraitSet traitSet, final RelNode input, final RelCollation collation, final RexNode offset, final RexNode fetch) {
        return new SSLimitSort(input.getCluster(), traitSet, input, collation, offset, fetch);
    }
    
    /**
     * create <code>SSLimitSort</code> instance.
     * @param input Input of <code>SSLimitSort</code>
     * @param collation Array of sort specifications
     * @param offset    Expression for number of rows to discard before returning first row
     * @param fetch     Expression for number of rows to fetch
     * @return <code>SSLimitSort</code>
     */
    public static SSLimitSort create(final RelNode input, final RelCollation collation, final RexNode offset, 
                                     final RexNode fetch) {
        final RelOptCluster cluster = input.getCluster();
        final RelTraitSet traitSet = cluster.traitSetOf(ShardingSphereConvention.INSTANCE).replace(collation);
        return new SSLimitSort(cluster, traitSet, input, collation, offset, fetch);
    }
}
