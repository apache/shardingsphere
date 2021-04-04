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
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.optimizer.planner.ShardingSphereConvention;

/**
 * sort without offset and fetch.
 */
public class SSSort extends Sort implements SSRel {
    
    public SSSort(final RelOptCluster cluster, final RelTraitSet traits, final RelNode child, final RelCollation collation) {
        super(cluster, traits, child, collation);
    }
    
    protected SSSort(final RelOptCluster cluster, final RelTraitSet traits, final RelNode child, final RelCollation collation,
                     final RexNode offset, final RexNode fetch) {
        super(cluster, traits, child, collation, offset, fetch);
    }
    
    /**
     * Clone another Sort instance.
     * @param traitSet relTraitSet represents an ordered set of {@link org.apache.calcite.plan.RelTrait}s.
     * @param input Input of this operator 
     * @param newCollation Array of sort specifications
     * @param offset    Expression for number of rows to discard before returning first row
     * @param fetch     Expression for number of rows to fetch
     * @return new Sort instance
     */
    @Override
    public Sort copy(final RelTraitSet traitSet, final RelNode input, final RelCollation newCollation, final RexNode offset, final RexNode fetch) {
        return new SSSort(input.getCluster(), traitSet, input, newCollation, offset, fetch);
    }
    
    /**
     * Create <code>SSSort</code>.
     * @param input input of Sort
     * @param collation Array of sort specifications
     * @return <code>SSSort</code>
     */
    public static SSSort create(final RelNode input, final RelCollation collation) {
        RelOptCluster cluster = input.getCluster();
        RelTraitSet traitSet = cluster.traitSetOf(ShardingSphereConvention.INSTANCE).replace(collation);
        return new SSSort(cluster, traitSet, input, collation);
    }
}
