package org.apache.shardingsphere.infra.optimize.rel.physical;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTrait;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Aggregate;
import org.apache.calcite.rel.core.AggregateCall;
import org.apache.calcite.rel.hint.RelHint;
import org.apache.calcite.util.ImmutableBitSet;

import java.util.Collections;
import java.util.List;

public class SSHashAggregate extends Aggregate implements SSRel {
    
    protected SSHashAggregate(final RelOptCluster cluster, final RelTraitSet traitSet, final List<RelHint> hints, 
                              final RelNode input, final ImmutableBitSet groupSet, final List<ImmutableBitSet> groupSets,
                              final List<AggregateCall> aggCalls) {
        super(cluster, traitSet, hints, input, groupSet, groupSets, aggCalls);
    }
    
    @Override
    public final Aggregate copy(final RelTraitSet traitSet, final RelNode input, final ImmutableBitSet groupSet, 
                          final List<ImmutableBitSet> groupSets, final List<AggregateCall> aggCalls) {
        return new SSHashAggregate(this.getCluster(), traitSet, Collections.emptyList(), input, groupSet, groupSets, aggCalls);
    }
    
    /**
     * create <code>SSHashAggregate</code> operator instance.
     * @param cluster An environment for related relational expressions during theoptimization of a query.
     * @param traitSet RelTraitSet represents an ordered set of {@link RelTrait}s.
     * @param input Input of <code>SSHashAggregate</code> 
     * @param groupSet Group by members
     * @param groupSets Group by members, All members of  groupSets must be sub-sets of  groupSet.
     * @param aggCalls Collection of calls to aggregate functions
     * @return <code>SSHashAggregate</code>
     */
    public static SSHashAggregate create(final RelOptCluster cluster, final RelTraitSet traitSet, final RelNode input,
                                         final ImmutableBitSet groupSet, final List<ImmutableBitSet> groupSets,
                                         final List<AggregateCall> aggCalls) {
        for (AggregateCall aggCall : aggCalls) {
            if (aggCall.isDistinct()) {
                throw new IllegalArgumentException(
                        "distinct aggregation not supported");
            }
        }
        return new SSHashAggregate(cluster, traitSet, Collections.emptyList(), input, groupSet, groupSets, aggCalls);
        
    }
}
