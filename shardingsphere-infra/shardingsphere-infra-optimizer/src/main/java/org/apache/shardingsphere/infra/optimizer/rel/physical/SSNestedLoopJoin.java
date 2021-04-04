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
import org.apache.calcite.rel.core.CorrelationId;
import org.apache.calcite.rel.core.Join;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.hint.RelHint;
import org.apache.calcite.rel.metadata.RelMdCollation;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.optimizer.planner.ShardingSphereConvention;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Physical reation operator nested loop join.
 */
public class SSNestedLoopJoin extends SSAbstractJoin implements SSRel {
    
    protected SSNestedLoopJoin(final RelOptCluster cluster, final RelTraitSet traitSet, final List<RelHint> hints, 
                               final RelNode left, final RelNode right, final RexNode condition, 
                               final Set<CorrelationId> variablesSet, final JoinRelType joinType) {
        super(cluster, traitSet, hints, left, right, condition, variablesSet, joinType);
    }
    
    /**
     * Create <code>SSNestedLoopJoin</code>.
     * @param left left input of this join
     * @param right right input of this join
     * @param condition join condition
     * @param variablesSet variables that are set by the LHS and used by the RHS and are not available to nodes above this Join in the tree
     * @param joinType join type
     * @return <code>SSNestedLoopJoin</code>
     */
    public static SSNestedLoopJoin create(final RelNode left, final RelNode right, final RexNode condition, 
                                 final Set<CorrelationId> variablesSet, final JoinRelType joinType) {
        final RelOptCluster cluster = left.getCluster();
        final RelMetadataQuery mq = cluster.getMetadataQuery();
        final RelTraitSet traitSet =
                cluster.traitSetOf(ShardingSphereConvention.INSTANCE)
                        .replaceIfs(RelCollationTraitDef.INSTANCE, 
                            () -> RelMdCollation.enumerableNestedLoopJoin(mq, left, right, joinType));
        return new SSNestedLoopJoin(cluster, traitSet, Collections.emptyList(), left, right, condition, variablesSet, joinType);
    }
    
    @Override
    public final Join copy(final RelTraitSet traitSet, final RexNode conditionExpr, final RelNode left, final RelNode right, final JoinRelType joinType, final boolean semiJoinDone) {
        return new SSNestedLoopJoin(getCluster(), traitSet, Collections.emptyList(), left, right,
                condition, variablesSet, joinType);
    }
    
}
