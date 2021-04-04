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

package org.apache.shardingsphere.infra.optimizer.planner.rule;

import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexDynamicParam;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.infra.optimizer.rel.logical.LogicalMergeSort;
import org.apache.shardingsphere.infra.optimizer.rel.logical.LogicalScan;

import java.math.BigDecimal;
import java.util.function.Predicate;

public final class PushSortToMultiRoutingRule extends PushSortToScanRule {
    
    private static final Predicate<LogicalScan> NOT_SINGLE_ROUTING_PREDICATE = logicalScan -> !logicalScan.isSingleRouting();
    
    /**
     * Creates a RelRule.
     *
     * @param config config
     */
    protected PushSortToMultiRoutingRule(final Config config) {
        super(config);
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalSort sort = call.rel(0);
        LogicalScan scan = call.rel(1);
        RexBuilder rexBuilder = sort.getCluster().getRexBuilder();
        LogicalMergeSort mergeSort;
        if (sort.fetch == null) {
            LogicalScan logicalScan = pushdownSort(LogicalSort.create(scan, sort.getCollation(), null, null), scan);
            mergeSort = LogicalMergeSort.create(sort.getTraitSet(), logicalScan, sort.collation);
        } else {
            RexNode fetchRex;
            if (sort.offset instanceof RexDynamicParam || sort.fetch instanceof RexDynamicParam) {
                fetchRex = rexBuilder.makeCall(SqlStdOperatorTable.PLUS, sort.offset, sort.fetch);
            } else {
                int offset = sort.offset == null ? 0 : RexLiteral.intValue(sort.offset);
                int fetch = sort.fetch == null ? 0 : RexLiteral.intValue(sort.fetch);
                fetchRex = rexBuilder.makeBigintLiteral(new BigDecimal(offset + fetch)); 
            }
            LogicalScan logicalScan = pushdownSort(LogicalSort.create(scan, sort.getCollation(), null, fetchRex), scan);
            mergeSort = LogicalMergeSort.create(sort.getTraitSet(), logicalScan, sort.collation, sort.offset, sort.fetch);
        }
        call.transformTo(mergeSort);
    }
    
    public interface Config extends PushSortToScanRule.Config {
    
        Config DEFAULT = EMPTY.as(Config.class).withOperandFor(LogicalSort.class, LogicalScan.class, NOT_SINGLE_ROUTING_PREDICATE, Config.class);
    
        @Override
        default PushSortToMultiRoutingRule toRule() {
            return new PushSortToMultiRoutingRule(this);
        }
        
    }
}
