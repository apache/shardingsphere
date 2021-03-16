package org.apache.shardingsphere.infra.optimize.rel.physical;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.hint.RelHint;
import org.apache.calcite.rel.metadata.RelMdCollation;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;

import java.util.Collections;
import java.util.List;

public class SSProject extends Project implements SSRel {
    
    protected SSProject(final RelOptCluster cluster, final RelTraitSet traits, final List<RelHint> hints, final RelNode input, final List<? extends RexNode> projects, final RelDataType rowType) {
        super(cluster, traits, hints, input, projects, rowType);
    }
    
    @Override
    public Project copy(final RelTraitSet traitSet, final RelNode input, final List<RexNode> projects, final RelDataType rowType) {
        return new SSProject(this.getCluster(), traitSet, Collections.emptyList(), input, projects, rowType);
    }
    
    public static SSProject create(RelNode input, final List<RexNode> projects, RelDataType relDataType) {
        RelOptCluster cluster = input.getCluster();
        RelMetadataQuery mq = cluster.getMetadataQuery();
        RelTraitSet traitSet = cluster.traitSet().replace(ShardingSphereConvention.INSTANCE)
                .replaceIfs(RelCollationTraitDef.INSTANCE, () -> RelMdCollation.project(mq, input, projects));
        return new SSProject(cluster, traitSet, Collections.emptyList(), input, projects, relDataType);
    }
}
