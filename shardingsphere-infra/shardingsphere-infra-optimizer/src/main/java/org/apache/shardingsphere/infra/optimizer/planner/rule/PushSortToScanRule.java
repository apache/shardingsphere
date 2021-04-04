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
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.calcite.rel.rules.TransformationRule;
import org.apache.shardingsphere.infra.optimizer.rel.logical.LogicalScan;

import java.util.function.Predicate;

public abstract class PushSortToScanRule extends RelRule<PushSortToScanRule.Config> implements TransformationRule {
    
    /**
     * Creates a RelRule.
     *
     * @param config config
     */
    protected PushSortToScanRule(final Config config) {
        super(config);
    }
    
    /**
     * matched rule, then to execute rule.
     * @param call rule call
     */
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalScan logicalScan = pushdownSort(call.rel(0), call.rel(1));
        call.transformTo(logicalScan);
    }
    
    protected final LogicalScan pushdownSort(final LogicalSort logicalSort, final LogicalScan logicalScan) {
        return logicalScan.pushdown(logicalSort);
    }
    
    public interface Config extends RelRule.Config {
    
        /**
         * Untility method.
         * @param sort sort
         * @param scan scan
         * @param predicate predicate before using rule
         * @param config config
         * @param <T> config type
         * @return Config
         */
        default <T extends Config> T withOperandFor(Class<? extends LogicalSort> sort,
                                                         Class<? extends LogicalScan> scan,
                                                         Predicate<LogicalScan> predicate,
                                                         Class<T> config) {
            return withOperandSupplier(b ->
                    b.operand(sort).oneInput(b2 ->
                            b2.operand(scan).predicate(predicate).anyInputs()))
                    .as(config);
        }
    }
}
