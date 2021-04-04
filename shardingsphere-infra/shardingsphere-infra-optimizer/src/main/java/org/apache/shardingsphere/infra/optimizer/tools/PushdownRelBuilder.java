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

package org.apache.shardingsphere.infra.optimizer.tools;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptSchema;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.tools.RelBuilder;

/**
 * Pushdown RelNode builder.
 */
public final class PushdownRelBuilder extends RelBuilder {
    
    private PushdownRelBuilder(final RelOptCluster cluster, final RelOptSchema relOptSchema) {
        super(Contexts.of(RelFactories.DEFAULT_STRUCT), cluster, relOptSchema);
    }
    
    /**
     * Push down sort limit.
     * @param offsetRex Expression for number of rows to discard before returning first row
     * @param fetchRex Expression for number of rows to fetch
     * @param nodes Array of sort specifications
     * @return <code>PushdownRelBuilder</code>
     */
    public PushdownRelBuilder sortLimit(final RexNode offsetRex, final RexNode fetchRex, final Iterable<? extends RexNode> nodes) {
        int offset = offsetRex == null ? 0 : RexLiteral.intValue(offsetRex);
        if (fetchRex == null || fetchRex instanceof RexLiteral) {
            int fetch = fetchRex == null ? -1 : RexLiteral.intValue(fetchRex);
            super.sortLimit(offset, fetch, nodes);
        } else {
            // TODO 
            // fetchRex is a RexCall of which one of the operand is RexDynamicParam
            ImmutableList<RexNode> fields = super.fields();
            throw new UnsupportedOperationException();
        }
        return this;
    }
    
    private void replaceTop(final RelNode node) {
        this.pop();
        super.push(node);
    }
    
    protected RelNode pop() {
        return super.build();
    }
    
    /**
     * Create <code>PushdownRelBuilder</code> with a <code>TableScan</code> operator.
     * @param tableScan table scan
     * @return <code>PushdownRelBuilder</code> 
     */
    public static PushdownRelBuilder create(final TableScan tableScan) {
        return new PushdownRelBuilder(tableScan.getCluster(), tableScan.getTable().getRelOptSchema());
    }
}
