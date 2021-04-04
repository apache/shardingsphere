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
    
    /**
     * Get outer input relnode.
     * @return outer of this join
     */
    public RelNode getOuter() {
        return this.joinType.generatesNullsOnLeft() ? left : right;
    }
    
    /**
     * Get inner input relnode.
     * @return inner of this join 
     */
    public RelNode getInner() {
        return this.joinType.generatesNullsOnLeft() ? right : left;
    }
}
