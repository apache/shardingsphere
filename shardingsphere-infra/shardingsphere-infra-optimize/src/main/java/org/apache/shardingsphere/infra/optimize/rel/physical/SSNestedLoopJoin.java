package org.apache.shardingsphere.infra.optimize.rel.physical;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.CorrelationId;
import org.apache.calcite.rel.core.Join;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.hint.RelHint;
import org.apache.calcite.rel.metadata.RelMdCollation;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SSNestedLoopJoin extends SSAbstractJoin implements SSRel {
    
    
    protected SSNestedLoopJoin(final RelOptCluster cluster, final RelTraitSet traitSet, final List<RelHint> hints, final RelNode left, final RelNode right, final RexNode condition, final Set<CorrelationId> variablesSet, final JoinRelType joinType) {
        super(cluster, traitSet, hints, left, right, condition, variablesSet, joinType);
    }
    
    public static RelNode create(final RelNode left, final RelNode right, final RexNode condition, final Set<CorrelationId> variablesSet, final JoinRelType joinType) {
        final RelOptCluster cluster = left.getCluster();
        final RelMetadataQuery mq = cluster.getMetadataQuery();
        final RelTraitSet traitSet =
                cluster.traitSetOf(ShardingSphereConvention.INSTANCE)
                        .replaceIfs(RelCollationTraitDef.INSTANCE,
                                () -> RelMdCollation.enumerableNestedLoopJoin(mq, left, right, joinType));
        return new SSNestedLoopJoin(cluster, traitSet, Collections.emptyList(), left, right, condition, variablesSet, joinType);
    }
    
    @Override
    public Join copy(final RelTraitSet traitSet, final RexNode conditionExpr, final RelNode left, final RelNode right, final JoinRelType joinType, final boolean semiJoinDone) {
        return new SSNestedLoopJoin(getCluster(), traitSet, Collections.emptyList(), left, right,
                condition, variablesSet, joinType);
    }
    
    
}
