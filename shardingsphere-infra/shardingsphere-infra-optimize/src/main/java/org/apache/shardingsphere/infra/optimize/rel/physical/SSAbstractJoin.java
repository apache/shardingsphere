package org.apache.shardingsphere.infra.optimize.rel.physical;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.CorrelationId;
import org.apache.calcite.rel.core.Join;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.hint.RelHint;
import org.apache.calcite.rex.RexNode;

import java.util.List;
import java.util.Set;

public abstract class SSAbstractJoin extends Join {
    
    protected SSAbstractJoin(final RelOptCluster cluster, final RelTraitSet traitSet, final List<RelHint> hints, 
                             final RelNode left, final RelNode right, final RexNode condition, 
                             final Set<CorrelationId> variablesSet, final JoinRelType joinType) {
        super(cluster, traitSet, hints, left, right, condition, variablesSet, joinType);
    }
    
    public RelNode getOuter() {
        return this.joinType.generatesNullsOnLeft() ? left : right;
    }
    
    public RelNode getInner() {
        return this.joinType.generatesNullsOnLeft() ? right : left;
    }
}
