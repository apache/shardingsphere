/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.optimizer.rel.physical;

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
import org.apache.shardingsphere.infra.optimizer.planner.ShardingSphereConvention;

import java.util.Collections;
import java.util.List;

/**
 * Physical operator Project.
 */
public class SSProject extends Project implements SSRel {
    
    protected SSProject(final RelOptCluster cluster, final RelTraitSet traits, final List<RelHint> hints, final RelNode input, final List<? extends RexNode> projects, final RelDataType rowType) {
        super(cluster, traits, hints, input, projects, rowType);
    }
    
    @Override
    public final Project copy(final RelTraitSet traitSet, final RelNode input, final List<RexNode> projects, final RelDataType rowType) {
        return new SSProject(this.getCluster(), traitSet, Collections.emptyList(), input, projects, rowType);
    }
    
    /**
     * Create <code>SSProject</code>.
     * @param input input of Porject operator
     * @param projects projecttions
     * @param relDataType return data type
     * @return <code>SSProject</code>
     */
    public static SSProject create(final RelNode input, final List<RexNode> projects, final RelDataType relDataType) {
        RelOptCluster cluster = input.getCluster();
        RelMetadataQuery mq = cluster.getMetadataQuery();
        RelTraitSet traitSet = cluster.traitSet().replace(ShardingSphereConvention.INSTANCE)
                .replaceIfs(RelCollationTraitDef.INSTANCE, () -> RelMdCollation.project(mq, input, projects));
        return new SSProject(cluster, traitSet, Collections.emptyList(), input, projects, relDataType);
    }
}
