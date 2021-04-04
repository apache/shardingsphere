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
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Calc;
import org.apache.calcite.rel.hint.RelHint;
import org.apache.calcite.rel.metadata.RelMdCollation;
import org.apache.calcite.rel.metadata.RelMdDistribution;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rex.RexProgram;
import org.apache.shardingsphere.infra.optimizer.planner.ShardingSphereConvention;

import java.util.Collections;
import java.util.List;

public class SSCalc extends Calc implements SSRel {
    protected SSCalc(final RelOptCluster cluster, final RelTraitSet traits, final List<RelHint> hints, final RelNode child, final RexProgram program) {
        super(cluster, traits, hints, child, program);
    }
    
    @Override
    public final Calc copy(final RelTraitSet traitSet, final RelNode child, final RexProgram program) {
        return new SSCalc(this.getCluster(), traitSet, Collections.emptyList(), child, program);
    }
    
    /**
     * create <code>SSCalc</code> instance.
     * @param input input of <code>SSCalc</code>
     * @param program contains project or condition
     * @return <code>SSCalc</code>
     */
    public static SSCalc create(final RelNode input, final RexProgram program) {
        RelOptCluster cluster = input.getCluster();
        RelMetadataQuery mq = cluster.getMetadataQuery();
        RelTraitSet traitSet = cluster.traitSet()
                .replace(ShardingSphereConvention.INSTANCE)
                .replaceIfs(RelCollationTraitDef.INSTANCE, () -> RelMdCollation.calc(mq, input, program))
                .replaceIf(RelDistributionTraitDef.INSTANCE, () -> RelMdDistribution.calc(mq, input, program));
        return new SSCalc(cluster, traitSet, Collections.emptyList(), input, program);
    }
}
