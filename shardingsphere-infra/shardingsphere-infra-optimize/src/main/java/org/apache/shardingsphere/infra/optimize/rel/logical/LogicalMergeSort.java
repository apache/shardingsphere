package org.apache.shardingsphere.infra.optimize.rel.logical;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rex.RexNode;

public final class LogicalMergeSort extends Sort {
    
    public LogicalMergeSort(final RelOptCluster cluster, final RelTraitSet traits, final RelNode child, final RelCollation collation) {
        super(cluster, traits, child, collation);
    }
    
    public LogicalMergeSort(final RelOptCluster cluster, final RelTraitSet traits, final RelNode child, final RelCollation collation, final RexNode offset, final RexNode fetch) {
        super(cluster, traits, child, collation, offset, fetch);
    }
    
    @Override
    public Sort copy(final RelTraitSet traitSet, final RelNode newInput, final RelCollation newCollation, final RexNode offset, final RexNode fetch) {
        return new LogicalMergeSort(newInput.getCluster(), traitSet, newInput, newCollation, offset, fetch);
    }
    
    /**
     * Create <code>LogicalMergeSort</code>.
     * @param traitSet relTraitSet represents an ordered set of {@link org.apache.calcite.plan.RelTrait}s.
     * @param input Input of this operator 
     * @param collation Array of sort specifications
     * @param offset    Expression for number of rows to discard before returning first row
     * @param fetch     Expression for number of rows to fetch
     * @return <code>LogicalMergeSort</code> 
     */
    public static LogicalMergeSort create(final RelTraitSet traitSet, final RelNode input, final RelCollation collation,
                                     final RexNode offset, final RexNode fetch) {
        return new LogicalMergeSort(input.getCluster(), traitSet, input, collation, offset, fetch);
    }
    
    /**
     * see {@link #create(RelTraitSet, RelNode, RelCollation, RexNode, RexNode)}.
     * @param traitSet traitSet
     * @param input input
     * @param collation collation
     * @return  <code>LogicalMergeSort</code> 
     */
    public static LogicalMergeSort create(final RelTraitSet traitSet, final RelNode input, final RelCollation collation) {
        return new LogicalMergeSort(input.getCluster(), traitSet, input, collation);
    }
}
