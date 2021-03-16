package org.apache.shardingsphere.infra.optimize.rel.physical;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Aggregate;
import org.apache.calcite.rel.core.AggregateCall;
import org.apache.calcite.rel.hint.RelHint;
import org.apache.calcite.util.ImmutableBitSet;

import java.util.Collections;
import java.util.List;

public class SSHashAggregate extends Aggregate implements SSRel {
    
    protected SSHashAggregate(RelOptCluster cluster, RelTraitSet traitSet, List<RelHint> hints, RelNode input, 
                           ImmutableBitSet groupSet, List<ImmutableBitSet> groupSets, List<AggregateCall> aggCalls) {
        super(cluster, traitSet, hints, input, groupSet, groupSets, aggCalls);
    }
    
    @Override
    public Aggregate copy(RelTraitSet traitSet, RelNode input, ImmutableBitSet groupSet, List<ImmutableBitSet> groupSets,
                          List<AggregateCall> aggCalls) {
        return new SSHashAggregate(this.getCluster(), traitSet, Collections.emptyList(), input, groupSet, groupSets, aggCalls);
    }
    
    public static SSHashAggregate create(RelOptCluster cluster, RelTraitSet traitSet, RelNode input, 
                                         ImmutableBitSet groupSet, List<ImmutableBitSet> groupSets,
                                         List<AggregateCall> aggCalls) {
        for (AggregateCall aggCall : aggCalls) {
            if (aggCall.isDistinct()) {
                throw new IllegalArgumentException(
                        "distinct aggregation not supported");
            }
        }
        return new SSHashAggregate(cluster, traitSet, Collections.emptyList(), input, groupSet, groupSets, aggCalls);
        
    }
}
