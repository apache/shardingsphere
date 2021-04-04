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

import lombok.Getter;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.optimizer.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimizer.rel.AbstractScan;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

public final class SSScan extends AbstractScan implements SSRel {
    
    @Getter
    private final RelNode pushdownRelNode;
    
    public SSScan(final RelOptCluster cluster, final RelTraitSet traitSet, final RelNode pushdownRelNode) {
        super(cluster, traitSet);
        this.traitSet = this.traitSet.replace(ShardingSphereConvention.INSTANCE);
        this.pushdownRelNode = pushdownRelNode;
    }
    
    @Override
    public RouteContext route(final ShardingRule shardingRule) {
        return route(pushdownRelNode, shardingRule);
    }
    
    @Override
    protected RelDataType deriveRowType() {
        return pushdownRelNode.getRowType();
    }
    
    /**
     * create <code>SSScan</code>.
     * @param cluster rel opt cluster, see {@link RelOptCluster#create(org.apache.calcite.plan.RelOptPlanner, org.apache.calcite.rex.RexBuilder)}
     * @param traitSet rel traitset
     * @param pushdownRelNode <code>RelNode</code> that been push down to execute in shard database
     * @return <code>SSScan</code> instance
     */
    public static SSScan create(final RelOptCluster cluster, final RelTraitSet traitSet, final RelNode pushdownRelNode) {
        return new SSScan(cluster, traitSet, pushdownRelNode);
    }
}
