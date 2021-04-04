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
 * Physical Relational operator that imposes a particular sort order on its input 
 * with rows start from and number of rows to fetch. 
 */
public final class SSLimitSort extends SSSort implements SSRel {
    
    public SSLimitSort(final RelOptCluster cluster, final RelTraitSet traits, final RelNode child, final RelCollation collation, final RexNode offset, final RexNode fetch) {
        super(cluster, traits, child, collation, offset, fetch);
    }
    
    @Override
    public Sort copy(final RelTraitSet traitSet, final RelNode input, final RelCollation collation, final RexNode offset, final RexNode fetch) {
        return new SSLimitSort(input.getCluster(), traitSet, input, collation, offset, fetch);
    }
    
    /**
     * create <code>SSLimitSort</code> instance.
     * @param input Input of <code>SSLimitSort</code>
     * @param collation Array of sort specifications
     * @param offset    Expression for number of rows to discard before returning first row
     * @param fetch     Expression for number of rows to fetch
     * @return <code>SSLimitSort</code>
     */
    public static SSLimitSort create(final RelNode input, final RelCollation collation, final RexNode offset, 
                                     final RexNode fetch) {
        final RelOptCluster cluster = input.getCluster();
        final RelTraitSet traitSet = cluster.traitSetOf(ShardingSphereConvention.INSTANCE).replace(collation);
        return new SSLimitSort(cluster, traitSet, input, collation, offset, fetch);
    }
}
